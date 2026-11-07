package com.shop.pattern.structural.decorator;

import com.shop.entity.Cart;

import java.math.BigDecimal;

/** Mẫu DECORATOR – vai trò ConcreteComponent: giá gốc (tạm tính) chưa điều chỉnh. */
public class BasePriceCalculator implements PriceCalculator {

    private final BigDecimal subtotal;

    public BasePriceCalculator(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public static BasePriceCalculator of(Cart cart) {
        return new BasePriceCalculator(cart.getSubtotal());
    }

    @Override
    public BigDecimal calculate() {
        return subtotal;
    }
}
