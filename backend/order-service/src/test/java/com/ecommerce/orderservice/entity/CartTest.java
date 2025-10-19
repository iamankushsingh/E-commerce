package com.ecommerce.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(cart);
        assertNotNull(cart.getCartItems());
    }

    @Test
    void testParameterizedConstructor() {
        Cart newCart = new Cart(1L);

        assertEquals(1L, newCart.getUserId());
        assertEquals(BigDecimal.ZERO, newCart.getTotalAmount());
    }

    @Test
    void testAddCartItem() {
        CartItem item = new CartItem(1L, "Product", new BigDecimal("50.00"), 2, "url");
        cart.addCartItem(item);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(cart, item.getCart());
        assertEquals(new BigDecimal("100.00"), cart.getTotalAmount());
    }

    @Test
    void testRemoveCartItem() {
        CartItem item = new CartItem(1L, "Product", new BigDecimal("50.00"), 2, "url");
        cart.addCartItem(item);
        cart.removeCartItem(item);

        assertEquals(0, cart.getCartItems().size());
        assertNull(item.getCart());
    }

    @Test
    void testClearCart() {
        CartItem item1 = new CartItem(1L, "Product 1", new BigDecimal("50.00"), 2, "url");
        CartItem item2 = new CartItem(2L, "Product 2", new BigDecimal("30.00"), 1, "url");
        cart.addCartItem(item1);
        cart.addCartItem(item2);

        cart.clearCart();

        assertEquals(0, cart.getCartItems().size());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
    }

    @Test
    void testGetTotalItems() {
        CartItem item1 = new CartItem(1L, "Product 1", new BigDecimal("50.00"), 2, "url");
        CartItem item2 = new CartItem(2L, "Product 2", new BigDecimal("30.00"), 3, "url");
        cart.addCartItem(item1);
        cart.addCartItem(item2);

        assertEquals(5, cart.getTotalItems());
    }

    @Test
    void testCalculateTotalAmount() {
        CartItem item1 = new CartItem(1L, "Product 1", new BigDecimal("50.00"), 2, "url");
        CartItem item2 = new CartItem(2L, "Product 2", new BigDecimal("30.00"), 1, "url");
        cart.getCartItems().add(item1);
        cart.getCartItems().add(item2);

        cart.calculateTotalAmount();

        assertEquals(new BigDecimal("130.00"), cart.getTotalAmount());
    }

    @Test
    void testOnCreate() {
        cart.onCreate();

        assertNotNull(cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        cart.onUpdate();

        assertNotNull(cart.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        cart.setId(100L);
        cart.setUserId(200L);
        cart.setTotalAmount(new BigDecimal("250.00"));
        
        assertEquals(100L, cart.getId());
        assertEquals(200L, cart.getUserId());
        assertEquals(new BigDecimal("250.00"), cart.getTotalAmount());
    }

    @Test
    void testCalculateTotalAmountWithEmptyCart() {
        cart.calculateTotalAmount();
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
    }

    @Test
    void testGetTotalItemsWithEmptyCart() {
        assertEquals(0, cart.getTotalItems());
    }

    @Test
    void testAddMultipleCartItems() {
        CartItem item1 = new CartItem(1L, "Product 1", new BigDecimal("50.00"), 2, "url1");
        CartItem item2 = new CartItem(2L, "Product 2", new BigDecimal("30.00"), 3, "url2");
        CartItem item3 = new CartItem(3L, "Product 3", new BigDecimal("20.00"), 1, "url3");
        
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cart.addCartItem(item3);

        assertEquals(3, cart.getCartItems().size());
        assertEquals(new BigDecimal("210.00"), cart.getTotalAmount());
        assertEquals(6, cart.getTotalItems());
    }
}

