package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.service.CartService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartDTO testCartDTO;
    private AddToCartDTO addToCartDTO;
    private UpdateCartItemDTO updateCartItemDTO;

    @BeforeEach
    void setUp() {
        testCartDTO = new CartDTO();
        testCartDTO.setId(1L);
        testCartDTO.setUserId(1L);
        testCartDTO.setTotalAmount(new BigDecimal("100.00"));
        testCartDTO.setTotalItems(2);
        
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(1L);
        cartItemDTO.setProductId(1L);
        cartItemDTO.setProductName("Product 1");
        cartItemDTO.setQuantity(2);
        cartItemDTO.setUnitPrice(new BigDecimal("50.00"));
        cartItemDTO.setTotalPrice(new BigDecimal("100.00"));
        testCartDTO.setCartItems(Arrays.asList(cartItemDTO));

        addToCartDTO = new AddToCartDTO();
        addToCartDTO.setProductId(1L);
        addToCartDTO.setQuantity(2);

        updateCartItemDTO = new UpdateCartItemDTO();
        updateCartItemDTO.setQuantity(3);
    }

    @Test
    void testGetCart() throws Exception {
        when(cartService.getOrCreateCartForUser(anyLong())).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart retrieved successfully"))
                .andExpect(jsonPath("$.cart.id").value(1))
                .andExpect(jsonPath("$.cart.totalItems").value(2));

        verify(cartService).getOrCreateCartForUser(1L);
    }

    @Test
    void testAddToCart() throws Exception {
        when(cartService.addItemToCart(anyLong(), any(AddToCartDTO.class))).thenReturn(testCartDTO);

        mockMvc.perform(post("/api/cart/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item added to cart successfully"))
                .andExpect(jsonPath("$.cart.totalItems").value(2));

        verify(cartService).addItemToCart(eq(1L), any(AddToCartDTO.class));
    }

    @Test
    void testUpdateCartItem() throws Exception {
        when(cartService.updateCartItemQuantity(anyLong(), anyLong(), any(UpdateCartItemDTO.class)))
                .thenReturn(testCartDTO);

        mockMvc.perform(put("/api/cart/1/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart item updated successfully"))
                .andExpect(jsonPath("$.cart.totalItems").value(2));

        verify(cartService).updateCartItemQuantity(eq(1L), eq(1L), any(UpdateCartItemDTO.class));
    }

    @Test
    void testRemoveFromCart() throws Exception {
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(testCartDTO);

        mockMvc.perform(delete("/api/cart/1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item removed from cart successfully"))
                .andExpect(jsonPath("$.cart.totalItems").value(2));

        verify(cartService).removeItemFromCart(1L, 1L);
    }

    @Test
    void testClearCart() throws Exception {
        doNothing().when(cartService).clearCart(anyLong());

        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        verify(cartService).clearCart(1L);
    }

    @Test
    void testValidateCart() throws Exception {
        when(cartService.validateCartForCheckout(anyLong())).thenReturn(true);

        mockMvc.perform(get("/api/cart/1/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart is valid for checkout"))
                .andExpect(jsonPath("$.isValid").value(true));

        verify(cartService).validateCartForCheckout(1L);
    }

    @Test
    void testValidateCart_Invalid() throws Exception {
        when(cartService.validateCartForCheckout(anyLong())).thenReturn(false);

        mockMvc.perform(get("/api/cart/1/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart validation failed"))
                .andExpect(jsonPath("$.isValid").value(false));

        verify(cartService).validateCartForCheckout(1L);
    }

    @Test
    void testAddToCart_WithError() throws Exception {
        when(cartService.addItemToCart(anyLong(), any(AddToCartDTO.class)))
                .thenThrow(new RuntimeException("Product not available"));

        mockMvc.perform(post("/api/cart/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not available"));

        verify(cartService).addItemToCart(eq(1L), any(AddToCartDTO.class));
    }

    @Test
    void testUpdateCartItem_WithError() throws Exception {
        when(cartService.updateCartItemQuantity(anyLong(), anyLong(), any(UpdateCartItemDTO.class)))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(put("/api/cart/1/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartItemDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cart item not found"));

        verify(cartService).updateCartItemQuantity(eq(1L), eq(1L), any(UpdateCartItemDTO.class));
    }

    @Test
    void testRemoveFromCart_WithError() throws Exception {
        when(cartService.removeItemFromCart(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(delete("/api/cart/1/items/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cart item not found"));

        verify(cartService).removeItemFromCart(1L, 999L);
    }

    @Test
    void testGetCart_WithError() throws Exception {
        when(cartService.getOrCreateCartForUser(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/cart/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(cartService).getOrCreateCartForUser(999L);
    }

    @Test
    void testClearCart_WithError() throws Exception {
        doThrow(new RuntimeException("Cart not found")).when(cartService).clearCart(anyLong());

        mockMvc.perform(delete("/api/cart/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cart not found"));

        verify(cartService).clearCart(999L);
    }

    @Test
    void testGetCartItemsCount() throws Exception {
        when(cartService.getCartItemsCount(anyLong())).thenReturn(5);

        mockMvc.perform(get("/api/cart/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart items count retrieved successfully"))
                .andExpect(jsonPath("$.itemsCount").value(5));

        verify(cartService).getCartItemsCount(1L);
    }

    @Test
    void testGetCartItemsCount_WithError() throws Exception {
        when(cartService.getCartItemsCount(anyLong()))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/api/cart/999/count"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cart not found"));

        verify(cartService).getCartItemsCount(999L);
    }

    @Test
    void testCartTest() throws Exception {
        when(cartService.getOrCreateCartForUser(anyLong())).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/cart/test/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart test successful - database connection working"))
                .andExpect(jsonPath("$.cart").exists())
                .andExpect(jsonPath("$.cartItemsCount").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cartService).getOrCreateCartForUser(1L);
    }

    @Test
    void testCartTest_WithError() throws Exception {
        when(cartService.getOrCreateCartForUser(anyLong()))
                .thenThrow(new RuntimeException("Connection failed"));

        mockMvc.perform(get("/api/cart/test/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").value("RuntimeException"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cartService).getOrCreateCartForUser(999L);
    }

    @Test
    void testValidateCart_WithError() throws Exception {
        when(cartService.validateCartForCheckout(anyLong()))
                .thenThrow(new RuntimeException("Validation error"));

        mockMvc.perform(get("/api/cart/999/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.isValid").value(false));

        verify(cartService).validateCartForCheckout(999L);
    }
}

