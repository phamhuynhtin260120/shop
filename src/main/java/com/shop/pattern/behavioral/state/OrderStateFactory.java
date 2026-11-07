package com.shop.pattern.behavioral.state;

/** Dựng lại đối tượng trạng thái từ chuỗi status lưu trong cơ sở dữ liệu. */
public final class OrderStateFactory {

    private OrderStateFactory() { }

    public static OrderState from(String status) {
        if (status == null) {
            return new PendingState();
        }
        return switch (status) {
            case "PENDING" -> new PendingState();
            case "PAID" -> new PaidState();
            case "SHIPPING" -> new ShippingState();
            case "DELIVERED" -> new DeliveredState();
            case "CANCELLED" -> new CancelledState();
            default -> throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        };
    }
}
