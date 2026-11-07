package com.shop.service;

import com.shop.entity.User;

import java.util.Optional;

/**
 * "Ai đang dùng hệ thống?"
 * Các Service khác chỉ phụ thuộc vào INTERFACE này (nguyên lý DIP).
 * Giai đoạn đầu cài đặt là DemoCurrentUserService (luôn trả về khách mẫu);
 * nay thay bằng SessionCurrentUserService đọc người dùng từ phiên đăng nhập.
 * CartService, OrderService, CheckoutFacade không phải sửa dòng nào.
 */
public interface CurrentUserService {

    /** Người dùng đang đăng nhập; ném NotLoggedInException nếu chưa đăng nhập. */
    User getCurrentUser();

    /** Người dùng đang đăng nhập, hoặc rỗng nếu là khách vãng lai. */
    Optional<User> findCurrentUser();
}
