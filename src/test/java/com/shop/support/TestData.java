package com.shop.support;

import com.shop.entity.Cart;
import com.shop.entity.Category;
import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.entity.User;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Dữ liệu dùng chung cho unit test. Không cần database:
 * id được gán bằng reflection thay cho việc JPA tự sinh.
 */
public final class TestData {

    private TestData() { }

    public static Category category() {
        return withId(new Category("Áo", "ao"), 1L);
    }

    public static Product product(long id, String price, int stock) {
        return withId(new Product("Sản phẩm " + id, "Mô tả", new BigDecimal(price), stock, category(), false), id);
    }

    public static User user() {
        return withId(new User("khach@test.vn", "Khách Test", "0901234567", "12 Nguyễn Văn Bảo, Gò Vấp"), 1L);
    }

    public static Cart cartWith(Product product, int quantity) {
        Cart cart = new Cart(user());
        cart.addItem(product, quantity);
        return cart;
    }

    /** Đơn hàng hợp lệ (PENDING) gồm 1 sản phẩm. */
    public static Order order(Product product, int quantity, String paymentMethod) {
        return Order.builder()
                .customer(user())
                .receiver("Khách Test", "0901234567")
                .address("12 Nguyễn Văn Bảo, Gò Vấp")
                .shippingMethod("STANDARD")
                .paymentMethod(paymentMethod)
                .item(product, quantity)
                .build();
    }

    public static <T> T withId(T entity, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
