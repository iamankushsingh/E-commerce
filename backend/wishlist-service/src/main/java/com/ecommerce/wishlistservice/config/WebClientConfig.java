package com.ecommerce.wishlistservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${webclient.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${webclient.read-timeout:30000}")
    private int readTimeout;

    @Value("${webclient.write-timeout:30000}")
    private int writeTimeout;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)); // 16MB buffer
    }

    @Bean("productServiceWebClient")
    public WebClient productServiceWebClient(
            @Value("${services.product.name}") String productServiceName,
            WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://" + productServiceName)
                .build();
    }

    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient(
            @Value("${services.user.name}") String userServiceName,
            WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://" + userServiceName)
                .build();
    }
} 