package com.ecommerce.productservice.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PaginatedResponseTest {

    private PaginatedResponse<ProductDTO> paginatedResponse;
    private List<ProductDTO> sampleData;

    @BeforeEach
    void setUp() {
        paginatedResponse = new PaginatedResponse<>();
        
        // Create sample data
        ProductDTO product1 = new ProductDTO();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("10.00"));
        
        ProductDTO product2 = new ProductDTO();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("20.00"));
        
        sampleData = Arrays.asList(product1, product2);
    }

    @Test
    void testAllSettersAndGetters() {
        // Test all setter/getter pairs
        paginatedResponse.setData(sampleData);
        paginatedResponse.setPage(0);
        paginatedResponse.setPageSize(10);
        paginatedResponse.setTotal(100L);
        paginatedResponse.setTotalPages(10);

        // Verify all values
        assertThat(paginatedResponse.getData()).isEqualTo(sampleData);
        assertThat(paginatedResponse.getPage()).isEqualTo(0);
        assertThat(paginatedResponse.getPageSize()).isEqualTo(10);
        assertThat(paginatedResponse.getTotal()).isEqualTo(100L);
        assertThat(paginatedResponse.getTotalPages()).isEqualTo(10);
    }

    @Test
    void testDefaultConstructor() {
        PaginatedResponse<String> newResponse = new PaginatedResponse<>();
        
        assertThat(newResponse.getData()).isNull();
        assertThat(newResponse.getPage()).isEqualTo(0);
        assertThat(newResponse.getPageSize()).isEqualTo(0);
        assertThat(newResponse.getTotal()).isEqualTo(0L);
        assertThat(newResponse.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testParameterizedConstructor() {
        // Test the parameterized constructor
        PaginatedResponse<ProductDTO> response = new PaginatedResponse<>(
            sampleData, 100L, 0, 10, 10);
        
        assertThat(response.getData()).isEqualTo(sampleData);
        assertThat(response.getTotal()).isEqualTo(100L);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(10);
    }

    @Test
    void testNullValues() {
        // Test setting null values for data
        paginatedResponse.setData(null);

        assertThat(paginatedResponse.getData()).isNull();
        assertThat(paginatedResponse.getPage()).isEqualTo(0);
        assertThat(paginatedResponse.getPageSize()).isEqualTo(0);
        assertThat(paginatedResponse.getTotal()).isEqualTo(0L);
        assertThat(paginatedResponse.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testEmptyData() {
        // Test with empty list
        paginatedResponse.setData(Collections.emptyList());
        paginatedResponse.setPage(0);
        paginatedResponse.setPageSize(10);
        paginatedResponse.setTotal(0L);
        paginatedResponse.setTotalPages(0);

        assertThat(paginatedResponse.getData()).isEmpty();
        assertThat(paginatedResponse.getPage()).isEqualTo(0);
        assertThat(paginatedResponse.getPageSize()).isEqualTo(10);
        assertThat(paginatedResponse.getTotal()).isEqualTo(0L);
        assertThat(paginatedResponse.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testEdgeCaseValues() {
        // Test with edge case values
        paginatedResponse.setPage(Integer.MAX_VALUE);
        paginatedResponse.setPageSize(1);
        paginatedResponse.setTotal(Long.MAX_VALUE);
        paginatedResponse.setTotalPages(Integer.MAX_VALUE);

        assertThat(paginatedResponse.getPage()).isEqualTo(Integer.MAX_VALUE);
        assertThat(paginatedResponse.getPageSize()).isEqualTo(1);
        assertThat(paginatedResponse.getTotal()).isEqualTo(Long.MAX_VALUE);
        assertThat(paginatedResponse.getTotalPages()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void testNegativeValues() {
        // Test with negative values
        paginatedResponse.setPage(-1);
        paginatedResponse.setPageSize(-5);
        paginatedResponse.setTotal(-10L);
        paginatedResponse.setTotalPages(-2);

        assertThat(paginatedResponse.getPage()).isEqualTo(-1);
        assertThat(paginatedResponse.getPageSize()).isEqualTo(-5);
        assertThat(paginatedResponse.getTotal()).isEqualTo(-10L);
        assertThat(paginatedResponse.getTotalPages()).isEqualTo(-2);
    }

    @Test
    void testDifferentGenericTypes() {
        // Test with different generic types to ensure type safety
        PaginatedResponse<String> stringResponse = new PaginatedResponse<>();
        stringResponse.setData(Arrays.asList("item1", "item2"));
        stringResponse.setPage(0);
        stringResponse.setPageSize(2);
        stringResponse.setTotal(2L);

        assertThat(stringResponse.getData()).containsExactly("item1", "item2");
        assertThat(stringResponse.getData()).allMatch(item -> item instanceof String);
    }

    @Test
    void testToString() {
        // Test the toString method
        paginatedResponse.setData(Arrays.asList());
        paginatedResponse.setPage(1);
        paginatedResponse.setPageSize(10);
        paginatedResponse.setTotal(50L);
        paginatedResponse.setTotalPages(5);

        String result = paginatedResponse.toString();
        
        assertThat(result).contains("PaginatedResponse{");
        assertThat(result).contains("data=[]");
        assertThat(result).contains("page=1");
        assertThat(result).contains("pageSize=10");
        assertThat(result).contains("total=50");
        assertThat(result).contains("totalPages=5");
    }

    @Test
    void testLargeDataSet() {
        // Test with larger data set
        List<ProductDTO> largeData = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ProductDTO product = new ProductDTO();
            product.setId((long) i);
            product.setName("Product " + i);
            largeData.add(product);
        }
        
        // Set the large data set
        paginatedResponse.setData(largeData);
        paginatedResponse.setPage(5);
        paginatedResponse.setPageSize(20);
        paginatedResponse.setTotal(1000L);
        paginatedResponse.setTotalPages(50);

        assertThat(paginatedResponse.getData()).hasSize(100);
        assertThat(paginatedResponse.getPage()).isEqualTo(5);
        assertThat(paginatedResponse.getPageSize()).isEqualTo(20);
        assertThat(paginatedResponse.getTotal()).isEqualTo(1000L);
        assertThat(paginatedResponse.getTotalPages()).isEqualTo(50);
    }
} 