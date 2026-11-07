package com.shop.pattern.creational.factory;

import com.shop.pattern.structural.adapter.MomoAdapter;
import com.shop.pattern.structural.adapter.MomoClient;
import com.shop.pattern.structural.adapter.PaymentMethod;
import org.springframework.stereotype.Component;

/** Mẫu FACTORY METHOD – ConcreteCreator cho ví MoMo. */
@Component("MOMO")
public class MomoCreator extends PaymentCreator {

    private final MomoClient client;

    public MomoCreator(MomoClient client) {
        this.client = client;
    }

    @Override
    protected PaymentMethod createPayment() {
        return new MomoAdapter(client);
    }

    @Override
    public String code() { return "MOMO"; }

    @Override
    public String label() { return "Ví MoMo"; }
}
