package com.shop.entity;

import com.shop.pattern.behavioral.state.OrderStateFactory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Mỗi lần đơn đổi trạng thái (mẫu State) sẽ ghi 1 dòng vào bảng này. */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 20)
    private String fromStatus;

    @Column(nullable = false, length = 20)
    private String toStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    protected OrderStatusHistory() { }

    OrderStatusHistory(Order order, String fromStatus, String toStatus) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public Long getId() { return id; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }

    public String getToLabel() { return OrderStateFactory.from(toStatus).label(); }
}
