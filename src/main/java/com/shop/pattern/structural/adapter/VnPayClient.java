package com.shop.pattern.structural.adapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mẫu ADAPTER – vai trò Adaptee.
 * GIẢ LẬP thư viện của VNPay: giao diện riêng (Map tham số + chữ ký), KHÔNG khớp với PaymentMethod.
 * Ta coi như không được sửa lớp này (giống thư viện của bên thứ ba).
 */
@Component
public class VnPayClient {

    public static final String SECRET = "VNPAY-DEMO-SECRET";

    /** Tạo URL chuyển khách sang cổng VNPay. */
    public String createPaymentUrl(Map<String, String> params, String secureHash) {
        StringBuilder url = new StringBuilder("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        params.forEach((k, v) -> url.append(k).append('=').append(v).append('&'));
        return url.append("vnp_SecureHash=").append(secureHash).toString();
    }

    /**
     * Giả lập việc khách thanh toán trên trang VNPay rồi VNPay gọi về (return URL).
     * vnp_ResponseCode "00" là thành công; "24" là khách hủy giao dịch.
     */
    public Map<String, String> simulateReturn(Map<String, String> params, String secureHash) {
        Map<String, String> result = new HashMap<>();
        boolean validHash = HmacSigner.sign(SECRET, params).equals(secureHash);
        result.put("vnp_TxnRef", params.get("vnp_TxnRef"));
        result.put("vnp_ResponseCode", validHash ? "00" : "97");   // 97: sai chữ ký
        result.put("vnp_TransactionNo", validHash ? UUID.randomUUID().toString().substring(0, 8) : "");
        return result;
    }
}
