package com.ecommerce.wishlistservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WishlistItemTest {

    private WishlistItem item;

    @BeforeEach
    void setUp() {
        item = new WishlistItem();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(item);
    }

    @Test
    void testParameterizedConstructor() {
        WishlistItem newItem = new WishlistItem(1L, "Test Product", new BigDecimal("99.99"), "Electronics", "http://example.com/image.jpg");

        assertEquals(1L, newItem.getProductId());
        assertEquals("Test Product", newItem.getProductName());
        assertEquals(new BigDecimal("99.99"), newItem.getPrice());
        assertEquals("Electronics", newItem.getCategory());
        assertEquals("http://example.com/image.jpg", newItem.getImageUrl());
        assertNotNull(newItem.getAddedAt());
    }

    @Test
    void testGettersAndSetters() {
        WishlistCollection collection = new WishlistCollection("Test", 1L);

        item.setId(1L);
        item.setProductId(1L);
        item.setProductName("Test Product");
        item.setPrice(new BigDecimal("99.99"));
        item.setCategory("Electronics");
        item.setImageUrl("http://example.com/image.jpg");
        item.setCollection(collection);

        LocalDateTime now = LocalDateTime.now();
        item.setAddedAt(now);

        assertEquals(1L, item.getId());
        assertEquals(1L, item.getProductId());
        assertEquals("Test Product", item.getProductName());
        assertEquals(new BigDecimal("99.99"), item.getPrice());
        assertEquals("Electronics", item.getCategory());
        assertEquals("http://example.com/image.jpg", item.getImageUrl());
        assertEquals(collection, item.getCollection());
        assertEquals(now, item.getAddedAt());
    }

    @Test
    void testPrePersist() {
        WishlistItem newItem = new WishlistItem();
        newItem.prePersist();

        assertNotNull(newItem.getAddedAt());
    }

    @Test
    void testPriceValidation() {
        item.setPrice(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("0.01"), item.getPrice());

        item.setPrice(new BigDecimal("999999.99"));
        assertEquals(new BigDecimal("999999.99"), item.getPrice());
    }
}

