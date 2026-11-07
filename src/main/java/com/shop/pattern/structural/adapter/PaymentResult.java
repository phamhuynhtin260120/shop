package com.shop.pattern.structural.adapter;

/** Kết quả thanh toán CHUNG của hệ thống, mọi cổng đều được chuyển về kiểu này. */
public record PaymentResult(boolean success, String transactionRef, String message) {

    public static PaymentResult success(String transactionRef, String message) {
        return new PaymentResult(true, transactionRef, message);
    }

    public static PaymentResult failed(String message) {
        return new PaymentResult(false, null, message);
    }
}
