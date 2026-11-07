package com.shop.pattern.behavioral.state;

import com.shop.entity.Order;

/**
 * Mẫu STATE – vai trò State.
 * Mỗi trạng thái của đơn hàng là một lớp cài đặt interface này và tự quyết định:
 * hành động nào được phép, chuyển sang trạng thái nào, hành động nào bị từ chối.
 */
public interface OrderState {

    void pay(Order order);

    void ship(Order order);

    void deliver(Order order);

    void cancel(Order order);

    /** Tên lưu trong DB: PENDING, PAID... */
    String name();

    /** Tên hiển thị tiếng Việt. */
    String label();

    // Dùng cho giao diện: hiện/ẩn nút theo trạng thái
    default boolean canCancel()  { return false; }
    default boolean canShip()    { return false; }
    default boolean canDeliver() { return false; }
}
