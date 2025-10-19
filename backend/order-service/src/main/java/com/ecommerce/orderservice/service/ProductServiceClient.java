package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ProductServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClient.class);

    private final WebClient webClient;

    @Value("${services.product.name}")
    private String productServiceName;

    @Autowired
    public ProductServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Get product details by ID
     */
    public ProductDTO getProductById(Long productId) {
        try {
            logger.info("Fetching product details for product ID: {}", productId);
            
            return webClient.get()
                    .uri("http://" + productServiceName + "/api/products/" + productId)
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .doOnError(error -> logger.error("Error fetching product {}: {}", productId, error.getMessage()))
                    .onErrorResume(error -> {
                        logger.warn("Fallback: Creating empty product for ID: {}", productId);
                        ProductDTO fallbackProduct = new ProductDTO();
                        fallbackProduct.setId(productId);
                        fallbackProduct.setName("Product Not Available");
                        fallbackProduct.setDescription("Product details not available");
                        fallbackProduct.setImageUrl("assets/placeholder.png");
                        return Mono.just(fallbackProduct);
                    })
                    .block();
                    
        } catch (Exception e) {
            logger.error("Failed to fetch product {}: {}", productId, e.getMessage());
            
            // Create fallback product
            ProductDTO fallbackProduct = new ProductDTO();
            fallbackProduct.setId(productId);
            fallbackProduct.setName("Product Not Available");
            fallbackProduct.setDescription("Product details not available");
            fallbackProduct.setImageUrl("assets/placeholder.png");
            return fallbackProduct;
        }
    }

    /**
     * Check product availability and stock
     */
    public boolean isProductAvailable(Long productId, Integer quantity) {
        try {
            logger.info("Checking availability for product ID: {} with quantity: {}", productId, quantity);
            
            ProductDTO product = getProductById(productId);
            
            if (product == null || product.getStock() == null) {
                return false;
            }
            
            return "ACTIVE".equalsIgnoreCase(product.getStatus()) && 
                   product.getStock() >= quantity;
                   
        } catch (Exception e) {
            logger.error("Error checking product availability for {}: {}", productId, e.getMessage());
            return false;
        }
    }

    /**
     * Reserve product stock (placeholder - would integrate with inventory service)
     */
    public boolean reserveProductStock(Long productId, Integer quantity) {
        try {
            logger.info("Attempting to reserve {} units of product ID: {}", quantity, productId);
            
            // In a real implementation, this would call an inventory management API
            // For now, we'll just check availability
            return isProductAvailable(productId, quantity);
            
        } catch (Exception e) {
            logger.error("Error reserving product stock for {}: {}", productId, e.getMessage());
            return false;
        }
    }

    /**
     * Release product stock (placeholder - would integrate with inventory service)
     */
    public void releaseProductStock(Long productId, Integer quantity) {
        try {
            logger.info("Releasing {} units of product ID: {}", quantity, productId);
            
            // In a real implementation, this would call an inventory management API
            // to release the reserved stock
            
        } catch (Exception e) {
            logger.error("Error releasing product stock for {}: {}", productId, e.getMessage());
        }
    }
} 