package com.shop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tài khoản người dùng. Bảng tên "users" vì "user" là từ khóa của PostgreSQL.
 * Tạm thời hệ thống chỉ có 1 khách mẫu (xem DataSeeder, DemoCurrentUserService).
 */
@Entity
@Table(name = "users")
public class User {

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Sẽ lưu mật khẩu đã băm BCrypt khi làm đăng nhập. */
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 20)
    private String role = ROLE_CUSTOMER;

    private int loyaltyPoints;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected User() { }

    public User(String email, String fullName, String phone, String address) {
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    /** Dùng cho LoyaltyListener (mẫu Observer) ở bước đặt hàng. */
    public void addPoints(int points) {
        this.loyaltyPoints += points;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(String role) { this.role = role; }
}
