package com.ecommerce.wishlistservice.controller;

import com.ecommerce.wishlistservice.dto.*;
import com.ecommerce.wishlistservice.exception.DuplicateItemException;
import com.ecommerce.wishlistservice.exception.WishlistNotFoundException;
import com.ecommerce.wishlistservice.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WishlistService wishlistService;

    private WishlistCollectionDTO testCollectionDTO;
    private WishlistItemDTO testItemDTO;
    private CreateCollectionDTO createCollectionDTO;
    private AddItemToCollectionDTO addItemDTO;
    private WishlistStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        testCollectionDTO = new WishlistCollectionDTO();
        testCollectionDTO.setId(1L);
        testCollectionDTO.setName("My Wishlist");
        testCollectionDTO.setUserId(1L);

        testItemDTO = new WishlistItemDTO();
        testItemDTO.setId(1L);
        testItemDTO.setProductId(1L);
        testItemDTO.setProductName("Test Product");
        testItemDTO.setPrice(new BigDecimal("99.99"));

        createCollectionDTO = new CreateCollectionDTO();
        createCollectionDTO.setName("New Wishlist");
        createCollectionDTO.setUserId(1L);

        addItemDTO = new AddItemToCollectionDTO();
        addItemDTO.setProductId(1L);
        addItemDTO.setProductName("Test Product");
        addItemDTO.setPrice(new BigDecimal("99.99"));

        statsDTO = new WishlistStatsDTO();
        statsDTO.setUserId(1L);
        statsDTO.setTotalCollections(5L);
        statsDTO.setTotalItems(20L);
    }

    @Test
    void testGetUserCollections_Success() throws Exception {
        when(wishlistService.getUserCollections(1L)).thenReturn(Arrays.asList(testCollectionDTO));

        mockMvc.perform(get("/api/wishlist/user/1/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("My Wishlist"));

        verify(wishlistService).getUserCollections(1L);
    }

    @Test
    void testCreateCollection_Success() throws Exception {
        when(wishlistService.createCollection(any(CreateCollectionDTO.class))).thenReturn(testCollectionDTO);

        mockMvc.perform(post("/api/wishlist/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCollectionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Wishlist"));

        verify(wishlistService).createCollection(any(CreateCollectionDTO.class));
    }

    @Test
    void testCreateCollection_DuplicateName() throws Exception {
        when(wishlistService.createCollection(any(CreateCollectionDTO.class)))
                .thenThrow(new DuplicateItemException("Collection already exists"));

        mockMvc.perform(post("/api/wishlist/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCollectionDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetCollection_Success() throws Exception {
        when(wishlistService.getCollection(1L, 1L)).thenReturn(testCollectionDTO);

        mockMvc.perform(get("/api/wishlist/collections/1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(wishlistService).getCollection(1L, 1L);
    }

    @Test
    void testGetCollection_NotFound() throws Exception {
        when(wishlistService.getCollection(1L, 1L))
                .thenThrow(new WishlistNotFoundException("Collection not found"));

        mockMvc.perform(get("/api/wishlist/collections/1/user/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateCollection_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Updated Name");

        when(wishlistService.updateCollection(anyLong(), anyLong(), anyString())).thenReturn(testCollectionDTO);

        mockMvc.perform(put("/api/wishlist/collections/1/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(wishlistService).updateCollection(anyLong(), anyLong(), anyString());
    }

    @Test
    void testDeleteCollection_Success() throws Exception {
        doNothing().when(wishlistService).deleteCollection(1L, 1L);

        mockMvc.perform(delete("/api/wishlist/collections/1/user/1"))
                .andExpect(status().isNoContent());

        verify(wishlistService).deleteCollection(1L, 1L);
    }

    @Test
    void testAddItemToCollection_Success() throws Exception {
        when(wishlistService.addItemToCollection(anyLong(), anyLong(), any(AddItemToCollectionDTO.class)))
                .thenReturn(testItemDTO);

        mockMvc.perform(post("/api/wishlist/collections/1/user/1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Test Product"));

        verify(wishlistService).addItemToCollection(anyLong(), anyLong(), any(AddItemToCollectionDTO.class));
    }

    @Test
    void testRemoveItemFromCollection_Success() throws Exception {
        doNothing().when(wishlistService).removeItemFromCollection(1L, 1L, 1L);

        mockMvc.perform(delete("/api/wishlist/collections/1/user/1/items/1"))
                .andExpect(status().isNoContent());

        verify(wishlistService).removeItemFromCollection(1L, 1L, 1L);
    }

    @Test
    void testGetAllUserItems_Success() throws Exception {
        when(wishlistService.getAllUserItems(1L)).thenReturn(Arrays.asList(testItemDTO));

        mockMvc.perform(get("/api/wishlist/user/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productName").value("Test Product"));

        verify(wishlistService).getAllUserItems(1L);
    }

    @Test
    void testIsProductInWishlist_True() throws Exception {
        when(wishlistService.isProductInWishlist(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/wishlist/user/1/product/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        verify(wishlistService).isProductInWishlist(1L, 1L);
    }

    @Test
    void testIsProductInWishlist_False() throws Exception {
        when(wishlistService.isProductInWishlist(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/wishlist/user/1/product/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));

        verify(wishlistService).isProductInWishlist(1L, 1L);
    }

    @Test
    void testGetUserWishlistStats_Success() throws Exception {
        when(wishlistService.getUserWishlistStats(1L)).thenReturn(statsDTO);

        mockMvc.perform(get("/api/wishlist/user/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalCollections").value(5))
                .andExpect(jsonPath("$.totalItems").value(20));

        verify(wishlistService).getUserWishlistStats(1L);
    }
}

