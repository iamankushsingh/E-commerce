package com.ecommerce.wishlistservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WishlistCollectionTest {

    private WishlistCollection collection;

    @BeforeEach
    void setUp() {
        collection = new WishlistCollection();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(collection);
    }

    @Test
    void testParameterizedConstructor() {
        WishlistCollection newCollection = new WishlistCollection("My Wishlist", 1L);

        assertEquals("My Wishlist", newCollection.getName());
        assertEquals(1L, newCollection.getUserId());
        assertNotNull(newCollection.getCreatedAt());
        assertNotNull(newCollection.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        collection.setId(1L);
        collection.setName("Test Collection");
        collection.setUserId(1L);

        LocalDateTime now = LocalDateTime.now();
        collection.setCreatedAt(now);
        collection.setUpdatedAt(now);

        assertEquals(1L, collection.getId());
        assertEquals("Test Collection", collection.getName());
        assertEquals(1L, collection.getUserId());
        assertEquals(now, collection.getCreatedAt());
        assertEquals(now, collection.getUpdatedAt());
    }

    @Test
    void testAddItem() {
        WishlistItem item = new WishlistItem(1L, "Product", new BigDecimal("99.99"), "Category", "url");

        collection.addItem(item);

        assertEquals(1, collection.getItems().size());
        assertEquals(collection, item.getCollection());
    }

    @Test
    void testRemoveItem() {
        WishlistItem item = new WishlistItem(1L, "Product", new BigDecimal("99.99"), "Category", "url");
        collection.addItem(item);

        collection.removeItem(item);

        assertEquals(0, collection.getItems().size());
        assertNull(item.getCollection());
    }

    @Test
    void testPrePersist() {
        WishlistCollection newCollection = new WishlistCollection();
        newCollection.prePersist();

        assertNotNull(newCollection.getCreatedAt());
        assertNotNull(newCollection.getUpdatedAt());
    }

    @Test
    void testPreUpdate() {
        WishlistCollection existingCollection = new WishlistCollection();
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        existingCollection.setCreatedAt(originalCreatedAt);
        existingCollection.setUpdatedAt(originalCreatedAt);

        existingCollection.preUpdate();

        assertEquals(originalCreatedAt, existingCollection.getCreatedAt());
        assertNotEquals(originalCreatedAt, existingCollection.getUpdatedAt());
    }

    @Test
    void testItemsList() {
        assertNotNull(collection.getItems());
        assertEquals(0, collection.getItems().size());
    }
}

