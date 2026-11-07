package com.shop.pattern.structural.decorator;

import java.math.BigDecimal;

/** Mẫu DECORATOR – vai trò Component: giao diện chung của mọi bộ tính giá. */
public interface PriceCalculator {
    BigDecimal calculate();
}
