package com.shop.pattern.behavioral.observer;

import com.shop.entity.Product;
import org.springframework.stereotype.Component;

/** Trừ tồn kho theo số lượng trong đơn. */
@Component
public class InventoryListener implements OrderListener {

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        event.order().getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
        });
    }
}
