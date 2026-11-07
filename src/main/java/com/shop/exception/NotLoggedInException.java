package com.shop.exception;

/** Gọi chức năng cần đăng nhập khi chưa đăng nhập (bình thường AuthInterceptor đã chặn trước). */
public class NotLoggedInException extends RuntimeException {
    public NotLoggedInException() {
        super("Vui lòng đăng nhập để tiếp tục");
    }
}
