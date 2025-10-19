package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.PaginatedResponse;
import com.ecommerce.productservice.dto.ProductCreateDTO;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductStatus;
import com.ecommerce.productservice.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    // Admin Methods - Can see all products regardless of status

    /**
     * Get products for admin with filters and pagination
     */
    public PaginatedResponse<ProductDTO> getProductsForAdmin(
            String category, String status, String search, 
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortDirection) {
        
        // Create pageable with sorting
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "updatedAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Convert status string to enum
        ProductStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = ProductStatus.fromValue(status);
        }
        
        Page<Product> productPage = productRepository.findProductsWithFilters(
            category, statusEnum, search, minPrice, maxPrice, pageable);
        
        return mapToPagedResponse(productPage);
    }

    /**
     * Get all products for admin (no filters)
     */
    public List<ProductDTO> getAllProductsForAdmin() {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
        return products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // User Methods - Only active products

    /**
     * Get active products for users with filters and pagination
     */
    public PaginatedResponse<ProductDTO> getActiveProducts(
            String category, String search, 
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortDirection) {
        
        // Create pageable with sorting
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "updatedAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findActiveProductsWithFilters(
            category, search, minPrice, maxPrice, pageable);
        
        return mapToPagedResponse(productPage);
    }

    // CRUD Operations

    /**
     * Create a new product
     */
    public ProductDTO createProduct(ProductCreateDTO productCreateDTO) {
        // Check for duplicate name
        if (productRepository.existsByNameIgnoreCase(productCreateDTO.getName())) {
            throw new IllegalArgumentException("Product with name '" + productCreateDTO.getName() + "' already exists");
        }
        
        Product product = mapToEntity(productCreateDTO);
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Update an existing product
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        // Check for duplicate name (excluding current product)
        if (productRepository.existsByNameIgnoreCaseAndIdNot(productDTO.getName(), id)) {
            throw new IllegalArgumentException("Product with name '" + productDTO.getName() + "' already exists");
        }
        
        // Update fields
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setStatus(ProductStatus.fromValue(productDTO.getStatus()));
        
        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }

    /**
     * Delete product by ID
     */
    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        return true;
    }

    // Category Methods

    /**
     * Get all categories (for admin) - shows all categories from all products
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    /**
     * Get all categories (for users) - same as admin, shows all categories
     */
    @Transactional(readOnly = true)
    public List<String> getActiveCategories() {
        return productRepository.findDistinctCategories();
    }

    /**
     * Bulk delete products by IDs
     */
    @Transactional
    public int bulkDeleteProducts(List<Long> productIds) {
        // Find existing products
        List<Product> productsToDelete = productRepository.findAllById(productIds);
        
        if (productsToDelete.isEmpty()) {
            return 0;
        }
        
        // Delete the products
        productRepository.deleteAll(productsToDelete);
        
        return productsToDelete.size();
    }

    // Statistics Methods

    /**
     * Get product count by status
     */
    @Transactional(readOnly = true)
    public long getProductCountByStatus(String status) {
        ProductStatus statusEnum = ProductStatus.fromValue(status);
        return productRepository.countByStatus(statusEnum);
    }

    /**
     * Get total product count
     */
    @Transactional(readOnly = true)
    public long getTotalProductCount() {
        return productRepository.count();
    }

    // Helper Methods

    private PaginatedResponse<ProductDTO> mapToPagedResponse(Page<Product> productPage) {
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(
            productDTOs,
            productPage.getTotalElements(),
            productPage.getNumber() + 1, // Convert to 1-based page numbering
            productPage.getSize(),
            productPage.getTotalPages()
        );
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        dto.setStatus(product.getStatus().getValue());
        return dto;
    }

    private Product mapToEntity(ProductCreateDTO dto) {
        Product product = modelMapper.map(dto, Product.class);
        product.setStatus(ProductStatus.fromValue(dto.getStatus()));
        return product;
    }
} 