package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Get or create cart for user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long userId) {
        try {
            logger.info("Getting cart for user: {}", userId);
            
            CartDTO cart = cartService.getOrCreateCartForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart retrieved successfully");
            response.put("cart", cart);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting cart for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<Map<String, Object>> addItemToCart(@PathVariable Long userId, 
                                                           @Valid @RequestBody AddToCartDTO addToCartDTO) {
        try {
            logger.info("Adding item to cart for user {}: {}", userId, addToCartDTO);
            
            CartDTO updatedCart = cartService.addItemToCart(userId, addToCartDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item added to cart successfully");
            response.put("cart", updatedCart);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error adding item to cart for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<Map<String, Object>> updateCartItemQuantity(@PathVariable Long userId,
                                                                    @PathVariable Long cartItemId,
                                                                    @Valid @RequestBody UpdateCartItemDTO updateDTO) {
        try {
            logger.info("Updating cart item {} for user {}: {}", cartItemId, userId, updateDTO);
            
            CartDTO updatedCart = cartService.updateCartItemQuantity(userId, cartItemId, updateDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart item updated successfully");
            response.put("cart", updatedCart);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating cart item {} for user {}: {}", cartItemId, userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<Map<String, Object>> removeItemFromCart(@PathVariable Long userId,
                                                                @PathVariable Long cartItemId) {
        try {
            logger.info("Removing cart item {} for user {}", cartItemId, userId);
            
            CartDTO updatedCart = cartService.removeItemFromCart(userId, cartItemId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item removed from cart successfully");
            response.put("cart", updatedCart);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error removing cart item {} for user {}: {}", cartItemId, userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> clearCart(@PathVariable Long userId) {
        try {
            logger.info("Clearing cart for user: {}", userId);
            
            cartService.clearCart(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing cart for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get cart items count
     */
    @GetMapping("/{userId}/count")
    public ResponseEntity<Map<String, Object>> getCartItemsCount(@PathVariable Long userId) {
        try {
            logger.info("Getting cart items count for user: {}", userId);
            
            Integer itemsCount = cartService.getCartItemsCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart items count retrieved successfully");
            response.put("itemsCount", itemsCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting cart items count for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Test endpoint for cart functionality
     */
    @GetMapping("/test/{userId}")
    public ResponseEntity<Map<String, Object>> testCart(@PathVariable Long userId) {
        try {
            logger.info("Testing cart functionality for user: {}", userId);
            
            // Test database connection by getting/creating cart
            CartDTO cart = cartService.getOrCreateCartForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart test successful - database connection working");
            response.put("cart", cart);
            response.put("cartItemsCount", cart.getCartItems().size());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Cart test failed for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Cart test failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Validate cart for checkout
     */
    @GetMapping("/{userId}/validate")
    public ResponseEntity<Map<String, Object>> validateCart(@PathVariable Long userId) {
        try {
            logger.info("Validating cart for user: {}", userId);
            
            boolean isValid = cartService.validateCartForCheckout(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isValid ? "Cart is valid for checkout" : "Cart validation failed");
            response.put("isValid", isValid);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating cart for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("isValid", false);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 