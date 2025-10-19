package com.ecommerce.analyticsservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class SalesReportDTO {
    @JsonProperty("totalRevenue")
    private BigDecimal totalRevenue;
    
    @JsonProperty("totalOrders")
    private Long totalOrders;
    
    @JsonProperty("averageOrderValue") 
    private BigDecimal averageOrderValue;
    
    @JsonProperty("topProducts")
    private List<TopProductDTO> topProducts;
    
    @JsonProperty("revenueByMonth")
    private List<RevenueDataDTO> revenueByMonth;
    
    @JsonProperty("ordersByStatus")
    private List<OrderStatusDataDTO> ordersByStatus;

    // Default constructor
    public SalesReportDTO() {}

    // Constructor
    public SalesReportDTO(BigDecimal totalRevenue, Long totalOrders, BigDecimal averageOrderValue,
                         List<TopProductDTO> topProducts, List<RevenueDataDTO> revenueByMonth,
                         List<OrderStatusDataDTO> ordersByStatus) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.averageOrderValue = averageOrderValue;
        this.topProducts = topProducts;
        this.revenueByMonth = revenueByMonth;
        this.ordersByStatus = ordersByStatus;
    }

    // Getters and Setters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public List<TopProductDTO> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDTO> topProducts) {
        this.topProducts = topProducts;
    }

    public List<RevenueDataDTO> getRevenueByMonth() {
        return revenueByMonth;
    }

    public void setRevenueByMonth(List<RevenueDataDTO> revenueByMonth) {
        this.revenueByMonth = revenueByMonth;
    }

    public List<OrderStatusDataDTO> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(List<OrderStatusDataDTO> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }
} 