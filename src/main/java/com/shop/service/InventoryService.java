package com.shop.service;

import com.shop.entity.Cart;
import com.shop.exception.BusinessException;
import org.springframework.stereotype.Service;

/** Kiểm tra tồn kho trước khi đặt hàng (việc TRỪ kho do InventoryListener làm sau khi đặt thành công). */
@Service
public class InventoryService {

    public void checkStock(Cart cart) {
        cart.getItems().forEach(item -> {
            if (item.getQuantity() > item.getProduct().getStock()) {
                throw new BusinessException("Sản phẩm \"" + item.getProduct().getName() + "\" chỉ còn "
                        + item.getProduct().getStock() + " cái, vui lòng giảm số lượng trong giỏ");
            }
        });
    }
}
