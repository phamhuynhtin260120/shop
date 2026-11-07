package com.shop.pattern.creational.factory;

import com.shop.entity.Order;
import com.shop.pattern.structural.adapter.PaymentMethod;
import com.shop.pattern.structural.adapter.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mẫu FACTORY METHOD – vai trò Creator.
 * processPayment() là quy trình chung; bước TẠO đối tượng thanh toán (createPayment)
 * được để cho lớp con quyết định. CheckoutFacade chỉ biết PaymentCreator, không biết
 * VnPayAdapter hay MomoAdapter tồn tại.
 */
public abstract class PaymentCreator {

    private static final Logger log = LoggerFactory.getLogger(PaymentCreator.class);

    public PaymentResult processPayment(Order order) {
        PaymentMethod method = createPayment();          // factory method
        PaymentResult result = method.pay(order);
        log.info("Thanh toán đơn {} bằng {}: {}", order.getCode(),
                method.getClass().getSimpleName(), result.success() ? "THÀNH CÔNG" : "THẤT BẠI");
        return result;
    }

    /** Factory method: lớp con ghi đè để tạo đúng loại PaymentMethod. */
    protected abstract PaymentMethod createPayment();

    /** Mã phương thức: COD, VNPAY, MOMO (trùng tên bean). */
    public abstract String code();

    /** Tên hiển thị ở trang thanh toán. */
    public abstract String label();
}
