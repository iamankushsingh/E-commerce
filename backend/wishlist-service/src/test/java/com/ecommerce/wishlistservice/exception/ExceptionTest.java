package com.ecommerce.wishlistservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testDuplicateItemException_WithMessage() {
        String message = "Duplicate item found";
        DuplicateItemException exception = new DuplicateItemException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testDuplicateItemException_WithMessageAndCause() {
        String message = "Duplicate item found";
        Throwable cause = new RuntimeException("Cause");
        DuplicateItemException exception = new DuplicateItemException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testWishlistNotFoundException_WithMessage() {
        String message = "Wishlist not found";
        WishlistNotFoundException exception = new WishlistNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testWishlistNotFoundException_WithMessageAndCause() {
        String message = "Wishlist not found";
        Throwable cause = new RuntimeException("Cause");
        WishlistNotFoundException exception = new WishlistNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

