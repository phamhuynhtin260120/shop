package com.shop.controller.admin;

import com.shop.dto.ProductForm;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import com.shop.storage.ImageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quản lý sản phẩm: danh sách, thêm, sửa, ẩn/hiện. */
@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;

    public AdminProductController(ProductService productService, CategoryService categoryService,
                                  ImageService imageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAllForAdmin());
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        return form(new ProductForm(), model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return form(ProductForm.from(productService.getForAdmin(id)), model);
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("form") ProductForm form, BindingResult errors,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return form(form, model);
        }
        try {
            // Có chọn file từ máy thì tải lên và dùng ảnh đó (ưu tiên hơn ô đường dẫn)
            if (imageFile != null && !imageFile.isEmpty()) {
                form.setImageUrl(imageService.upload(imageFile));
            }
            Product p = productService.save(form);
            flash.addFlashAttribute("success", "Đã lưu sản phẩm \"" + p.getName() + "\"");
            return "redirect:/admin/products";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return form(form, model);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes flash) {
        Product p = productService.toggleActive(id);
        flash.addFlashAttribute("success", (p.isActive() ? "Đã hiện " : "Đã ẩn ") + "sản phẩm \"" + p.getName() + "\"");
        return "redirect:/admin/products";
    }

    private String form(ProductForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("categories", categoryService.getAll());
        return "admin/product-form";
    }
}
