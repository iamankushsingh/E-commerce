package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateOrderDTO {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 1000, message = "Shipping address must not exceed 1000 characters")
    private String shippingAddress;

    @Size(max = 1000, message = "Billing address must not exceed 1000 characters")
    private String billingAddress;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @NotBlank(message = "Payment method is required")
    @Size(max = 100, message = "Payment method must not exceed 100 characters")
    private String paymentMethod;

    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Shipping amount cannot be negative")
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Size(max = 50, message = "Coupon code must not exceed 50 characters")
    private String couponCode;

    // Constructors
    public CreateOrderDTO() {}

    public CreateOrderDTO(String shippingAddress, String paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    @Override
    public String toString() {
        return "CreateOrderDTO{" +
                "shippingAddress='" + shippingAddress + '\'' +
                ", billingAddress='" + billingAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", taxAmount=" + taxAmount +
                ", shippingAmount=" + shippingAmount +
                ", discountAmount=" + discountAmount +
                ", couponCode='" + couponCode + '\'' +
                '}';
    }
} 