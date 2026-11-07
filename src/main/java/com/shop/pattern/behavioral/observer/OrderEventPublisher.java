package com.shop.pattern.behavioral.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mẫu OBSERVER – vai trò Subject.
 * Lúc khởi động, Spring truyền vào TẤT CẢ bean cài đặt OrderListener và đăng ký chúng.
 * CheckoutFacade chỉ gọi publish(), không biết có bao nhiêu listener và chúng làm gì.
 */
@Component
public class OrderEventPublisher {

    private final List<OrderListener> listeners = new ArrayList<>();

    public OrderEventPublisher(List<OrderListener> initialListeners) {
        listeners.addAll(initialListeners);
    }

    public void subscribe(OrderListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(OrderListener listener) {
        listeners.remove(listener);
    }

    public void publish(OrderPlacedEvent event) {
        for (OrderListener listener : listeners) {
            listener.onOrderPlaced(event);
        }
    }

    public int listenerCount() {
        return listeners.size();
    }
}
