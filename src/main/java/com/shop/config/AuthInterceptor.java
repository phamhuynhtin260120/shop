package com.shop.config;

import com.shop.entity.User;
import com.shop.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Chạy TRƯỚC mọi Controller của các đường dẫn được đăng ký trong WebConfig.
 *  - Chưa đăng nhập  -> chuyển tới /login (kèm trang muốn quay lại).
 *  - Vào /admin mà không phải ADMIN -> về trang chủ kèm thông báo.
 * Nhờ vậy các Controller không phải tự kiểm tra quyền (SRP).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final CurrentUserService currentUserService;

    public AuthInterceptor(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {
        Optional<User> user = currentUserService.findCurrentUser();
        if (user.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/login?required&redirect="
                    + URLEncoder.encode(returnUrl(req), StandardCharsets.UTF_8));
            return false;
        }
        if (req.getRequestURI().startsWith(req.getContextPath() + "/admin") && !user.get().isAdmin()) {
            res.sendRedirect(req.getContextPath() + "/?denied");
            return false;
        }
        return true;
    }

    /** Trang sẽ quay lại sau khi đăng nhập. Với POST (ví dụ "Thêm vào giỏ") thì quay về trang trước đó. */
    private String returnUrl(HttpServletRequest req) {
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            String query = req.getQueryString();
            return req.getRequestURI().substring(req.getContextPath().length()) + (query == null ? "" : "?" + query);
        }
        String referer = req.getHeader("Referer");
        if (referer != null) {
            String host = req.getScheme() + "://" + req.getHeader("Host");
            if (referer.startsWith(host)) {
                return referer.substring(host.length());
            }
        }
        return "/";
    }
}
