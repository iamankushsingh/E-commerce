package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.PaginatedResponse;
import com.ecommerce.productservice.dto.ProductCreateDTO;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "http://localhost:4200")

public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    private final ProductService productService;

    @Autowired
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get products for admin with filters and pagination
     * GET /api/admin/products
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductDTO>> getProductsForAdmin(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            logger.info("Admin fetching products - Page: {}, Size: {}, Category: {}, Status: {}, Search: {}", 
                       page, size, category, status, search);
            
            PaginatedResponse<ProductDTO> response = productService.getProductsForAdmin(
                category, status, search, minPrice, maxPrice, page, size, sortBy, sortDirection);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching products for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all products for admin (no pagination)
     * GET /api/admin/products/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProductsForAdmin() {
        try {
            logger.info("Admin fetching all products");
            List<ProductDTO> products = productService.getAllProductsForAdmin();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error fetching all products for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get product by ID
     * GET /api/admin/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            logger.info("Admin fetching product with ID: {}", id);
            Optional<ProductDTO> product = productService.getProductById(id);
            
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching product with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new product
     * POST /api/admin/products
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO) {
        try {
            logger.info("Admin creating new product: {}", productCreateDTO.getName());
            ProductDTO createdProduct = productService.createProduct(productCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid product data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing product
     * PUT /api/admin/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody ProductDTO productDTO) {
        try {
            logger.info("Admin updating product with ID: {}", id);
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid product data for update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating product with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a product
     * DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        try {
            logger.info("Admin deleting product with ID: {}", id);
            boolean deleted = productService.deleteProduct(id);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Product deleted successfully", "success", true));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Product not found for deletion: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting product with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all categories
     * GET /api/admin/products/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        try {
            logger.info("Admin fetching all categories");
            List<String> categories = productService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get product statistics
     * GET /api/admin/products/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        try {
            logger.info("Admin fetching product statistics");
            long totalProducts = productService.getTotalProductCount();
            long activeProducts = productService.getProductCountByStatus("active");
            long inactiveProducts = productService.getProductCountByStatus("inactive");
            
            Map<String, Object> stats = Map.of(
                "totalProducts", totalProducts,
                "activeProducts", activeProducts,
                "inactiveProducts", inactiveProducts
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching product statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 