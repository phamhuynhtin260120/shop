package com.shop.pattern.creational.factory;

import com.shop.pattern.structural.adapter.PaymentMethod;
import com.shop.pattern.structural.adapter.VnPayAdapter;
import com.shop.pattern.structural.adapter.VnPayClient;
import org.springframework.stereotype.Component;

/** Mẫu FACTORY METHOD – ConcreteCreator: sản phẩm tạo ra chính là một ADAPTER. */
@Component("VNPAY")
public class VnPayCreator extends PaymentCreator {

    private final VnPayClient client;

    public VnPayCreator(VnPayClient client) {
        this.client = client;
    }

    @Override
    protected PaymentMethod createPayment() {
        return new VnPayAdapter(client);
    }

    @Override
    public String code() { return "VNPAY"; }

    @Override
    public String label() { return "Ví / thẻ qua VNPay"; }
}
