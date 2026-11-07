package com.shop.pattern.behavioral.state;

import com.shop.entity.Order;

/** PENDING: vừa tạo, chờ thanh toán/xác nhận. Được phép: pay, cancel. */
public class PendingState extends AbstractOrderState {

    @Override
    public void pay(Order order) { order.changeState(new PaidState()); }

    @Override
    public void cancel(Order order) { order.changeState(new CancelledState()); }

    @Override
    public boolean canCancel() { return true; }

    @Override
    public String name() { return "PENDING"; }

    @Override
    public String label() { return "Chờ thanh toán"; }
}
