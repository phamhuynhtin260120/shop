package com.shop.controller;

import com.shop.entity.Category;
import com.shop.service.CategoryService;
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

    public GlobalModelAttributes(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        return categoryService.getAll();
    }

    /** Tạm thời = 0; sẽ lấy từ CartService ở bước làm giỏ hàng. */
    @ModelAttribute("cartCount")
    public int cartCount() {
        return 0;
    }
}
