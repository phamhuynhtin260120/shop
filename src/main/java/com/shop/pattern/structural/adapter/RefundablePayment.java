package com.shop.pattern.structural.adapter;

import com.shop.entity.Order;

/**
 * Nguyên lý ISP: tách khả năng hoàn tiền ra interface riêng.
 * Thanh toán online (VNPay, MoMo) hoàn tiền được; COD không bị ép phải cài đặt hàm này.
 */
public interface RefundablePayment extends PaymentMethod {
    PaymentResult refund(Order order);
}
