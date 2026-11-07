package com.shop.pattern;

import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.pattern.behavioral.observer.EmailListener;
import com.shop.pattern.behavioral.observer.InventoryListener;
import com.shop.pattern.behavioral.observer.LoyaltyListener;
import com.shop.pattern.behavioral.observer.OrderEventPublisher;
import com.shop.pattern.behavioral.observer.OrderListener;
import com.shop.pattern.behavioral.observer.OrderPlacedEvent;
import com.shop.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Observer: sự kiện sau khi đặt hàng")
class ObserverTest {

    @Test
    @DisplayName("TC05: đặt hàng thành công -> cả 3 listener chạy: email, trừ kho, cộng điểm")
    void tc05_allListenersNotified() {
        Product product = TestData.product(1, "500000", 10);
        Order order = TestData.order(product, 2, "COD");            // tổng 1.000.000đ
        int pointsBefore = order.getUser().getLoyaltyPoints();

        OrderEventPublisher publisher = new OrderEventPublisher(
                List.of(new EmailListener(), new InventoryListener(), new LoyaltyListener()));
        publisher.publish(new OrderPlacedEvent(order));

        assertEquals(8, product.getStock(), "Tồn kho phải giảm 2");
        assertEquals(pointsBefore + 100, order.getUser().getLoyaltyPoints(), "1.000.000đ = 100 điểm");
    }

    @Test
    @DisplayName("TC10: thêm SmsListener mới chỉ cần subscribe, không sửa CheckoutFacade (OCP)")
    void tc10_newListenerWithoutModifyingExistingCode() {
        List<String> sent = new ArrayList<>();
        OrderListener smsListener = event -> sent.add("SMS tới " + event.order().getPhone());

        OrderEventPublisher publisher = new OrderEventPublisher(List.of(new EmailListener()));
        publisher.subscribe(smsListener);
        publisher.publish(new OrderPlacedEvent(TestData.order(TestData.product(1, "100000", 5), 1, "COD")));

        assertEquals(2, publisher.listenerCount());
        assertEquals(List.of("SMS tới 0901234567"), sent);
    }

    @Test
    @DisplayName("Hủy đăng ký thì listener không còn nhận sự kiện")
    void unsubscribe() {
        List<String> received = new ArrayList<>();
        OrderListener listener = event -> received.add(event.order().getCode());
        OrderEventPublisher publisher = new OrderEventPublisher(List.of(listener));

        publisher.unsubscribe(listener);
        publisher.publish(new OrderPlacedEvent(TestData.order(TestData.product(1, "100000", 5), 1, "COD")));

        assertTrue(received.isEmpty());
    }
}
