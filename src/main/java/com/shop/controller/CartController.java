package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Mọi thao tác thay đổi dữ liệu dùng POST rồi redirect (mẫu Post/Redirect/Get),
 * để bấm F5 không gửi lại form. Thông báo được truyền qua "flash attribute".
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String view(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        model.addAttribute("freeShipRemaining", cartService.amountToFreeShipping(cart));
        return "cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      RedirectAttributes flash) {
        try {
            Product product = cartService.addItem(productId, quantity);
            flash.addFlashAttribute("success", "Đã thêm \"" + product.getName() + "\" vào giỏ hàng");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String update(@RequestParam Long productId,
                         @RequestParam int quantity,
                         RedirectAttributes flash) {
        try {
            cartService.updateQuantity(productId, quantity);
            flash.addFlashAttribute("success", "Đã cập nhật giỏ hàng");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId, RedirectAttributes flash) {
        cartService.removeItem(productId);
        flash.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng");
        return "redirect:/cart";
    }
}
