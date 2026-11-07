package com.shop.pattern.behavioral.state;

/** CANCELLED: đã hủy. Trạng thái cuối, mọi hành động bị từ chối. */
public class CancelledState extends AbstractOrderState {

    @Override
    public String name() { return "CANCELLED"; }

    @Override
    public String label() { return "Đã hủy"; }
}
