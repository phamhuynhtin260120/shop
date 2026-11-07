package com.shop.pattern.behavioral.strategy;

import com.shop.entity.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Giao hỏa tốc trong 24 giờ: 45.000đ cố định, không áp dụng miễn phí. */
@Component("EXPRESS")
public class ExpressShipping implements ShippingStrategy {

    static final BigDecimal FEE = new BigDecimal("45000");

    @Override
    public String code() { return "EXPRESS"; }

    @Override
    public String label() { return "Giao hỏa tốc (24 giờ)"; }

    @Override
    public BigDecimal calculateFee(Cart cart) { return FEE; }

    @Override
    public int estimatedDays() { return 1; }
}
