package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by user ID
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Find orders by user ID with order items
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.userId = :userId")
    List<Order> findByUserIdWithOrderItems(@Param("userId") Long userId);

    /**
     * Find order by ID with order items
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);

    /**
     * Find orders by status
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    /**
     * Find orders by payment status
     */
    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders by user ID and status
     */
    Page<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);

    /**
     * Find orders created between dates
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders with multiple filters for admin
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:userId IS NULL OR o.userId = :userId) AND " +
           "(:orderStatus IS NULL OR o.orderStatus = :orderStatus) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Order> findOrdersWithFilters(@Param("userId") Long userId,
                                     @Param("orderStatus") OrderStatus orderStatus,
                                     @Param("paymentStatus") PaymentStatus paymentStatus,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    /**
     * Find orders with multiple filters and search for admin
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:userId IS NULL OR o.userId = :userId) AND " +
           "(:orderStatus IS NULL OR o.orderStatus = :orderStatus) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.transactionId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> findOrdersWithFiltersAndSearch(@Param("userId") Long userId,
                                              @Param("orderStatus") OrderStatus orderStatus,
                                              @Param("paymentStatus") PaymentStatus paymentStatus,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("search") String search,
                                              Pageable pageable);

    /**
     * Count orders by user ID
     */
    int countByUserId(Long userId);

    /**
     * Count orders by status
     */
    int countByOrderStatus(OrderStatus orderStatus);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);
} 