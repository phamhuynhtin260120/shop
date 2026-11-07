package com.shop.controller;

import com.shop.dto.CheckoutRequest;
import com.shop.entity.Cart;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.facade.CheckoutFacade;
import com.shop.facade.CheckoutResult;
import com.shop.service.CartService;
import com.shop.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller "mỏng": chỉ nhận form, gọi CheckoutFacade và chọn trang hiển thị.
 * Toàn bộ nghiệp vụ đặt hàng nằm sau Facade.
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutFacade checkoutFacade;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CheckoutController(CheckoutFacade checkoutFacade, CartService cartService,
                              CurrentUserService currentUserService) {
        this.checkoutFacade = checkoutFacade;
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    /** GET cũng dùng để "cập nhật tổng tiền" khi khách đổi cách giao hàng hoặc nhập voucher. */
    @GetMapping
    public String form(@ModelAttribute("form") CheckoutRequest form, Model model, RedirectAttributes flash) {
        Cart cart = cartService.getCart();
        if (cart.isEmpty()) {
            flash.addFlashAttribute("error", "Giỏ hàng đang trống");
            return "redirect:/cart";
        }
        if (form.getReceiverName() == null) {           // lần đầu mở trang: điền sẵn thông tin khách
            User user = currentUserService.getCurrentUser();
            form.setReceiverName(user.getFullName());
            form.setPhone(user.getPhone());
            form.setAddress(user.getAddress());
        }
        return render(form, cart, model);
    }

    @PostMapping
    public String place(@Valid @ModelAttribute("form") CheckoutRequest form, BindingResult errors,
                        Model model, RedirectAttributes flash) {
        Cart cart = cartService.getCart();
        if (cart.isEmpty()) {
            flash.addFlashAttribute("error", "Giỏ hàng đang trống");
            return "redirect:/cart";
        }
        if (errors.hasErrors()) {
            return render(form, cart, model);
        }
        try {
            CheckoutResult result = checkoutFacade.placeOrder(form);
            if (result.payment().success()) {
                flash.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn của bạn là " + result.order().getCode());
            } else {
                flash.addFlashAttribute("error", "Đơn " + result.order().getCode()
                        + " đã được tạo nhưng thanh toán thất bại: " + result.payment().message());
            }
            return "redirect:/orders/" + result.order().getId();
        } catch (BusinessException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return render(form, cartService.getCart(), model);
        }
    }

    private String render(CheckoutRequest form, Cart cart, Model model) {
        model.addAttribute("cart", cart);
        model.addAttribute("summary", checkoutFacade.preview(form));
        model.addAttribute("shippingOptions", checkoutFacade.shippingOptions());
        model.addAttribute("paymentOptions", checkoutFacade.paymentOptions());
        return "checkout";
    }
}
