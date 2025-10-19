package com.ecommerce.wishlistservice.repository;

import com.ecommerce.wishlistservice.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Find all items in a specific collection
     */
    List<WishlistItem> findByCollectionIdOrderByAddedAtDesc(Long collectionId);

    /**
     * Find a specific item in a collection by product ID
     */
    Optional<WishlistItem> findByCollectionIdAndProductId(Long collectionId, Long productId);

    /**
     * Check if a product exists in a collection
     */
    boolean existsByCollectionIdAndProductId(Long collectionId, Long productId);

    /**
     * Find all items for a specific user across all collections
     */
    @Query("SELECT wi FROM WishlistItem wi JOIN wi.collection wc WHERE wc.userId = :userId ORDER BY wi.addedAt DESC")
    List<WishlistItem> findByUserId(@Param("userId") Long userId);

    /**
     * Find all collections that contain a specific product for a user
     */
    @Query("SELECT wi FROM WishlistItem wi JOIN wi.collection wc WHERE wc.userId = :userId AND wi.productId = :productId")
    List<WishlistItem> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * Count total items for a user across all collections
     */
    @Query("SELECT COUNT(wi) FROM WishlistItem wi JOIN wi.collection wc WHERE wc.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Delete all items for a specific product across all collections for a user
     */
    @Query("DELETE FROM WishlistItem wi WHERE wi.productId = :productId AND wi.collection.userId = :userId")
    void deleteByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);
} 