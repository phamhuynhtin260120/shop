package com.shop.pattern.behavioral.state;

import com.shop.entity.Order;

/**
 * Mặc định: mọi hành động đều bị TỪ CHỐI.
 * Mỗi lớp trạng thái con chỉ ghi đè những hành động hợp lệ với nó
 * (tương ứng các ô "→" trong bảng chuyển trạng thái của báo cáo).
 */
public abstract class AbstractOrderState implements OrderState {

    @Override
    public void pay(Order order) { reject("thanh toán"); }

    @Override
    public void ship(Order order) { reject("giao hàng"); }

    @Override
    public void deliver(Order order) { reject("xác nhận đã giao"); }

    @Override
    public void cancel(Order order) { reject("hủy"); }

    protected void reject(String action) {
        throw new IllegalStateException("Không thể " + action + " đơn hàng đang ở trạng thái \"" + label() + "\"");
    }
}
