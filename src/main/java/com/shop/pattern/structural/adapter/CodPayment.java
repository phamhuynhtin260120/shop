package com.shop.pattern.structural.adapter;

import com.shop.entity.Order;

/** Thanh toán khi nhận hàng: không gọi cổng nào, chỉ ghi nhận đơn đã được xác nhận. */
public class CodPayment implements PaymentMethod {

    @Override
    public PaymentResult pay(Order order) {
        return PaymentResult.success("COD-" + order.getCode(), "Thanh toán khi nhận hàng");
    }
}
