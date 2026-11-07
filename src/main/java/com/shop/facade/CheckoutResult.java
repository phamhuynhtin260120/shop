package com.shop.facade;

import com.shop.entity.Order;
import com.shop.pattern.structural.adapter.PaymentResult;

/** Kết quả đặt hàng: đơn đã tạo + kết quả thanh toán. */
public record CheckoutResult(Order order, PaymentResult payment) { }
