package com.ecommerce.analyticsservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class RevenueDataDTO {
    @JsonProperty("month")
    private String month;
    
    @JsonProperty("revenue")
    private BigDecimal revenue;
    
    @JsonProperty("orders")
    private Integer orders;

    // Default constructor
    public RevenueDataDTO() {}

    // Constructor
    public RevenueDataDTO(String month, BigDecimal revenue, Integer orders) {
        this.month = month;
        this.revenue = revenue;
        this.orders = orders;
    }

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
} 