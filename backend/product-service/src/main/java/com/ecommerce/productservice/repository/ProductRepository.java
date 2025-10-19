package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category
    Page<Product> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    // Find products by status
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Find products by name or description containing search term
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> findByNameOrDescriptionContainingIgnoreCase(@Param("search") String search, Pageable pageable);

    // Complex filter query for admin and user product listing
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findProductsWithFilters(
            @Param("category") String category,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // For user listing - only active products
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND " +
           "(:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findActiveProductsWithFilters(
            @Param("category") String category,
            @Param("search") String search,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Get distinct categories
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findDistinctCategories();

    // Get distinct categories for active products only
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.category")
    List<String> findDistinctActiveCategories();

    // Count products by status
    long countByStatus(ProductStatus status);

    // Check if product exists by name (for duplicate validation)
    boolean existsByNameIgnoreCase(String name);

    // Check if product exists by name and not the same id (for update validation)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
} 