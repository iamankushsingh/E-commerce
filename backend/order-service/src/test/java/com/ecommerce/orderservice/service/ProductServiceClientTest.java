package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ProductServiceClient productServiceClient;

    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productServiceClient, "productServiceName", "product-service");

        testProductDTO = new ProductDTO();
        testProductDTO.setId(1L);
        testProductDTO.setName("Test Product");
        testProductDTO.setDescription("Test Description");
        testProductDTO.setPrice(new BigDecimal("50.00"));
        testProductDTO.setStock(100);
        testProductDTO.setStatus("ACTIVE");
        testProductDTO.setImageUrl("http://image.url");
    }

    @Test
    void testGetProductById_Success() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class)).thenReturn(Mono.just(testProductDTO));

        ProductDTO result = productServiceClient.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void testGetProductById_Error() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        ProductDTO result = productServiceClient.getProductById(1L);

        assertNotNull(result);
        assertEquals("Product Not Available", result.getName());
    }

    @Test
    void testIsProductAvailable_Available() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class)).thenReturn(Mono.just(testProductDTO));

        boolean result = productServiceClient.isProductAvailable(1L, 10);

        assertTrue(result);
    }

    @Test
    void testIsProductAvailable_NotAvailable_InsufficientStock() {
        testProductDTO.setStock(5);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class)).thenReturn(Mono.just(testProductDTO));

        boolean result = productServiceClient.isProductAvailable(1L, 10);

        assertFalse(result);
    }

    @Test
    void testIsProductAvailable_NotAvailable_InactiveProduct() {
        testProductDTO.setStatus("INACTIVE");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class)).thenReturn(Mono.just(testProductDTO));

        boolean result = productServiceClient.isProductAvailable(1L, 10);

        assertFalse(result);
    }

    @Test
    void testReserveProductStock_Success() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class)).thenReturn(Mono.just(testProductDTO));

        boolean result = productServiceClient.reserveProductStock(1L, 10);

        assertTrue(result);
    }

    @Test
    void testReserveProductStock_Failed() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        boolean result = productServiceClient.reserveProductStock(1L, 10);

        assertFalse(result);
    }

    @Test
    void testReleaseProductStock() {
        assertDoesNotThrow(() -> productServiceClient.releaseProductStock(1L, 10));
    }
}

