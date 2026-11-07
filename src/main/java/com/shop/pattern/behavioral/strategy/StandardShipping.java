package com.shop.pattern.behavioral.strategy;

import com.shop.entity.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Giao tiêu chuẩn: 30.000đ cho 3 sản phẩm đầu, mỗi sản phẩm thêm +5.000đ. */
@Component("STANDARD")
public class StandardShipping implements ShippingStrategy {

    static final BigDecimal BASE_FEE = new BigDecimal("30000");
    static final BigDecimal EXTRA_PER_ITEM = new BigDecimal("5000");
    static final int INCLUDED_ITEMS = 3;

    @Override
    public String code() { return "STANDARD"; }

    @Override
    public String label() { return "Giao tiêu chuẩn"; }

    @Override
    public BigDecimal calculateFee(Cart cart) {
        int extra = Math.max(0, cart.getTotalQuantity() - INCLUDED_ITEMS);
        return BASE_FEE.add(EXTRA_PER_ITEM.multiply(BigDecimal.valueOf(extra)));
    }

    @Override
    public int estimatedDays() { return 4; }
}
