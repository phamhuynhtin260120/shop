package com.shop.controller;

import com.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang chủ: trả về template templates/index.html */
@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newArrivals", productService.getNewArrivals());
        model.addAttribute("bestSellers", productService.getBestSellers());
        return "index";
    }
}
