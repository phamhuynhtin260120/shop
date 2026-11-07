package com.shop.pattern.structural.adapter;

import com.shop.entity.Order;

import java.util.Map;

/**
 * Mẫu ADAPTER – vai trò Adapter cho MoMo.
 * Chuyển lời gọi pay(Order) thành requestPayment(partnerCode, orderId, amount, signature)
 * và đổi MomoResponse về PaymentResult chung.
 */
public class MomoAdapter implements RefundablePayment {

    private final MomoClient client;

    public MomoAdapter(MomoClient client) {
        this.client = client;
    }

    @Override
    public PaymentResult pay(Order order) {
        long amount = order.getTotal().longValueExact();
        String signature = HmacSigner.sign(MomoClient.PARTNER_CODE,
                Map.of("orderId", order.getCode(), "amount", String.valueOf(amount)));

        MomoClient.MomoResponse response =
                client.requestPayment(MomoClient.PARTNER_CODE, order.getCode(), amount, signature);

        if (response.resultCode() == 0) {
            return PaymentResult.success(response.transId(), "Thanh toán MoMo thành công");
        }
        return PaymentResult.failed("MoMo: " + response.message());
    }

    @Override
    public PaymentResult refund(Order order) {
        return PaymentResult.success("MOMO-REFUND-" + order.getCode(), "Đã gửi yêu cầu hoàn tiền MoMo");
    }
}
