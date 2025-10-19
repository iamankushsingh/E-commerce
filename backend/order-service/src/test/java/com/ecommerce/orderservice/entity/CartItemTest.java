package com.ecommerce.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private CartItem cartItem;
    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);

        cartItem = new CartItem();
        cartItem.setProductId(1L);
        cartItem.setProductName("Test Product");
        cartItem.setUnitPrice(new BigDecimal("50.00"));
        cartItem.setQuantity(2);
        cartItem.setProductImageUrl("http://image.url");
        cartItem.setCart(cart);
    }

    @Test
    void testSettersAndGetters() {
        assertEquals(1L, cartItem.getProductId());
        assertEquals("Test Product", cartItem.getProductName());
        assertEquals(new BigDecimal("50.00"), cartItem.getUnitPrice());
        assertEquals(2, cartItem.getQuantity());
        assertEquals("http://image.url", cartItem.getProductImageUrl());
        assertEquals(cart, cartItem.getCart());
    }

    @Test
    void testSetId() {
        cartItem.setId(10L);
        assertEquals(10L, cartItem.getId());
    }

    @Test
    void testCalculateTotalPrice() {
        cartItem.calculateTotalPrice();
        assertEquals(new BigDecimal("100.00"), cartItem.getTotalPrice());
    }

    @Test
    void testCalculateTotalPriceWithDifferentQuantity() {
        cartItem.setQuantity(5);
        cartItem.calculateTotalPrice();
        assertEquals(new BigDecimal("250.00"), cartItem.getTotalPrice());
    }

    @Test
    void testCalculateTotalPriceWithZeroQuantity() {
        cartItem.setQuantity(0);
        cartItem.calculateTotalPrice();
        assertNotNull(cartItem.getTotalPrice());
    }

    @Test
    void testCalculateTotalPriceWithNullUnitPrice() {
        cartItem.setUnitPrice(null);
        cartItem.calculateTotalPrice();
        // When unit price is null, total price won't be calculated
        assertNotNull(cartItem);
    }

    @Test
    void testUpdateQuantity() {
        cartItem.setQuantity(10);
        assertEquals(10, cartItem.getQuantity());
    }

    @Test
    void testConstructorWithAllParameters() {
        CartItem newItem = new CartItem(100L, "Product Name", new BigDecimal("50.00"), 3, "http://image.url");
        
        assertEquals(100L, newItem.getProductId());
        assertEquals("Product Name", newItem.getProductName());
        assertEquals(new BigDecimal("50.00"), newItem.getUnitPrice());
        assertEquals(3, newItem.getQuantity());
        assertEquals("http://image.url", newItem.getProductImageUrl());
    }

    @Test
    void testCalculateTotalPriceWithLargeQuantity() {
        cartItem.setUnitPrice(new BigDecimal("10.00"));
        cartItem.setQuantity(1000);
        
        cartItem.calculateTotalPrice();
        
        assertEquals(new BigDecimal("10000.00"), cartItem.getTotalPrice());
    }

    @Test
    void testSetTotalPriceDirectly() {
        cartItem.setTotalPrice(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("500.00"), cartItem.getTotalPrice());
    }

    @Test
    void testOnCreate() {
        cartItem.onCreate();
        assertNotNull(cartItem.getCreatedAt());
        assertNotNull(cartItem.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        cartItem.onUpdate();
        assertNotNull(cartItem.getUpdatedAt());
    }
}

