package com.shop.dto;

import com.shop.entity.Voucher;

import java.math.BigDecimal;

/** Bảng tính tiền hiển thị ở trang thanh toán và dùng để tạo đơn. */
public record CheckoutSummary(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal shippingFee,
        BigDecimal total,
        String shippingCode,
        String shippingLabel,
        Voucher voucher,
        String voucherError,
        int memberDiscountPercent) {
}
