package com.shop.dto;

import com.shop.entity.Voucher;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Form thêm/sửa voucher của quản trị viên. */
public class VoucherForm {

    private Long id;

    @NotBlank(message = "Vui lòng nhập mã")
    @Pattern(regexp = "^[A-Za-z0-9]{3,30}$", message = "Mã gồm 3–30 chữ cái hoặc chữ số, không dấu, không khoảng trắng")
    private String code;

    @NotBlank
    private String type = Voucher.PERCENT;

    @NotNull(message = "Vui lòng nhập giá trị")
    @DecimalMin(value = "1", message = "Giá trị phải lớn hơn 0")
    private BigDecimal value;

    @NotNull(message = "Vui lòng nhập đơn tối thiểu")
    @DecimalMin(value = "0", message = "Đơn tối thiểu không được âm")
    private BigDecimal minOrder = BigDecimal.ZERO;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresAt;

    @NotNull
    @Min(value = 0, message = "Số lượt không được âm (0 = không giới hạn)")
    private Integer usageLimit = 0;

    public static VoucherForm from(Voucher v) {
        VoucherForm f = new VoucherForm();
        f.id = v.getId();
        f.code = v.getCode();
        f.type = v.getType();
        f.value = v.getValue();
        f.minOrder = v.getMinOrder();
        f.expiresAt = v.getExpiresAt();
        f.usageLimit = v.getUsageLimit();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public BigDecimal getMinOrder() { return minOrder; }
    public void setMinOrder(BigDecimal minOrder) { this.minOrder = minOrder; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
}
