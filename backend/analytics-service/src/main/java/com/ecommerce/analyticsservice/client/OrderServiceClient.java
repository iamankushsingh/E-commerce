package com.ecommerce.analyticsservice.client;

import com.ecommerce.analyticsservice.dto.external.OrderDTO;
import com.ecommerce.analyticsservice.dto.external.PaginatedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class OrderServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceClient.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OrderServiceClient(@Qualifier("orderServiceWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all orders for admin with pagination
     */
    public Mono<PaginatedResponse<OrderDTO>> getAllOrdersForAdmin(int page, int size) {
        logger.info("Fetching orders from order service - page: {}, size: {}", page, size);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/admin/orders")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sortBy", "createdAt")
                        .queryParam("sortDirection", "desc")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> {
                    try {
                        // Extract the orders from the response
                        Object ordersData = response.get("orders");
                        return objectMapper.convertValue(ordersData, new TypeReference<PaginatedResponse<OrderDTO>>() {});
                    } catch (Exception e) {
                        logger.error("Error parsing order response: {}", e.getMessage());
                        return new PaginatedResponse<OrderDTO>();
                    }
                })
                .doOnError(error -> logger.error("Error fetching orders: {}", error.getMessage()));
    }

    /**
     * Get order statistics
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getOrderStatistics() {
        logger.info("Fetching order statistics from order service");
        
        return webClient.get()
                .uri("/api/admin/orders/statistics")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> (Map<String, Object>) response)
                .doOnError(error -> logger.error("Error fetching order statistics: {}", error.getMessage()));
    }
} 