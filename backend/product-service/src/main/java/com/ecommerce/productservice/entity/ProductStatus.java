package com.ecommerce.productservice.entity;

public enum ProductStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProductStatus fromValue(String value) {
        for (ProductStatus status : ProductStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid product status: " + value);
    }
} 