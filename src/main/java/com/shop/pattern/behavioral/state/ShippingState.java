package com.shop.pattern.behavioral.state;

import com.shop.entity.Order;

/** SHIPPING: đang giao. Chỉ được phép deliver; hủy sẽ bị từ chối. */
public class ShippingState extends AbstractOrderState {

    @Override
    public void deliver(Order order) { order.changeState(new DeliveredState()); }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Không thể hủy đơn đang giao");
    }

    @Override
    public boolean canDeliver() { return true; }

    @Override
    public String name() { return "SHIPPING"; }

    @Override
    public String label() { return "Đang giao"; }
}
