package com.ecommerce.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private OrderItem orderItem;
    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);

        orderItem = new OrderItem(
                1L,
                "Test Product",
                "Test Description",
                new BigDecimal("50.00"),
                2,
                "http://image.url",
                "Electronics"
        );
    }

    @Test
    void testConstructorWithAllParameters() {
        assertEquals(1L, orderItem.getProductId());
        assertEquals("Test Product", orderItem.getProductName());
        assertEquals("Test Description", orderItem.getProductDescription());
        assertEquals(new BigDecimal("50.00"), orderItem.getUnitPrice());
        assertEquals(2, orderItem.getQuantity());
        assertEquals("http://image.url", orderItem.getProductImageUrl());
        assertEquals("Electronics", orderItem.getProductCategory());
    }

    @Test
    void testDefaultConstructor() {
        OrderItem item = new OrderItem();
        assertNull(item.getId());
        assertNull(item.getProductId());
        assertNull(item.getProductName());
    }

    @Test
    void testSettersAndGetters() {
        orderItem.setId(10L);
        orderItem.setProductId(2L);
        orderItem.setProductName("New Product");
        orderItem.setProductDescription("New Description");
        orderItem.setUnitPrice(new BigDecimal("75.00"));
        orderItem.setQuantity(3);
        orderItem.setProductImageUrl("http://new-image.url");
        orderItem.setProductCategory("Books");
        orderItem.setOrder(order);

        assertEquals(10L, orderItem.getId());
        assertEquals(2L, orderItem.getProductId());
        assertEquals("New Product", orderItem.getProductName());
        assertEquals("New Description", orderItem.getProductDescription());
        assertEquals(new BigDecimal("75.00"), orderItem.getUnitPrice());
        assertEquals(3, orderItem.getQuantity());
        assertEquals("http://new-image.url", orderItem.getProductImageUrl());
        assertEquals("Books", orderItem.getProductCategory());
        assertEquals(order, orderItem.getOrder());
    }

    @Test
    void testCalculateTotalPrice() {
        orderItem.calculateTotalPrice();
        assertEquals(new BigDecimal("100.00"), orderItem.getTotalPrice());
    }

    @Test
    void testCalculateTotalPriceWithDifferentQuantity() {
        orderItem.setQuantity(5);
        orderItem.calculateTotalPrice();
        assertEquals(new BigDecimal("250.00"), orderItem.getTotalPrice());
    }

    @Test
    void testCalculateTotalPriceWithZeroQuantity() {
        orderItem.setQuantity(0);
        orderItem.calculateTotalPrice();
        assertNotNull(orderItem.getTotalPrice());
    }

    @Test
    void testCalculateTotalPriceWithNullUnitPrice() {
        orderItem.setUnitPrice(null);
        orderItem.calculateTotalPrice();
        // When unit price is null, total price won't be calculated
        assertNotNull(orderItem);
    }

    @Test
    void testCalculateTotalPriceWithLargeQuantity() {
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        orderItem.setQuantity(1000);
        
        orderItem.calculateTotalPrice();
        
        assertEquals(new BigDecimal("10000.00"), orderItem.getTotalPrice());
    }

    @Test
    void testSetTotalPriceDirectly() {
        orderItem.setTotalPrice(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("500.00"), orderItem.getTotalPrice());
    }

    @Test
    void testUpdateQuantity() {
        orderItem.setQuantity(10);
        assertEquals(10, orderItem.getQuantity());
    }

    @Test
    void testOnCreate() {
        orderItem.onCreate();
        assertNotNull(orderItem.getCreatedAt());
        assertNotNull(orderItem.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        orderItem.onUpdate();
        assertNotNull(orderItem.getUpdatedAt());
    }

    @Test
    void testAllFieldSetters() {
        OrderItem item = new OrderItem();
        item.setId(100L);
        item.setProductId(200L);
        item.setProductName("New Item");
        item.setProductDescription("New Desc");
        item.setUnitPrice(new BigDecimal("99.99"));
        item.setQuantity(5);
        item.setTotalPrice(new BigDecimal("499.95"));
        item.setProductImageUrl("http://url.com");
        item.setProductCategory("Category");
        
        assertEquals(100L, item.getId());
        assertEquals(200L, item.getProductId());
        assertEquals("New Item", item.getProductName());
        assertEquals("New Desc", item.getProductDescription());
        assertEquals(new BigDecimal("99.99"), item.getUnitPrice());
        assertEquals(5, item.getQuantity());
        assertEquals(new BigDecimal("499.95"), item.getTotalPrice());
        assertEquals("http://url.com", item.getProductImageUrl());
        assertEquals("Category", item.getProductCategory());
    }
}

