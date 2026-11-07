package com.shop.dto;

import com.shop.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Form thêm/sửa sản phẩm của quản trị viên. */
public class ProductForm {

    private Long id;

    @NotBlank(message = "Vui lòng nhập tên sản phẩm")
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull(message = "Vui lòng nhập giá")
    @DecimalMin(value = "1000", message = "Giá tối thiểu 1.000đ")
    private BigDecimal price;

    @NotNull(message = "Vui lòng nhập tồn kho")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stock = 0;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private String imageUrl;

    private boolean bestSeller;

    private boolean active = true;

    public static ProductForm from(Product p) {
        ProductForm f = new ProductForm();
        f.id = p.getId();
        f.name = p.getName();
        f.description = p.getDescription();
        f.price = p.getPrice();
        f.stock = p.getStock();
        f.categoryId = p.getCategory() == null ? null : p.getCategory().getId();
        f.imageUrl = p.getImageUrl();
        f.bestSeller = p.isBestSeller();
        f.active = p.isActive();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isBestSeller() { return bestSeller; }
    public void setBestSeller(boolean bestSeller) { this.bestSeller = bestSeller; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
