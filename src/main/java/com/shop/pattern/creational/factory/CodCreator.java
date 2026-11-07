package com.shop.pattern.creational.factory;

import com.shop.pattern.structural.adapter.CodPayment;
import com.shop.pattern.structural.adapter.PaymentMethod;
import org.springframework.stereotype.Component;

/** Mẫu FACTORY METHOD – ConcreteCreator cho thanh toán khi nhận hàng. */
@Component("COD")
public class CodCreator extends PaymentCreator {

    @Override
    protected PaymentMethod createPayment() {
        return new CodPayment();
    }

    @Override
    public String code() { return "COD"; }

    @Override
    public String label() { return "Thanh toán khi nhận hàng (COD)"; }
}
