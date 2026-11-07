package com.shop.pattern;

import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.pattern.behavioral.state.OrderStateFactory;
import com.shop.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("State: vòng đời đơn hàng")
class StateTest {

    @Test
    @DisplayName("TC06: hủy đơn đang SHIPPING -> IllegalStateException, trạng thái giữ nguyên")
    void tc06_cancelShippingOrder_isRejected() {
        Order order = TestData.order(TestData.product(1, "200000", 10), 1, "COD");
        order.pay();
        order.ship();

        IllegalStateException e = assertThrows(IllegalStateException.class, order::cancel);

        assertEquals("Không thể hủy đơn đang giao", e.getMessage());
        assertEquals("SHIPPING", order.getStatus());
    }

    @Test
    @DisplayName("TC07: hủy đơn PENDING -> CANCELLED, có ghi lịch sử")
    void tc07_cancelPendingOrder() {
        Order order = TestData.order(TestData.product(1, "200000", 10), 1, "COD");

        order.cancel();

        assertEquals("CANCELLED", order.getStatus());
        assertEquals(2, order.getHistory().size(), "PENDING (lúc tạo) và CANCELLED");
    }

    @Test
    @DisplayName("Luồng chuẩn PENDING -> PAID -> SHIPPING -> DELIVERED")
    void happyPath() {
        Order order = TestData.order(TestData.product(1, "200000", 10), 1, "COD");

        order.pay();
        order.ship();
        order.deliver();

        assertEquals("DELIVERED", order.getStatus());
        assertEquals(4, order.getHistory().size());
        assertThrows(IllegalStateException.class, order::cancel, "Đơn đã giao không được hủy");
    }

    @Test
    @DisplayName("Không thể giao hàng khi chưa thanh toán/xác nhận")
    void cannotShipPendingOrder() {
        Order order = TestData.order(TestData.product(1, "200000", 10), 1, "COD");

        assertThrows(IllegalStateException.class, order::ship);
        assertEquals("PENDING", order.getStatus());
    }

    @Test
    @DisplayName("Hủy đơn đã xác nhận thì hoàn lại tồn kho")
    void cancelPaidOrderRestocks() {
        Product product = TestData.product(1, "200000", 8);
        Order order = TestData.order(product, 2, "COD");
        order.pay();

        order.cancel();

        assertEquals(10, product.getStock());
    }

    @Test
    @DisplayName("Dựng lại đúng lớp trạng thái từ chuỗi lưu trong DB")
    void stateFactory() {
        assertEquals("Đang giao", OrderStateFactory.from("SHIPPING").label());
        assertThrows(IllegalArgumentException.class, () -> OrderStateFactory.from("ABC"));
    }
}
