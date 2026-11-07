package com.shop.pattern;

import com.shop.entity.Cart;
import com.shop.exception.BusinessException;
import com.shop.pattern.behavioral.strategy.ExpressShipping;
import com.shop.pattern.behavioral.strategy.FreeShipping;
import com.shop.pattern.behavioral.strategy.ShippingContext;
import com.shop.pattern.behavioral.strategy.ShippingStrategy;
import com.shop.pattern.behavioral.strategy.StandardShipping;
import com.shop.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Strategy: phí vận chuyển")
class StrategyTest {

    /** Giống cách Spring gom bean: key là tên bean. */
    private final ShippingContext context = new ShippingContext(Map.of(
            "STANDARD", new StandardShipping(),
            "EXPRESS", new ExpressShipping(),
            "FREE", new FreeShipping()));

    @Test
    @DisplayName("TC03: đơn 600.000đ chọn giao tiêu chuẩn -> tự áp FreeShipping, phí 0đ")
    void tc03_standardOver500k_isFree() {
        Cart cart = TestData.cartWith(TestData.product(1, "300000", 10), 2);

        ShippingStrategy strategy = context.resolve("STANDARD", cart);

        assertInstanceOf(FreeShipping.class, strategy);
        assertEquals(BigDecimal.ZERO, strategy.calculateFee(cart));
    }

    @Test
    @DisplayName("TC04: đơn 300.000đ chọn hỏa tốc -> ExpressShipping, phí 45.000đ")
    void tc04_express() {
        Cart cart = TestData.cartWith(TestData.product(1, "300000", 10), 1);

        ShippingStrategy strategy = context.resolve("EXPRESS", cart);

        assertInstanceOf(ExpressShipping.class, strategy);
        assertEquals(new BigDecimal("45000"), strategy.calculateFee(cart));
    }

    @Test
    @DisplayName("Giao tiêu chuẩn dưới 500.000đ: 30.000đ cho 3 sản phẩm đầu, +5.000đ mỗi sản phẩm thêm")
    void standardFeeByQuantity() {
        Cart cart = TestData.cartWith(TestData.product(1, "50000", 10), 5);

        assertEquals(new BigDecimal("40000"), context.resolve("STANDARD", cart).calculateFee(cart));
    }

    @Test
    @DisplayName("Khách không được tự chọn mã FREE")
    void cannotChooseFreeDirectly() {
        Cart cart = TestData.cartWith(TestData.product(1, "100000", 10), 1);

        assertThrows(BusinessException.class, () -> context.resolve("FREE", cart));
    }
}
