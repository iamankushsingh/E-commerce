package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.PaginatedResponse;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ProductDTO sampleProduct;

    @BeforeEach
    void setupTestData() {
        sampleProduct = createSampleProduct();
    }

    private ProductDTO createSampleProduct() {
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setDescription("Latest iPhone with amazing features");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory("Electronics");
        product.setImageUrl("http://example.com/iphone15.jpg");
        product.setStock(10);
        product.setStatus("active");
        return product;
    }

    @Test
    void whenGetActiveProducts_thenReturnProductList() throws Exception {
        PaginatedResponse<ProductDTO> mockResponse = new PaginatedResponse<>(
            List.of(sampleProduct),
            1,
            1,
            10,
            1
        );
        when(productService.getActiveProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(mockResponse);
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].name", is("iPhone 15")));
    }

    @Test
    void whenGetActiveProductById_andProductExists_thenReturnProduct() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(Optional.of(sampleProduct));
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("iPhone 15")))
                .andExpect(jsonPath("$.price", is(999.99)));
    }

    @Test
    void whenGetActiveProductById_andProductIsInactive_thenReturn404() throws Exception {
        ProductDTO inactiveProduct = createSampleProduct();
        inactiveProduct.setStatus("inactive");
        when(productService.getProductById(1L))
                .thenReturn(Optional.of(inactiveProduct));
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetActiveProductById_andProductNotExists_thenReturn404() throws Exception {
        when(productService.getProductById(999L))
                .thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetCategories_thenReturnCategoryList() throws Exception {
        List<String> categories = List.of("Electronics", "Clothing", "Books");
        when(productService.getActiveCategories())
                .thenReturn(categories);
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("Electronics")))
                .andExpect(jsonPath("$[1]", is("Clothing")))
                .andExpect(jsonPath("$[2]", is("Books")));
    }

    @Test
    void whenServiceThrowsException_onGetActiveProducts_thenReturn500() throws Exception {
        when(productService.getActiveProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsException_onGetActiveProductById_thenReturn500() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsException_onGetActiveCategories_thenReturn500() throws Exception {
        when(productService.getActiveCategories())
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenGetActiveProducts_withAllParameters_thenReturnProducts() throws Exception {
        PaginatedResponse<ProductDTO> mockResponse = new PaginatedResponse<>(
            List.of(sampleProduct),
            1,
            1,
            6,
            1
        );
        when(productService.getActiveProducts(
            eq("Electronics"), 
            eq("search"), 
            eq(new BigDecimal("10.00")), 
            eq(new BigDecimal("1000.00")), 
            eq(0), 
            eq(6), 
            eq("name"), 
            eq("asc")))
                .thenReturn(mockResponse);
        
        mockMvc.perform(get("/api/products")
                .param("category", "Electronics")
                .param("search", "search")
                .param("minPrice", "10.00")
                .param("maxPrice", "1000.00")
                .param("page", "0")
                .param("pageSize", "6")
                .param("sortBy", "name")
                .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].name", is("iPhone 15")));
    }

    @Test
    void whenGetActiveProducts_withInvalidParameters_thenStillProcessRequest() throws Exception {
        // Given
        PaginatedResponse<ProductDTO> mockResponse = new PaginatedResponse<>(
            Arrays.asList(sampleProduct), 1L, 0, 10, 1);
        when(productService.getActiveProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(mockResponse);
        
        // When & Then - Test with edge case parameters
        mockMvc.perform(get("/api/products")
                .param("page", "-1")
                .param("size", "0")
                .param("minPrice", "-10")
                .param("maxPrice", "0"))
                .andExpect(status().isOk());
    }

    @Test 
    void whenGetActiveProductById_withInvalidId_thenHandledGracefully() throws Exception {
        // Given
        when(productService.getProductById(0L))
                .thenReturn(Optional.empty());
        
        // When & Then - Test with edge case ID
        mockMvc.perform(get("/api/products/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenServiceThrowsNullPointerException_onGetActiveProducts_thenReturn500() throws Exception {
        // Given
        when(productService.getActiveProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new NullPointerException("Null reference"));
        
        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isInternalServerError());
    }
}