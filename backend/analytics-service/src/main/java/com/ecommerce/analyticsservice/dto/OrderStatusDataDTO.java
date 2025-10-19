package com.ecommerce.analyticsservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderStatusDataDTO {
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("count")
    private Integer count;
    
    @JsonProperty("percentage")
    private Double percentage;

    // Default constructor
    public OrderStatusDataDTO() {}

    // Constructor
    public OrderStatusDataDTO(String status, Integer count, Double percentage) {
        this.status = status;
        this.count = count;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
} 