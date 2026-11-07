package com.shop.controller.admin;

import com.shop.dto.VoucherForm;
import com.shop.entity.Voucher;
import com.shop.exception.BusinessException;
import com.shop.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Quản lý mã giảm giá. */
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final VoucherService voucherService;

    public AdminVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherService.findAll());
        model.addAttribute("today", LocalDate.now());
        return "admin/vouchers";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new VoucherForm());
        return "admin/voucher-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", VoucherForm.from(voucherService.get(id)));
        return "admin/voucher-form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("form") VoucherForm form, BindingResult errors,
                       Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "admin/voucher-form";
        }
        try {
            Voucher v = voucherService.save(form);
            flash.addFlashAttribute("success", "Đã lưu voucher " + v.getCode());
            return "redirect:/admin/vouchers";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/voucher-form";
        }
    }
}
