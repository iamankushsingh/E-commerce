package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find order items by product ID
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Count order items by order ID
     */
    int countByOrderId(Long orderId);

    /**
     * Delete all order items by order ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * Find order items by user ID through order
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.userId = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);

    /**
     * Get total quantity of a product across all orders
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantityByProductId(@Param("productId") Long productId);

    /**
     * Find most popular products (by total quantity ordered)
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findMostPopularProducts();
} 