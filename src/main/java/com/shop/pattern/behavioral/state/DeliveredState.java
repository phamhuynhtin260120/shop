package com.shop.pattern.behavioral.state;

/** DELIVERED: đã giao thành công. Trạng thái cuối, mọi hành động bị từ chối. */
public class DeliveredState extends AbstractOrderState {

    @Override
    public String name() { return "DELIVERED"; }

    @Override
    public String label() { return "Đã giao"; }
}
