package com.shop.facade;

import com.shop.dto.CheckoutRequest;
import com.shop.dto.CheckoutSummary;
import com.shop.entity.Cart;
import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.entity.Voucher;
import com.shop.exception.BusinessException;
import com.shop.pattern.behavioral.observer.OrderEventPublisher;
import com.shop.pattern.behavioral.observer.OrderPlacedEvent;
import com.shop.pattern.behavioral.strategy.ShippingContext;
import com.shop.pattern.behavioral.strategy.ShippingStrategy;
import com.shop.pattern.creational.factory.PaymentCreator;
import com.shop.pattern.structural.adapter.PaymentResult;
import com.shop.pattern.structural.decorator.BasePriceCalculator;
import com.shop.pattern.structural.decorator.MemberDiscountDecorator;
import com.shop.pattern.structural.decorator.PriceCalculator;
import com.shop.pattern.structural.decorator.VatDecorator;
import com.shop.pattern.structural.decorator.VoucherDecorator;
import com.shop.repository.OrderRepository;
import com.shop.service.CartService;
import com.shop.service.CurrentUserService;
import com.shop.service.InventoryService;
import com.shop.service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Mẫu FACADE.
 * Một điểm vào duy nhất cho quy trình đặt hàng nhiều bước. Controller chỉ gọi
 * preview() và placeOrder(); Facade điều phối các hệ thống con và các mẫu thiết kế khác:
 *   Strategy (phí ship) → Decorator (tính giá) → Builder (tạo đơn)
 *   → Factory Method + Adapter (thanh toán) → State (chuyển trạng thái) → Observer (sự kiện).
 */
@Service
public class CheckoutFacade {

    public static final BigDecimal VAT_RATE = new BigDecimal("0.08");
    /** Thành viên có từ 100 điểm được giảm thêm 5%. */
    public static final int VIP_POINTS = 100;
    public static final int VIP_DISCOUNT_PERCENT = 5;

    private final CartService cartService;
    private final CurrentUserService currentUserService;
    private final InventoryService inventoryService;
    private final VoucherService voucherService;
    private final ShippingContext shippingContext;
    private final Map<String, PaymentCreator> paymentCreators;   // key = "COD", "VNPAY", "MOMO"
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public CheckoutFacade(CartService cartService, CurrentUserService currentUserService,
                          InventoryService inventoryService, VoucherService voucherService,
                          ShippingContext shippingContext, Map<String, PaymentCreator> paymentCreators,
                          OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
        this.inventoryService = inventoryService;
        this.voucherService = voucherService;
        this.shippingContext = shippingContext;
        this.paymentCreators = paymentCreators;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    /** Tính tiền để hiển thị (voucher sai thì bỏ qua và báo lý do, không ném lỗi). */
    @Transactional
    public CheckoutSummary preview(CheckoutRequest request) {
        return calculate(cartService.getCart(), currentUserService.getCurrentUser(), request, false);
    }

    /** Đặt hàng. Toàn bộ chạy trong 1 transaction: lỗi ở bất kỳ bước nào thì không lưu gì. */
    @Transactional
    public CheckoutResult placeOrder(CheckoutRequest request) {
        User user = currentUserService.getCurrentUser();
        Cart cart = cartService.getCart();
        if (cart.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }

        // 1. Kiểm tra tồn kho
        inventoryService.checkStock(cart);

        // 2. Strategy + Decorator: tính phí vận chuyển và tổng tiền
        CheckoutSummary summary = calculate(cart, user, request, true);

        // 3. Builder: tạo đơn hàng hợp lệ ở trạng thái PENDING
        Order order = Order.builder()
                .customer(user)
                .receiver(request.getReceiverName().trim(), request.getPhone().trim())
                .address(request.getAddress().trim())
                .note(request.getNote())
                .shippingMethod(summary.shippingCode())
                .paymentMethod(request.getPaymentMethod())
                .voucher(summary.voucher())
                .itemsFrom(cart)
                .subtotal(summary.subtotal())
                .discount(summary.discount())
                .vat(summary.vat())
                .shippingFee(summary.shippingFee())
                .total(summary.total())
                .build();

        // 4. Factory Method + Adapter: thanh toán qua đúng phương thức
        PaymentResult payment = paymentCreator(request.getPaymentMethod()).processPayment(order);
        order.addPayment(request.getPaymentMethod(), payment.success(), payment.transactionRef(), payment.message());

        if (payment.success()) {
            // 5. State: PENDING -> PAID
            order.pay();
            if (summary.voucher() != null) {
                summary.voucher().markUsed();
            }
            // 6. Observer: email, trừ kho, cộng điểm
            eventPublisher.publish(new OrderPlacedEvent(order));
        }

        orderRepository.save(order);
        cartService.clear();
        return new CheckoutResult(order, payment);
    }

    public List<ShippingStrategy> shippingOptions() {
        return shippingContext.options();
    }

    /** Tự động lấy mọi PaymentCreator: thêm cổng mới không cần sửa hàm này (OCP). */
    public List<PaymentCreator> paymentOptions() {
        return List.copyOf(paymentCreators.values());
    }

    // ------------------------------------------------------------------

    private PaymentCreator paymentCreator(String method) {
        PaymentCreator creator = paymentCreators.get(method);
        if (creator == null) {
            throw new BusinessException("Phương thức thanh toán không hợp lệ: " + method);
        }
        return creator;
    }

    private CheckoutSummary calculate(Cart cart, User user, CheckoutRequest request, boolean strictVoucher) {
        BigDecimal subtotal = cart.getSubtotal();

        // Strategy: chọn cách tính phí vận chuyển
        ShippingStrategy shipping = shippingContext.resolve(request.getShippingMethod(), cart);
        BigDecimal shippingFee = shipping.calculateFee(cart);

        // Voucher
        Voucher voucher = null;
        String voucherError = null;
        String code = request.getVoucherCode();
        if (code != null && !code.isBlank()) {
            try {
                voucher = voucherService.findUsable(code, subtotal);
            } catch (BusinessException e) {
                if (strictVoucher) {
                    throw e;
                }
                voucherError = e.getMessage();
            }
        }

        // Decorator: lồng các lớp điều chỉnh giá lên giá gốc
        PriceCalculator calculator = new BasePriceCalculator(subtotal);
        if (voucher != null) {
            calculator = new VoucherDecorator(calculator, voucher);
        }
        int memberPercent = user.getLoyaltyPoints() >= VIP_POINTS ? VIP_DISCOUNT_PERCENT : 0;
        if (memberPercent > 0) {
            calculator = new MemberDiscountDecorator(calculator, memberPercent);
        }
        BigDecimal afterDiscount = calculator.calculate();
        BigDecimal afterVat = new VatDecorator(calculator, VAT_RATE).calculate();

        BigDecimal discount = subtotal.subtract(afterDiscount);
        BigDecimal vat = afterVat.subtract(afterDiscount);
        BigDecimal total = afterVat.add(shippingFee);

        return new CheckoutSummary(subtotal, discount, vat, shippingFee, total,
                shipping.code(), shipping.label(), voucher, voucherError, memberPercent);
    }
}
