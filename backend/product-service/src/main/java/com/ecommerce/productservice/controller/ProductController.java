package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.PaginatedResponse;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get active products for users with filters and pagination
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductDTO>> getActiveProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            logger.info("User fetching products - Page: {}, Size: {}, Category: {}, Search: {}", 
                       page, pageSize, category, search);
            
            PaginatedResponse<ProductDTO> response = productService.getActiveProducts(
                category, search, minPrice, maxPrice, page, pageSize, sortBy, sortDirection);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching products for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get product by ID (only if active)
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getActiveProductById(@PathVariable Long id) {
        try {
            logger.info("User fetching product with ID: {}", id);
            Optional<ProductDTO> product = productService.getProductById(id);
            
            if (product.isPresent()) {
                // Check if product is active
                if ("active".equals(product.get().getStatus())) {
                    return ResponseEntity.ok(product.get());
                } else {
                    return ResponseEntity.notFound().build(); // Don't show inactive products to users
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching product with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active categories only (for users)
     * GET /api/products/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getActiveCategories() {
        try {
            logger.info("User fetching active categories");
            List<String> categories = productService.getActiveCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error fetching active categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 