package com.shop.service;

import com.shop.entity.Cart;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.pattern.behavioral.strategy.FreeShipping;
import com.shop.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Nghiệp vụ giỏ hàng. Quy tắc chính:
 *  - Số lượng phải >= 1.
 *  - Tổng số lượng trong giỏ không được vượt tồn kho.
 *  - Thêm sản phẩm đã có thì cộng dồn.
 */
@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public CartService(CartRepository cartRepository, ProductService productService,
                       CurrentUserService currentUserService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    /** Giỏ của khách hiện tại; chưa có thì tạo mới. */
    public Cart getCart() {
        User user = currentUserService.getCurrentUser();
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    /** Số lượng trên icon giỏ hàng; khách chưa đăng nhập thì là 0. */
    @Transactional(readOnly = true)
    public int countItems() {
        return currentUserService.findCurrentUser()
                .flatMap(user -> cartRepository.findByUserId(user.getId()))
                .map(Cart::getTotalQuantity)
                .orElse(0);
    }

    public Product addItem(Long productId, int quantity) {
        requirePositive(quantity);
        Product product = productService.getById(productId);
        Cart cart = getCart();
        ensureStock(product, cart.quantityOf(productId) + quantity);
        cart.addItem(product, quantity);
        return product;
    }

    /** quantity <= 0 được hiểu là xóa sản phẩm khỏi giỏ. */
    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeItem(productId);
            return;
        }
        Product product = productService.getById(productId);
        ensureStock(product, quantity);
        getCart().updateQuantity(productId, quantity);
    }

    public void removeItem(Long productId) {
        getCart().removeItem(productId);
    }

    /** Dùng sau khi đặt hàng thành công (CheckoutFacade). */
    public void clear() {
        getCart().clear();
    }

    /** Số tiền còn thiếu để được miễn phí vận chuyển (0 nếu đã đủ). */
    public BigDecimal amountToFreeShipping(Cart cart) {
        BigDecimal remaining = FreeShipping.THRESHOLD.subtract(cart.getSubtotal());
        return remaining.signum() > 0 ? remaining : BigDecimal.ZERO;
    }

    private void requirePositive(int quantity) {
        if (quantity < 1) {
            throw new BusinessException("Số lượng phải từ 1 trở lên");
        }
    }

    private void ensureStock(Product product, int requested) {
        if (requested > product.getStock()) {
            throw new BusinessException("Chỉ còn " + product.getStock() + " sản phẩm \""
                    + product.getName() + "\" trong kho");
        }
    }
}
