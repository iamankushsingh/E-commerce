package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.*;

public class UpdateCartItemDTO {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100 items")
    private Integer quantity;

    // Constructors
    public UpdateCartItemDTO() {}

    public UpdateCartItemDTO(Integer quantity) {
        this.quantity = quantity;
    }

    // Getters and Setters
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "UpdateCartItemDTO{" +
                "quantity=" + quantity +
                '}';
    }
} 