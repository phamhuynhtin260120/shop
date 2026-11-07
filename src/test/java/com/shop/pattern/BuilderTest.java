package com.shop.pattern;

import com.shop.entity.Order;
import com.shop.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Builder: tạo đơn hàng")
class BuilderTest {

    @Test
    @DisplayName("TC08: build() khi đơn không có sản phẩm -> IllegalStateException")
    void tc08_buildWithoutItems_throws() {
        Order.OrderBuilder builder = Order.builder()
                .customer(TestData.user())
                .receiver("Khách", "0901234567")
                .address("Gò Vấp")
                .shippingMethod("STANDARD")
                .paymentMethod("COD");

        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("Đơn hàng không có sản phẩm", e.getMessage());
    }

    @Test
    @DisplayName("Thiếu địa chỉ -> báo lỗi rõ ràng")
    void missingAddress() {
        Order.OrderBuilder builder = Order.builder()
                .customer(TestData.user())
                .receiver("Khách", "0901234567")
                .shippingMethod("STANDARD")
                .paymentMethod("COD")
                .item(TestData.product(1, "100000", 5), 1);

        assertEquals("Thiếu địa chỉ giao hàng", assertThrows(IllegalStateException.class, builder::build).getMessage());
    }

    @Test
    @DisplayName("Đơn hợp lệ: trạng thái PENDING, tự sinh mã, tự tính tổng, lưu giá lúc mua")
    void validOrder() {
        Order order = Order.builder()
                .customer(TestData.user())
                .receiver("Khách", "0901234567")
                .address("Gò Vấp")
                .shippingMethod("STANDARD")
                .paymentMethod("COD")
                .item(TestData.product(1, "150000", 5), 2)
                .shippingFee(new BigDecimal("30000"))
                .build();

        assertEquals("PENDING", order.getStatus());
        assertTrue(order.getCode().startsWith("PS"));
        assertEquals(new BigDecimal("300000"), order.getSubtotal());
        assertEquals(new BigDecimal("330000"), order.getTotal());
        assertEquals(new BigDecimal("150000"), order.getItems().get(0).getUnitPrice());
    }
}
