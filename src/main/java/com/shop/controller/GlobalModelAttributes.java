package com.shop.controller;

import com.shop.entity.Category;
import com.shop.entity.User;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.CurrentUserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Dữ liệu dùng chung cho MỌI trang (menu danh mục, số lượng giỏ hàng).
 * Nhờ vậy từng Controller không phải tự thêm vào Model.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public GlobalModelAttributes(CategoryService categoryService, CartService cartService,
                                 CurrentUserService currentUserService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    /** Người đang đăng nhập (null nếu là khách vãng lai), dùng cho header. */
    @ModelAttribute("currentUser")
    public User currentUser() {
        return currentUserService.findCurrentUser().orElse(null);
    }

    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        return categoryService.getAll();
    }

    /** Tổng số lượng sản phẩm trong giỏ, hiện trên icon giỏ hàng ở header. */
    @ModelAttribute("cartCount")
    public int cartCount() {
        return cartService.countItems();
    }
}
