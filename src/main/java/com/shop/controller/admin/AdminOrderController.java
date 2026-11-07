package com.shop.controller.admin;

import com.shop.entity.Order;
import com.shop.exception.BusinessException;
import com.shop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Quản trị đơn hàng (chưa có phân quyền: sẽ chặn bằng đăng nhập ADMIN sau).
 * Dùng để demo mẫu State: giao hàng, xác nhận đã giao, hủy.
 */
@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.allOrders());
        return "admin/orders";
    }

    @PostMapping("/{id}/{action}")
    public String change(@PathVariable Long id, @PathVariable String action, RedirectAttributes flash) {
        try {
            Order order = switch (action) {
                case "ship" -> orderService.ship(id);
                case "deliver" -> orderService.deliver(id);
                case "cancel" -> orderService.cancel(id);
                default -> throw new BusinessException("Hành động không hợp lệ: " + action);
            };
            flash.addFlashAttribute("success", "Đơn " + order.getCode() + " chuyển sang \"" + order.getState().label() + "\"");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
