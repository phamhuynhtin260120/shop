package com.shop.pattern.behavioral.observer;

import com.shop.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Gửi email xác nhận (giả lập: ghi ra log thay vì gửi qua SMTP thật). */
@Component
public class EmailListener implements OrderListener {

    private static final Logger log = LoggerFactory.getLogger(EmailListener.class);

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        Order order = event.order();
        log.info("[EMAIL] Gửi tới {}: Đơn {} đã được đặt thành công, tổng {}đ",
                order.getUser().getEmail(), order.getCode(), order.getTotal().toPlainString());
    }
}
