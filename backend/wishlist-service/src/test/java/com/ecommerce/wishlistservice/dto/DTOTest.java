package com.ecommerce.wishlistservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testWishlistCollectionDTO() {
        WishlistCollectionDTO dto = new WishlistCollectionDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setName("My Wishlist");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        WishlistItemDTO itemDTO = new WishlistItemDTO();
        dto.setItems(Arrays.asList(itemDTO));

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getUserId());
        assertEquals("My Wishlist", dto.getName());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertEquals(1, dto.getItems().size());
    }

    @Test
    void testWishlistItemDTO() {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(1L);
        dto.setProductId(100L);
        dto.setProductName("Test Product");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setCategory("Electronics");
        dto.setImageUrl("http://image.url");
        dto.setAddedAt(LocalDateTime.now());

        assertEquals(1L, dto.getId());
        assertEquals(100L, dto.getProductId());
        assertEquals("Test Product", dto.getProductName());
        assertEquals(new BigDecimal("99.99"), dto.getPrice());
        assertEquals("Electronics", dto.getCategory());
        assertEquals("http://image.url", dto.getImageUrl());
        assertNotNull(dto.getAddedAt());
    }

    @Test
    void testCreateCollectionDTO() {
        CreateCollectionDTO dto = new CreateCollectionDTO();
        dto.setName("New Collection");
        dto.setUserId(1L);

        assertEquals("New Collection", dto.getName());
        assertEquals(1L, dto.getUserId());
    }

    @Test
    void testAddItemToCollectionDTO() {
        AddItemToCollectionDTO dto = new AddItemToCollectionDTO();
        dto.setProductId(100L);
        dto.setProductName("Test Product");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setCategory("Electronics");
        dto.setImageUrl("http://image.url");

        assertEquals(100L, dto.getProductId());
        assertEquals("Test Product", dto.getProductName());
        assertEquals(new BigDecimal("99.99"), dto.getPrice());
    }

    @Test
    void testWishlistStatsDTO() {
        WishlistStatsDTO dto = new WishlistStatsDTO();
        dto.setUserId(1L);
        dto.setTotalCollections(5L);
        dto.setTotalItems(25L);

        assertEquals(1L, dto.getUserId());
        assertEquals(5L, dto.getTotalCollections());
        assertEquals(25L, dto.getTotalItems());
    }
}

