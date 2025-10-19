package com.ecommerce.analyticsservice.client;

import com.ecommerce.analyticsservice.dto.external.OrderDTO;
import com.ecommerce.analyticsservice.dto.external.PaginatedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private UserServiceClient userServiceClient;

    private PaginatedResponse<OrderDTO> paginatedResponse;
    private Map<String, Object> mockResponse;

    @BeforeEach
    void setUp() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setTotalAmount(new BigDecimal("100.00"));
        orderDTO.setOrderStatus("DELIVERED");
        orderDTO.setCreatedAt(LocalDateTime.now());

        paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setContent(Arrays.asList(orderDTO));
        paginatedResponse.setPage(0);
        paginatedResponse.setSize(10);
        paginatedResponse.setTotalElements(1L);
        paginatedResponse.setTotalPages(1);

        mockResponse = new HashMap<>();
        mockResponse.put("orders", paginatedResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testOrderServiceClient_GetAllOrdersForAdmin() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockResponse));

        Mono<PaginatedResponse<OrderDTO>> result = 
            orderServiceClient.getAllOrdersForAdmin(0, 10);

        assertNotNull(result);
        verify(webClient).get();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testOrderServiceClient_GetOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", 100);
        stats.put("totalRevenue", 10000.00);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(stats));

        Mono<Map<String, Object>> result = orderServiceClient.getOrderStatistics();

        assertNotNull(result);
        Map<String, Object> response = result.block();
        assertNotNull(response);
        assertEquals(100, response.get("totalOrders"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testOrderServiceClient_Error() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.error(new RuntimeException("Service error")));

        Mono<PaginatedResponse<OrderDTO>> result = 
            orderServiceClient.getAllOrdersForAdmin(0, 10);

        assertNotNull(result);
    }

    @Test
    void testProductServiceClient_Exists() {
        assertNotNull(productServiceClient);
    }

    @Test
    void testUserServiceClient_Exists() {
        assertNotNull(userServiceClient);
    }
}

