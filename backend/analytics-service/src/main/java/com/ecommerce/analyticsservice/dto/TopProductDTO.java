package com.ecommerce.analyticsservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TopProductDTO {
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("totalSales")
    private BigDecimal totalSales;
    
    @JsonProperty("unitsSold")
    private Integer unitsSold;

    // Default constructor
    public TopProductDTO() {}

    // Constructor
    public TopProductDTO(String productId, String productName, BigDecimal totalSales, Integer unitsSold) {
        this.productId = productId;
        this.productName = productName;
        this.totalSales = totalSales;
        this.unitsSold = unitsSold;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Integer getUnitsSold() {
        return unitsSold;
    }

    public void setUnitsSold(Integer unitsSold) {
        this.unitsSold = unitsSold;
    }
} 