package com.ecommerce.wishlistservice.exception;

public class WishlistNotFoundException extends RuntimeException {
    
    public WishlistNotFoundException(String message) {
        super(message);
    }
    
    public WishlistNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 