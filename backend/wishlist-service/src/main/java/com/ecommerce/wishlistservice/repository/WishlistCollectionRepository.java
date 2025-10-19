package com.ecommerce.wishlistservice.repository;

import com.ecommerce.wishlistservice.entity.WishlistCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistCollectionRepository extends JpaRepository<WishlistCollection, Long> {

    /**
     * Find all collections for a specific user
     */
    List<WishlistCollection> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find a specific collection by ID and user ID
     */
    Optional<WishlistCollection> findByIdAndUserId(Long id, Long userId);

    /**
     * Check if a collection exists for a user with the given name
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * Count total collections for a user
     */
    long countByUserId(Long userId);

    /**
     * Find collections with items count
     */
    @Query("SELECT c FROM WishlistCollection c LEFT JOIN FETCH c.items WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<WishlistCollection> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Delete all collections for a user
     */
    void deleteByUserId(Long userId);
} 