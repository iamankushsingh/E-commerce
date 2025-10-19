package com.ecommerce.wishlistservice.exception;

public class DuplicateItemException extends RuntimeException {
    
    public DuplicateItemException(String message) {
        super(message);
    }
    
    public DuplicateItemException(String message, Throwable cause) {
        super(message, cause);
    }
} 