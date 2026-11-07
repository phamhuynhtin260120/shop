package com.shop.pattern.behavioral.observer;

import com.shop.entity.Order;

/** Sự kiện "đơn hàng vừa đặt thành công". */
public record OrderPlacedEvent(Order order) { }
