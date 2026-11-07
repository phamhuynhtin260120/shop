package com.shop.controller;

import com.shop.dto.RegisterRequest;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.service.AuthService;
import com.shop.service.SessionCurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Đăng nhập, đăng ký, đăng xuất. */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", safe(redirect));
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        @RequestParam(required = false) String redirect,
                        HttpServletRequest request, Model model, RedirectAttributes flash) {
        try {
            User user = authService.authenticate(email, password);
            signIn(request, user);
            flash.addFlashAttribute("success", "Xin chào " + user.getFullName() + "!");
            String target = safe(redirect);
            return "redirect:" + (target.equals("/") && user.isAdmin() ? "/admin" : target);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("redirect", safe(redirect));
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterRequest form, BindingResult errors,
                           HttpServletRequest request, Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "register";
        }
        try {
            User user = authService.register(form);
            signIn(request, user);
            flash.addFlashAttribute("success", "Đăng ký thành công. Chào mừng " + user.getFullName() + "!");
            return "redirect:/";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes flash) {
        session.invalidate();
        flash.addFlashAttribute("success", "Bạn đã đăng xuất");
        return "redirect:/";
    }

    /** Lưu id người dùng vào session; đổi mã session để chống tấn công session fixation. */
    private void signIn(HttpServletRequest request, User user) {
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(SessionCurrentUserService.SESSION_KEY, user.getId());
    }

    /** Chỉ cho phép quay về đường dẫn nội bộ, tránh bị lợi dụng chuyển hướng sang trang khác. */
    private String safe(String redirect) {
        if (redirect == null || !redirect.startsWith("/") || redirect.startsWith("//")) {
            return "/";
        }
        return redirect;
    }
}
