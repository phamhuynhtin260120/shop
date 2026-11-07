package com.shop.pattern.behavioral.state;

import com.shop.entity.Order;

/**
 * PAID: đã thanh toán online, hoặc đã xác nhận với đơn COD. Được phép: ship, cancel.
 * Hủy ở trạng thái này thì hoàn lại tồn kho (tiền hoàn qua cổng thanh toán – giả lập).
 */
public class PaidState extends AbstractOrderState {

    @Override
    public void ship(Order order) { order.changeState(new ShippingState()); }

    @Override
    public void cancel(Order order) {
        order.getItems().forEach(i -> i.getProduct().setStock(i.getProduct().getStock() + i.getQuantity()));
        order.changeState(new CancelledState());
    }

    @Override
    public boolean canCancel() { return true; }

    @Override
    public boolean canShip() { return true; }

    @Override
    public String name() { return "PAID"; }

    @Override
    public String label() { return "Đã xác nhận"; }
}
