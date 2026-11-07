package com.shop.pattern.structural.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cộng thuế VAT (ví dụ rate = 0.08 là 8%). Thường được bọc ngoài cùng. */
public class VatDecorator extends PriceDecorator {

    private final BigDecimal rate;

    public VatDecorator(PriceCalculator wrapped, BigDecimal rate) {
        super(wrapped);
        this.rate = rate;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        return base.add(base.multiply(rate)).setScale(0, RoundingMode.HALF_UP);
    }
}
