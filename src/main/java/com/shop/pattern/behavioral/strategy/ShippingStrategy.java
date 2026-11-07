package com.shop.pattern.behavioral.strategy;

import com.shop.entity.Cart;

import java.math.BigDecimal;

/**
 * Mẫu STRATEGY – vai trò Strategy.
 * Mỗi cách tính phí vận chuyển là một lớp riêng, thay thế được cho nhau lúc chạy.
 * Quy ước (LSP): phí trả về luôn >= 0 và không ném ngoại lệ.
 */
public interface ShippingStrategy {

    /** Mã lưu vào đơn hàng: STANDARD, EXPRESS, FREE */
    String code();

    String label();

    BigDecimal calculateFee(Cart cart);

    int estimatedDays();
}
