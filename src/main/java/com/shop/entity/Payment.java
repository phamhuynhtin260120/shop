package com.shop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Một lần thanh toán của đơn hàng (có thể thất bại rồi thanh toán lại). */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 100)
    private String transactionRef;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    @Column(length = 255)
    private String message;

    private LocalDateTime paidAt = LocalDateTime.now();

    protected Payment() { }

    Payment(Order order, String method, String status, String transactionRef,
            BigDecimal amount, String message) {
        this.order = order;
        this.method = method;
        this.status = status;
        this.transactionRef = transactionRef;
        this.amount = amount;
        this.message = message;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public String getTransactionRef() { return transactionRef; }
    public BigDecimal getAmount() { return amount; }
    public String getMessage() { return message; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
