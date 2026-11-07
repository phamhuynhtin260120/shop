package com.shop.entity;

import com.shop.pattern.behavioral.state.OrderState;
import com.shop.pattern.behavioral.state.OrderStateFactory;
import com.shop.pattern.behavioral.state.PendingState;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Đơn hàng.
 *  - Mẫu BUILDER: đơn hàng chỉ được tạo qua Order.builder()...build() (constructor là private).
 *  - Mẫu STATE (vai trò Context): mọi hành động pay/ship/deliver/cancel được ủy quyền
 *    cho đối tượng OrderState hiện tại.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    /** Tên trạng thái lưu trong DB: PENDING, PAID, SHIPPING, DELIVERED, CANCELLED */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 20)
    private String shippingMethod;

    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal discount;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal vat;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderStatusHistory> history = new ArrayList<>();

    /** Đối tượng trạng thái hiện tại; không lưu DB, dựng lại từ cột status khi cần. */
    @Transient
    private OrderState state;

    protected Order() { }   // dành cho JPA

    /** Chỉ OrderBuilder được gọi constructor này. */
    private Order(OrderBuilder b) {
        this.code = b.code;
        this.user = b.customer;
        this.voucher = b.voucher;
        this.shippingMethod = b.shippingMethod;
        this.paymentMethod = b.paymentMethod;
        this.receiverName = b.receiverName;
        this.phone = b.phone;
        this.shippingAddress = b.address;
        this.note = b.note;
        this.subtotal = b.subtotal;
        this.discount = b.discount;
        this.vat = b.vat;
        this.shippingFee = b.shippingFee;
        this.total = b.total;
        for (Line line : b.lines) {
            items.add(new OrderItem(this, line.product(), line.quantity()));
        }
        changeState(new PendingState());   // đơn mới luôn bắt đầu ở PENDING
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    // ================= Mẫu STATE: Context ủy quyền cho State =================

    public OrderState getState() {
        if (state == null) {
            state = OrderStateFactory.from(status);
        }
        return state;
    }

    /** Được các lớp State gọi để chuyển trạng thái; đồng thời ghi lịch sử. */
    public void changeState(OrderState next) {
        String from = status;
        this.state = next;
        this.status = next.name();
        history.add(new OrderStatusHistory(this, from, next.name()));
    }

    public void pay()     { getState().pay(this); }
    public void ship()    { getState().ship(this); }
    public void deliver() { getState().deliver(this); }
    public void cancel()  { getState().cancel(this); }

    // ==========================================================================

    public void addPayment(String method, boolean success, String transactionRef, String message) {
        payments.add(new Payment(this, method, success ? "SUCCESS" : "FAILED", transactionRef, total, message));
    }

    public String getPaymentMethodLabel() {
        return switch (paymentMethod) {
            case "COD" -> "Thanh toán khi nhận hàng";
            case "VNPAY" -> "VNPay";
            case "MOMO" -> "Ví MoMo";
            default -> paymentMethod;
        };
    }

    public String getShippingMethodLabel() {
        return switch (shippingMethod) {
            case "STANDARD" -> "Giao tiêu chuẩn";
            case "EXPRESS" -> "Giao hỏa tốc";
            case "FREE" -> "Miễn phí vận chuyển";
            default -> shippingMethod;
        };
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public User getUser() { return user; }
    public User getCustomer() { return user; }
    public Voucher getVoucher() { return voucher; }
    public String getStatus() { return status; }
    public String getShippingMethod() { return shippingMethod; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReceiverName() { return receiverName; }
    public String getPhone() { return phone; }
    public String getShippingAddress() { return shippingAddress; }
    public String getNote() { return note; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getVat() { return vat; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
    public List<Payment> getPayments() { return payments; }
    public List<OrderStatusHistory> getHistory() { return history; }

    /** Một dòng sản phẩm khi đang xây dựng đơn. */
    private record Line(Product product, int quantity) { }

    // ======================= Mẫu BUILDER =======================

    /**
     * Thu thập dữ liệu đơn hàng từng bước (fluent interface),
     * kiểm tra hợp lệ tập trung trong build() rồi mới tạo Order.
     */
    public static class OrderBuilder {
        private String code;
        private User customer;
        private String receiverName;
        private String phone;
        private String address;
        private String shippingMethod;
        private String paymentMethod;
        private Voucher voucher;
        private String note;
        private final List<Line> lines = new ArrayList<>();
        private BigDecimal subtotal;
        private BigDecimal discount = BigDecimal.ZERO;
        private BigDecimal vat = BigDecimal.ZERO;
        private BigDecimal shippingFee = BigDecimal.ZERO;
        private BigDecimal total;

        private OrderBuilder() { }

        public OrderBuilder code(String code)                { this.code = code; return this; }
        public OrderBuilder customer(User customer)          { this.customer = customer; return this; }
        public OrderBuilder receiver(String name, String phone) {
            this.receiverName = name;
            this.phone = phone;
            return this;
        }
        public OrderBuilder address(String address)          { this.address = address; return this; }
        public OrderBuilder shippingMethod(String method)    { this.shippingMethod = method; return this; }
        public OrderBuilder paymentMethod(String method)     { this.paymentMethod = method; return this; }
        public OrderBuilder voucher(Voucher voucher)         { this.voucher = voucher; return this; }
        public OrderBuilder note(String note)                { this.note = note; return this; }
        public OrderBuilder item(Product product, int quantity) {
            lines.add(new Line(product, quantity));
            return this;
        }
        public OrderBuilder itemsFrom(Cart cart) {
            cart.getItems().forEach(i -> item(i.getProduct(), i.getQuantity()));
            return this;
        }
        public OrderBuilder subtotal(BigDecimal subtotal)    { this.subtotal = subtotal; return this; }
        public OrderBuilder discount(BigDecimal discount)    { this.discount = discount; return this; }
        public OrderBuilder vat(BigDecimal vat)              { this.vat = vat; return this; }
        public OrderBuilder shippingFee(BigDecimal fee)      { this.shippingFee = fee; return this; }
        public OrderBuilder total(BigDecimal total)          { this.total = total; return this; }

        public Order build() {
            if (customer == null) throw new IllegalStateException("Thiếu khách hàng");
            if (lines.isEmpty()) throw new IllegalStateException("Đơn hàng không có sản phẩm");
            if (isBlank(address)) throw new IllegalStateException("Thiếu địa chỉ giao hàng");
            if (isBlank(receiverName) || isBlank(phone)) throw new IllegalStateException("Thiếu tên hoặc số điện thoại người nhận");
            if (isBlank(shippingMethod) || isBlank(paymentMethod)) throw new IllegalStateException("Thiếu phương thức vận chuyển hoặc thanh toán");
            if (lines.stream().anyMatch(l -> l.quantity() < 1)) throw new IllegalStateException("Số lượng sản phẩm phải từ 1 trở lên");

            if (subtotal == null) {
                subtotal = lines.stream()
                        .map(l -> l.product().getPrice().multiply(BigDecimal.valueOf(l.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            if (total == null) {
                total = subtotal.subtract(discount).add(vat).add(shippingFee);
            }
            if (total.signum() < 0) throw new IllegalStateException("Tổng tiền không được âm");
            if (code == null) {
                code = "PS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                        + ThreadLocalRandom.current().nextInt(10, 100);
            }
            return new Order(this);
        }

        private static boolean isBlank(String s) {
            return s == null || s.isBlank();
        }
    }
}
