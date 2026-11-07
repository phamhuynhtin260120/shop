package com.shop.pattern.structural.adapter;

import com.shop.entity.Order;

/** Mẫu ADAPTER – vai trò Target: giao diện thanh toán mà hệ thống sử dụng. */
public interface PaymentMethod {
    PaymentResult pay(Order order);
}
