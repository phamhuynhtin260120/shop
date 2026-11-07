package com.shop.pattern.structural.decorator;

import com.shop.entity.Voucher;

import java.math.BigDecimal;

/** Trừ tiền theo voucher (phần trăm hoặc số tiền cố định). */
public class VoucherDecorator extends PriceDecorator {

    private final Voucher voucher;

    public VoucherDecorator(PriceCalculator wrapped, Voucher voucher) {
        super(wrapped);
        this.voucher = voucher;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        return base.subtract(voucher.discountFor(base));
    }
}
