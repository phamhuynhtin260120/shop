package com.shop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Giỏ hàng: mỗi khách có đúng 1 giỏ (OneToOne với User).
 * Các thao tác trên dòng sản phẩm được đặt NGAY TRONG entity (thêm, sửa, xóa, tính tiền)
 * để CartService không phải biết chi tiết danh sách items được lưu thế nào.
 */
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * cascade = ALL: lưu Cart thì lưu luôn các CartItem.
     * orphanRemoval = true: xóa CartItem khỏi list thì JPA tự DELETE dòng trong DB.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    protected Cart() { }

    public Cart(User user) {
        this.user = user;
    }

    public Optional<CartItem> findItem(Long productId) {
        return items.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();
    }

    /** Số lượng sản phẩm này đang có trong giỏ (0 nếu chưa có). */
    public int quantityOf(Long productId) {
        return findItem(productId).map(CartItem::getQuantity).orElse(0);
    }

    /** Thêm sản phẩm; nếu đã có trong giỏ thì cộng dồn số lượng. */
    public void addItem(Product product, int quantity) {
        findItem(product.getId()).ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + quantity),
                () -> items.add(new CartItem(this, product, quantity)));
    }

    public void updateQuantity(Long productId, int quantity) {
        findItem(productId).ifPresent(item -> item.setQuantity(quantity));
    }

    public void removeItem(Long productId) {
        items.removeIf(i -> i.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    /** Tạm tính = tổng (đơn giá × số lượng), chưa gồm phí ship, giảm giá, VAT. */
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public List<CartItem> getItems() { return items; }
}
