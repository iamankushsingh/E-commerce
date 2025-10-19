package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart and product
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    /**
     * Find all cart items by cart ID
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Find all cart items by user ID through cart
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);

    /**
     * Count cart items by cart ID
     */
    int countByCartId(Long cartId);

    /**
     * Delete all cart items by cart ID
     */
    void deleteByCartId(Long cartId);

    /**
     * Check if cart item exists for cart and product
     */
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId")
    boolean existsByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
} 