package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.PaginatedResponse;
import com.ecommerce.productservice.dto.ProductCreateDTO;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasKey;

@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductCreateDTO sampleCreateRequest;
    private ProductDTO sampleProduct;

    @BeforeEach
    void setupTestData() {
        sampleCreateRequest = createSampleProductRequest();
        sampleProduct = createSampleProduct();
    }

    private ProductCreateDTO createSampleProductRequest() {
        ProductCreateDTO request = new ProductCreateDTO();
        request.setName("MacBook Pro");
        request.setDescription("Powerful laptop for professionals");
        request.setPrice(new BigDecimal("2499.99"));
        request.setCategory("Electronics");
        request.setImageUrl("http://example.com/macbook.jpg");
        request.setStock(15);
        request.setStatus("active");
        return request;
    }

    private ProductDTO createSampleProduct() {
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("MacBook Pro");
        product.setDescription("Powerful laptop for professionals");
        product.setPrice(new BigDecimal("2499.99"));
        product.setCategory("Electronics");
        product.setImageUrl("http://example.com/macbook.jpg");
        product.setStock(15);
        product.setStatus("active");
        return product;
    }

    @Test
    void whenAdminGetsProductList_thenReturnPaginatedProducts() throws Exception {
        PaginatedResponse<ProductDTO> mockResponse = new PaginatedResponse<>(
                List.of(sampleProduct),
                1,
                1,
                10,
                1
        );
        when(productService.getProductsForAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(mockResponse);
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].name", is("MacBook Pro")));
    }

    @Test
    void whenAdminCreatesProduct_thenReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(ProductCreateDTO.class)))
                .thenReturn(sampleProduct);

      //  sampleCreateRequest.setName(null);
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("MacBook Pro")))
                .andExpect(jsonPath("$.price", is(2499.99)));
    }

    @Test
    void whenAdminCreatesProductWithDuplicateName_thenReturnBadRequest() throws Exception {
        given(productService.createProduct(any(ProductCreateDTO.class)))
                .willThrow(new IllegalArgumentException("Product name already exists"));


        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenAdminDeletesProduct_thenReturnSuccessMessage() throws Exception {
        given(productService.deleteProduct(1L)).willReturn(true);

        
        mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product deleted successfully")));
    }

    @Test
    void whenAdminUpdatesProduct_thenReturnUpdatedProduct() throws Exception {
        ProductDTO updatedProduct = createSampleProduct();
        updatedProduct.setName("Updated MacBook Pro");
        updatedProduct.setPrice(new BigDecimal("2699.99"));
        
        when(productService.updateProduct(eq(1L), any(ProductDTO.class)))
                .thenReturn(updatedProduct);
        
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated MacBook Pro")))
                .andExpect(jsonPath("$.price", is(2699.99)));
    }

    @Test
    void whenAdminGetsProductById_thenReturnProduct() throws Exception {
        given(productService.getProductById(1L)).willReturn(Optional.of(sampleProduct));
        mockMvc.perform(get("/api/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("MacBook Pro")))
                .andExpect(jsonPath("$.price", is(2499.99)));
    }

    @Test
    void whenAdminGetsNonExistentProduct_thenReturn404() throws Exception {
        given(productService.getProductById(999L)).willReturn(Optional.empty());
        mockMvc.perform(get("/api/admin/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenAdminGetsAllProducts_thenReturnProductList() throws Exception {
        List<ProductDTO> products = Arrays.asList(sampleProduct);
        when(productService.getAllProductsForAdmin()).thenReturn(products);
        
        mockMvc.perform(get("/api/admin/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("MacBook Pro")))
                .andExpect(jsonPath("$[0].price", is(2499.99)));
    }

    @Test
    void whenAdminGetsCategories_thenReturnCategoryList() throws Exception {
        List<String> categories = Arrays.asList("Electronics", "Clothing", "Books");
        when(productService.getAllCategories()).thenReturn(categories);
        
        mockMvc.perform(get("/api/admin/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("Electronics")))
                .andExpect(jsonPath("$[1]", is("Clothing")))
                .andExpect(jsonPath("$[2]", is("Books")));
    }

    @Test
    void whenAdminGetsProductStats_thenReturnStatistics() throws Exception {
        when(productService.getTotalProductCount()).thenReturn(100L);
        when(productService.getProductCountByStatus("active")).thenReturn(80L);
        when(productService.getProductCountByStatus("inactive")).thenReturn(20L);
        
        mockMvc.perform(get("/api/admin/products/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts", is(100)))
                .andExpect(jsonPath("$.activeProducts", is(80)))
                .andExpect(jsonPath("$.inactiveProducts", is(20)));
    }

    @Test
    void whenServiceThrowsException_onGetProducts_thenReturn500() throws Exception {
        when(productService.getProductsForAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsException_onGetAllProducts_thenReturn500() throws Exception {
        when(productService.getAllProductsForAdmin())
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/admin/products/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsException_onGetProductById_thenReturn500() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/admin/products/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsGenericException_onCreateProduct_thenReturn500() throws Exception {
        when(productService.createProduct(any(ProductCreateDTO.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsGenericException_onUpdateProduct_thenReturn500() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductDTO.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsIllegalArgumentException_onUpdateProduct_thenReturn400() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductDTO.class)))
                .thenThrow(new IllegalArgumentException("Product not found"));
        
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenServiceThrowsException_onDeleteProduct_thenReturn500() throws Exception {
        when(productService.deleteProduct(1L))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsIllegalArgumentException_onDeleteProduct_thenReturn404() throws Exception {
        when(productService.deleteProduct(999L))
                .thenThrow(new IllegalArgumentException("Product not found"));
        
        mockMvc.perform(delete("/api/admin/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenServiceThrowsException_onGetCategories_thenReturn500() throws Exception {
        when(productService.getAllCategories())
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/admin/products/categories"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenServiceThrowsException_onGetStats_thenReturn500() throws Exception {
        when(productService.getTotalProductCount())
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/admin/products/stats"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenValidationFails_onCreateProduct_thenReturn400() throws Exception {
        // Create invalid product with missing required fields
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        invalidRequest.setName(""); // Invalid - empty name
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenValidationFails_onUpdateProduct_thenReturn400() throws Exception {
        // Create invalid product with missing required fields
        ProductDTO invalidProduct = new ProductDTO();
        invalidProduct.setName(""); // Invalid - empty name
        
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenValidationFails_onCreateProduct_withTooLongName_thenReturn400() throws Exception {
        // Create product with name exceeding 255 characters
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        invalidRequest.setName("a".repeat(256)); // Too long name
        invalidRequest.setDescription("Valid Description");
        invalidRequest.setPrice(new BigDecimal("10.00"));
        invalidRequest.setCategory("Electronics");
        invalidRequest.setStock(1);
        invalidRequest.setStatus("active");
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").value("Product name must not exceed 255 characters"));
    }

    @Test
    void whenValidationFails_onCreateProduct_withMultipleErrors_thenReturn400() throws Exception {
        // Create product with multiple validation errors
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        invalidRequest.setName(""); // Empty name
        invalidRequest.setDescription(""); // Empty description
        invalidRequest.setPrice(BigDecimal.ZERO); // Zero price
        invalidRequest.setCategory(""); // Empty category
        invalidRequest.setStock(-1); // Negative stock
        invalidRequest.setStatus("invalid"); // Invalid status
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors", hasKey("name")))
                .andExpect(jsonPath("$.errors", hasKey("description")))
                .andExpect(jsonPath("$.errors", hasKey("price")))
                .andExpect(jsonPath("$.errors", hasKey("category")))
                .andExpect(jsonPath("$.errors", hasKey("stock")))
                .andExpect(jsonPath("$.errors", hasKey("status")));
    }

    @Test
    void whenValidationFails_onUpdateProduct_withTooLongDescription_thenReturn400() throws Exception {
        // Create product with description exceeding 1000 characters
        ProductDTO invalidProduct = new ProductDTO();
        invalidProduct.setName("Valid Product");
        invalidProduct.setDescription("a".repeat(1001)); // Too long description
        invalidProduct.setPrice(new BigDecimal("10.00"));
        invalidProduct.setCategory("Electronics");
        invalidProduct.setStock(1);
        invalidProduct.setStatus("active");
        
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.description").value("Description must not exceed 1000 characters"));
    }

    @Test
    void whenValidationFails_onCreateProduct_withNegativePrice_thenReturn400() throws Exception {
        // Create product with negative price
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        invalidRequest.setName("Valid Product");
        invalidRequest.setDescription("Valid Description");
        invalidRequest.setPrice(new BigDecimal("-10.00")); // Negative price
        invalidRequest.setCategory("Electronics");
        invalidRequest.setStock(1);
        invalidRequest.setStatus("active");
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.price").value("Price must be greater than 0"));
    }

    @Test
    void whenValidationFails_onCreateProduct_withNullRequiredFields_thenReturn400() throws Exception {
        // Create product with null required fields
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        // All required fields are null by default
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors", hasKey("name")))
                .andExpect(jsonPath("$.errors", hasKey("description")))
                .andExpect(jsonPath("$.errors", hasKey("price")))
                .andExpect(jsonPath("$.errors", hasKey("category")))
                .andExpect(jsonPath("$.errors", hasKey("stock")))
                .andExpect(jsonPath("$.errors", hasKey("status")));
    }

    @Test
    void whenValidationFails_onCreateProduct_withInvalidImageUrlLength_thenReturn400() throws Exception {
        // Create product with imageUrl exceeding 500 characters
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        invalidRequest.setName("Valid Product");
        invalidRequest.setDescription("Valid Description");
        invalidRequest.setPrice(new BigDecimal("10.00"));
        invalidRequest.setCategory("Electronics");
        invalidRequest.setImageUrl("http://example.com/" + "a".repeat(500)); // Too long URL
        invalidRequest.setStock(1);
        invalidRequest.setStatus("active");
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.imageUrl").value("Image URL must not exceed 500 characters"));
    }

    @Test
    void whenValidationFails_onUpdateProduct_withNegativeStock_thenReturn400() throws Exception {
        // Create product with negative stock
        ProductDTO invalidProduct = new ProductDTO();
        invalidProduct.setName("Valid Product");
        invalidProduct.setDescription("Valid Description");
        invalidProduct.setPrice(new BigDecimal("10.00"));
        invalidProduct.setCategory("Electronics");
        invalidProduct.setStock(-5); // Negative stock
        invalidProduct.setStatus("active");
        
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.stock").value("Stock cannot be negative"));
    }

    @Test
    void whenValidationFails_onCreateProduct_withInvalidStatusPattern_thenReturn400() throws Exception {
        // Create product with invalid status pattern
        ProductCreateDTO invalidRequest = new ProductCreateDTO();
        invalidRequest.setName("Valid Product");
        invalidRequest.setDescription("Valid Description");
        invalidRequest.setPrice(new BigDecimal("10.00"));
        invalidRequest.setCategory("Electronics");
        invalidRequest.setStock(1);
        invalidRequest.setStatus("published"); // Invalid status - should be 'active' or 'inactive'
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.status").value("Status must be 'active' or 'inactive'"));
    }

    @Test
    void whenValidProduct_atBoundaryLimits_thenReturnSuccess() throws Exception {
        // Create product with values at exact boundary limits
        ProductCreateDTO validRequest = new ProductCreateDTO();
        validRequest.setName("a".repeat(255)); // Exactly 255 characters
        validRequest.setDescription("a".repeat(1000)); // Exactly 1000 characters
        validRequest.setPrice(new BigDecimal("0.01")); // Minimum valid price
        validRequest.setCategory("a".repeat(100)); // Exactly 100 characters
        validRequest.setImageUrl("http://example.com/" + "a".repeat(481)); // Exactly 500 characters
        validRequest.setStock(0); // Minimum valid stock
        validRequest.setStatus("active");

        ProductDTO createdProduct = new ProductDTO();
        createdProduct.setId(1L);
        when(productService.createProduct(any(ProductCreateDTO.class)))
            .thenReturn(createdProduct);
        
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void whenRequestParameterValidation_invalidPageSize_thenReturn400() throws Exception {
        // Test with invalid page size (should be positive)
        mockMvc.perform(get("/api/admin/products")
                .param("page", "0")
                .param("size", "0")) // Invalid size - should be > 0
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenRequestParameterValidation_invalidPageNumber_thenReturn400() throws Exception {
        // Test with invalid page number (should be non-negative)
        mockMvc.perform(get("/api/admin/products")
                .param("page", "-1") // Invalid page - should be >= 0
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenAdminDeletesProduct_andServiceReturnsFalse_thenReturn404() throws Exception {
        // This tests the missing branch where deleteProduct returns false instead of throwing exception
        given(productService.deleteProduct(999L)).willReturn(false);
        
        mockMvc.perform(delete("/api/admin/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenServiceThrowsIllegalArgumentException_onCreateProduct_thenReturn400() throws Exception {
        // Given
        given(productService.createProduct(any(ProductCreateDTO.class)))
                .willThrow(new IllegalArgumentException("Product name already exists"));
        
        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenServiceThrowsRuntimeException_onGetProducts_thenReturn500() throws Exception {
        // Given
        given(productService.getProductsForAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .willThrow(new RuntimeException("Database connection failed"));
        
        // When & Then
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenAdminGetsProductsWithInvalidParameters_thenStillProcessRequest() throws Exception {
        // Given
        PaginatedResponse<ProductDTO> mockResponse = new PaginatedResponse<>(
            Arrays.asList(sampleProduct), 1L, 0, 10, 1);
        given(productService.getProductsForAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .willReturn(mockResponse);
        
        // When & Then - Test with negative page number (should still work as service handles it)
        mockMvc.perform(get("/api/admin/products")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isOk());
    }

  
}