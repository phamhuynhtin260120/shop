package com.shop.entity;

import jakarta.persistence.*;

/** Danh mục sản phẩm (Áo, Quần, Váy đầm, Phụ kiện). */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Chuỗi không dấu dùng trên URL, ví dụ /products?category=vay-dam */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    protected Category() { }   // JPA bắt buộc có constructor rỗng

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
}
