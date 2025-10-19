package com.ecommerce.analyticsservice.client;

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
public class ProductServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClient.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ProductServiceClient(@Qualifier("productServiceWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get product statistics
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getProductStatistics() {
        logger.info("Fetching product statistics from product service");
        
        return webClient.get()
                .uri("/api/admin/products/stats")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> (Map<String, Object>) response)
                .doOnError(error -> logger.error("Error fetching product statistics: {}", error.getMessage()));
    }

    /**
     * Get product by ID
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getProductById(Long productId) {
        logger.info("Fetching product {} from product service", productId);
        
        return webClient.get()
                .uri("/api/admin/products/{id}", productId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> (Map<String, Object>) response)
                .doOnError(error -> logger.error("Error fetching product {}: {}", productId, error.getMessage()));
    }
} 