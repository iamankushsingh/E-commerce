package com.ecommerce.wishlistservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCollectionDTO {

    @NotBlank(message = "Collection name is required")
    @Size(max = 255, message = "Collection name must not exceed 255 characters")
    private String name;

    @NotNull(message = "User ID is required")
    private Long userId;

    // Constructors
    public CreateCollectionDTO() {}

    public CreateCollectionDTO(String name, Long userId) {
        this.name = name;
        this.userId = userId;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
} 