package com.ecommerce.wishlistservice.dto;

public class WishlistStatsDTO {
    
    private Long userId;
    private long totalCollections;
    private long totalItems;
    
    // Constructors
    public WishlistStatsDTO() {}
    
    public WishlistStatsDTO(Long userId, long totalCollections, long totalItems) {
        this.userId = userId;
        this.totalCollections = totalCollections;
        this.totalItems = totalItems;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public long getTotalCollections() {
        return totalCollections;
    }
    
    public void setTotalCollections(long totalCollections) {
        this.totalCollections = totalCollections;
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }
} 