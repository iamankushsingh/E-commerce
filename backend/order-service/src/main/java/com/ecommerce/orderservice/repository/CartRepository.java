package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete cart by user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Get cart with cart items for user
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithCartItems(@Param("userId") Long userId);

    /**
     * Count total items in user's cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.userId = :userId")
    Integer getTotalItemsByUserId(@Param("userId") Long userId);
} 