package com.shop.controller;

import com.shop.entity.Product;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    /** /products?category=ao&q=jeans&best=true&page=0 */
    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "false") boolean best,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Product> result = productService.browse(category, q, best, page);

        String title = "Tất cả sản phẩm";
        if (q != null && !q.isBlank()) {
            title = "Kết quả cho “" + q.trim() + "”";
        } else if (category != null && !category.isBlank()) {
            title = categoryService.findBySlug(category).map(c -> c.getName()).orElse("Danh mục");
        } else if (best) {
            title = "Bán chạy";
        }

        model.addAttribute("page", result);
        model.addAttribute("title", title);
        model.addAttribute("category", category);
        model.addAttribute("q", q);
        model.addAttribute("best", best);
        return "products";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("related", productService.getRelated(product));
        return "product-detail";
    }
}
