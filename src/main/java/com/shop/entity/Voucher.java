package com.shop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Mã giảm giá: PERCENT (giảm %) hoặc FIXED (giảm số tiền cố định). */
@Entity
@Table(name = "vouchers")
public class Voucher {

    public static final String PERCENT = "PERCENT";
    public static final String FIXED = "FIXED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 10)
    private String type;

    /** PERCENT: 10 nghĩa là 10%. FIXED: 50000 nghĩa là 50.000đ */
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal value;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal minOrder = BigDecimal.ZERO;

    private LocalDate expiresAt;

    private int usageLimit;

    private int usedCount;

    protected Voucher() { }

    public Voucher(String code, String type, BigDecimal value, BigDecimal minOrder,
                   LocalDate expiresAt, int usageLimit) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.minOrder = minOrder;
        this.expiresAt = expiresAt;
        this.usageLimit = usageLimit;
    }

    public static Voucher percent(int percent) {
        return new Voucher("TEST" + percent, PERCENT, BigDecimal.valueOf(percent), BigDecimal.ZERO, null, 0);
    }

    public static Voucher fixed(long amount) {
        return new Voucher("TEST" + amount, FIXED, BigDecimal.valueOf(amount), BigDecimal.ZERO, null, 0);
    }

    public boolean isExpired(LocalDate today) {
        return expiresAt != null && today.isAfter(expiresAt);
    }

    public boolean isUsedUp() {
        return usageLimit > 0 && usedCount >= usageLimit;
    }

    /** Số tiền được giảm khi áp vào một khoản tiền (không giảm quá số tiền đó). */
    public BigDecimal discountFor(BigDecimal amount) {
        BigDecimal discount = PERCENT.equals(type)
                ? amount.multiply(value).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : value;
        return discount.min(amount);
    }

    public String getDescription() {
        return PERCENT.equals(type) ? "Giảm " + value.toPlainString() + "%" : "Giảm " + value.toPlainString() + "đ";
    }

    public void markUsed() { usedCount++; }

    /** Cập nhật từ form quản trị. */
    public void update(String code, String type, BigDecimal value, BigDecimal minOrder,
                       LocalDate expiresAt, int usageLimit) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.minOrder = minOrder;
        this.expiresAt = expiresAt;
        this.usageLimit = usageLimit;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getType() { return type; }
    public BigDecimal getValue() { return value; }
    public BigDecimal getMinOrder() { return minOrder; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public int getUsageLimit() { return usageLimit; }
    public int getUsedCount() { return usedCount; }
}
