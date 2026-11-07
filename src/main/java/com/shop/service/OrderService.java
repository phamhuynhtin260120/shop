package com.shop.service;

import com.shop.entity.Order;
import com.shop.exception.BusinessException;
import com.shop.exception.OrderNotFoundException;
import com.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

/**
 * Xem và cập nhật đơn hàng. Việc chuyển trạng thái được ủy quyền cho Order (mẫu State):
 * Service KHÔNG có câu lệnh if (status == ...) nào.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    public OrderService(OrderRepository orderRepository, CurrentUserService currentUserService) {
        this.orderRepository = orderRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<Order> myOrders() {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId());
    }

    @Transactional(readOnly = true)
    public Order myOrder(Long id) {
        Order order = find(id);
        if (!order.getUser().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }

    public Order cancelMyOrder(Long id) {
        return transition(myOrder(id), Order::cancel);
    }

    // ---------- Dành cho quản trị viên ----------

    @Transactional(readOnly = true)
    public List<Order> allOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order ship(Long id)    { return transition(find(id), Order::ship); }
    public Order deliver(Long id) { return transition(find(id), Order::deliver); }
    public Order cancel(Long id)  { return transition(find(id), Order::cancel); }

    private Order find(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    /** Thực hiện hành động; nếu State từ chối thì đổi thành lỗi nghiệp vụ để hiện cho người dùng. */
    private Order transition(Order order, Consumer<Order> action) {
        try {
            action.accept(order);
            return order;
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
