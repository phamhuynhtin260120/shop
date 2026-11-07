package com.shop.pattern.structural.adapter;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mẫu ADAPTER – vai trò Adaptee.
 * GIẢ LẬP thư viện MoMo: giao diện hoàn toàn khác VNPay.
 * Ví MoMo giả lập giới hạn 20.000.000đ/giao dịch để minh họa trường hợp thất bại.
 */
@Component
public class MomoClient {

    public static final String PARTNER_CODE = "MOMO-DEMO";
    public static final long LIMIT = 20_000_000L;

    public record MomoResponse(int resultCode, String message, String transId) { }

    public MomoResponse requestPayment(String partnerCode, String orderId, long amount, String signature) {
        if (!PARTNER_CODE.equals(partnerCode) || signature == null || signature.isBlank()) {
            return new MomoResponse(11, "Truy cập bị từ chối", null);
        }
        if (amount > LIMIT) {
            return new MomoResponse(1002, "Giao dịch vượt hạn mức ví", null);
        }
        return new MomoResponse(0, "Thành công", "MM" + UUID.randomUUID().toString().substring(0, 8));
    }
}
