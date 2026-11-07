package com.shop.pattern.structural.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Giảm thêm cho thành viên thân thiết (ví dụ 5%). */
public class MemberDiscountDecorator extends PriceDecorator {

    private final int percent;

    public MemberDiscountDecorator(PriceCalculator wrapped, int percent) {
        super(wrapped);
        this.percent = percent;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        BigDecimal discount = base.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return base.subtract(discount);
    }
}
