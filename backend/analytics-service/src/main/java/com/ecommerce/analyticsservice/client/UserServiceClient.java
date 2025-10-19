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
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public UserServiceClient(@Qualifier("userServiceWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get user statistics
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getUserStatistics() {
        logger.info("Fetching user statistics from user service");
        
        return webClient.get()
                .uri("/api/admin/users/stats")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> (Map<String, Object>) response)
                .doOnError(error -> logger.error("Error fetching user statistics: {}", error.getMessage()));
    }
} 