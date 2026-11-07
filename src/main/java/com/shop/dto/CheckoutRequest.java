package com.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu form thanh toán (DTO): chỉ chứa dữ liệu khách nhập, không chứa logic. */
public class CheckoutRequest {

    @NotBlank(message = "Vui lòng nhập họ tên người nhận")
    @Size(max = 100)
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
    private String phone;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String address;

    @NotBlank
    private String shippingMethod = "STANDARD";

    @NotBlank
    private String paymentMethod = "COD";

    private String voucherCode;

    @Size(max = 500)
    private String note;

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
