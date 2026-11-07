package com.shop.exception;

/**
 * Lỗi nghiệp vụ có thể báo thẳng cho người dùng (hết hàng, số lượng không hợp lệ...).
 * Controller bắt lỗi này và hiển thị message dưới dạng thông báo trên trang.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
