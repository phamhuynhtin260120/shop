package com.shop.pattern.structural.decorator;

/**
 * Mẫu DECORATOR – vai trò Decorator.
 * Vừa LÀ một PriceCalculator, vừa CHỨA một PriceCalculator khác bên trong (wrapped).
 * Lớp con gọi wrapped.calculate() rồi điều chỉnh kết quả.
 */
public abstract class PriceDecorator implements PriceCalculator {

    protected final PriceCalculator wrapped;

    protected PriceDecorator(PriceCalculator wrapped) {
        this.wrapped = wrapped;
    }
}
