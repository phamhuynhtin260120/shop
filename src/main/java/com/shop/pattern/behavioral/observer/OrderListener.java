package com.shop.pattern.behavioral.observer;

/** Mẫu OBSERVER – vai trò Observer. */
public interface OrderListener {
    void onOrderPlaced(OrderPlacedEvent event);
}
