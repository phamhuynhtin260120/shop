package com.shop.pattern.structural.adapter;

import com.shop.entity.Order;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 * Mẫu ADAPTER – vai trò Adapter.
 * Cài đặt PaymentMethod (Target) và bên trong gọi VnPayClient (Adaptee):
 *   Order -> tham số VNPay -> ký -> gọi API riêng của VNPay -> đổi kết quả về PaymentResult.
 */
public class VnPayAdapter implements RefundablePayment {

    private final VnPayClient client;

    public VnPayAdapter(VnPayClient client) {
        this.client = client;
    }

    @Override
    public PaymentResult pay(Order order) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_TxnRef", order.getCode());
        // VNPay yêu cầu số tiền nhân 100
        params.put("vnp_Amount", order.getTotal().multiply(BigDecimal.valueOf(100)).toPlainString());
        params.put("vnp_OrderInfo", "Thanh toan don " + order.getCode());
        params.put("vnp_CurrCode", "VND");

        String secureHash = HmacSigner.sign(VnPayClient.SECRET, params);
        client.createPaymentUrl(params, secureHash);                      // API riêng của VNPay
        Map<String, String> response = client.simulateReturn(params, secureHash);

        if ("00".equals(response.get("vnp_ResponseCode"))) {
            return PaymentResult.success("VNP" + response.get("vnp_TransactionNo"), "Thanh toán VNPay thành công");
        }
        return PaymentResult.failed("VNPay từ chối giao dịch (mã " + response.get("vnp_ResponseCode") + ")");
    }

    @Override
    public PaymentResult refund(Order order) {
        return PaymentResult.success("VNP-REFUND-" + order.getCode(), "Đã gửi yêu cầu hoàn tiền VNPay");
    }
}
