package com.shop.pattern;

import com.shop.entity.Cart;
import com.shop.exception.BusinessException;
import com.shop.service.InventoryService;
import com.shop.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Facade: bước kiểm tra tồn kho trước khi đặt hàng")
class FacadeInventoryTest {

    @Test
    @DisplayName("TC09: đặt 5 sản phẩm khi kho còn 3 -> báo hết hàng, không tạo đơn")
    void tc09_notEnoughStock() {
        Cart cart = TestData.cartWith(TestData.product(1, "100000", 3), 5);

        BusinessException e = assertThrows(BusinessException.class, () -> new InventoryService().checkStock(cart));

        assertTrue(e.getMessage().contains("chỉ còn 3"));
    }

    @Test
    @DisplayName("Đủ hàng thì không báo lỗi")
    void enoughStock() {
        Cart cart = TestData.cartWith(TestData.product(1, "100000", 3), 3);

        assertDoesNotThrow(() -> new InventoryService().checkStock(cart));
    }
}
