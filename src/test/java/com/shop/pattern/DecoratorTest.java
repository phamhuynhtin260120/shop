package com.shop.pattern;

import com.shop.entity.Voucher;
import com.shop.pattern.structural.decorator.BasePriceCalculator;
import com.shop.pattern.structural.decorator.MemberDiscountDecorator;
import com.shop.pattern.structural.decorator.PriceCalculator;
import com.shop.pattern.structural.decorator.VatDecorator;
import com.shop.pattern.structural.decorator.VoucherDecorator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Decorator: tính tổng tiền")
class DecoratorTest {

    private static final BigDecimal VAT = new BigDecimal("0.08");

    @Test
    @DisplayName("TC02: 1.000.000đ, voucher 10%, VAT 8% -> 972.000đ")
    void tc02_voucher10_vat8() {
        PriceCalculator calc = new BasePriceCalculator(new BigDecimal("1000000"));
        calc = new VoucherDecorator(calc, Voucher.percent(10));
        calc = new VatDecorator(calc, VAT);

        assertEquals(new BigDecimal("972000"), calc.calculate());
    }

    @Test
    @DisplayName("Lồng thêm giảm giá thành viên 5%: 1.000.000 -> 900.000 -> 855.000 -> 923.400")
    void stackedDecorators() {
        PriceCalculator calc = new VatDecorator(
                new MemberDiscountDecorator(
                        new VoucherDecorator(new BasePriceCalculator(new BigDecimal("1000000")), Voucher.percent(10)),
                        5),
                VAT);

        assertEquals(new BigDecimal("923400"), calc.calculate());
    }

    @Test
    @DisplayName("Voucher cố định không làm giá âm")
    void fixedVoucherNeverNegative() {
        PriceCalculator calc = new VoucherDecorator(new BasePriceCalculator(new BigDecimal("30000")), Voucher.fixed(50000));

        assertEquals(0, calc.calculate().signum());
    }
}
