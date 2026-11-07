package com.shop.pattern.behavioral.strategy;

import com.shop.entity.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Miễn phí vận chuyển: tự áp dụng cho giao tiêu chuẩn khi tạm tính từ 500.000đ. */
@Component("FREE")
public class FreeShipping implements ShippingStrategy {

    public static final BigDecimal THRESHOLD = new BigDecimal("500000");

    @Override
    public String code() { return "FREE"; }

    @Override
    public String label() { return "Miễn phí vận chuyển"; }

    @Override
    public BigDecimal calculateFee(Cart cart) { return BigDecimal.ZERO; }

    @Override
    public int estimatedDays() { return 4; }
}
