package com.shop.pattern.behavioral.observer;

import com.shop.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Cộng điểm tích lũy: cứ 10.000đ được 1 điểm. */
@Component
public class LoyaltyListener implements OrderListener {

    static final BigDecimal VND_PER_POINT = new BigDecimal("10000");

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        Order order = event.order();
        int points = order.getTotal().divideToIntegralValue(VND_PER_POINT).intValue();
        order.getUser().addPoints(points);
    }
}
