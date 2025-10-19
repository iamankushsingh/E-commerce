package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);
    private final OrderService orderService;

    @Autowired
    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Get all orders with filters and pagination (Admin only)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        try {
            logger.info("Admin getting all orders with filters - page: {}, size: {}, search: {}", page, size, search);
            
            PaginatedResponse<OrderDTO> orders = orderService.getAllOrdersForAdmin(
                    userId, orderStatus, paymentStatus, startDate, endDate, 
                    page, size, sortBy, sortDirection, search);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orders retrieved successfully");
            response.put("orders", orders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting orders for admin: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get specific order by ID (Admin only)
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long orderId) {
        try {
            logger.info("Admin getting order: {}", orderId);
            
            // Admin can get any order without user validation
            OrderDTO order = orderService.getOrderByIdForAdmin(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order retrieved successfully");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting order {} for admin: {}", orderId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long orderId,
                                                               @Valid @RequestBody UpdateOrderStatusDTO updateStatusDTO) {
        try {
            logger.info("Admin updating order status for order: {}", orderId);
            
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, updateStatusDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order status updated successfully");
            response.put("order", updatedOrder);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating order status for order {}: {}", orderId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get order statistics (Admin only)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        try {
            logger.info("Admin getting order statistics");
            
            OrderService.OrderStatisticsDTO statistics = orderService.getOrderStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order statistics retrieved successfully");
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting order statistics: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get orders by status (Admin only)
     */
    @GetMapping("/status/{orderStatus}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable OrderStatus orderStatus,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "createdAt") String sortBy,
                                                               @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            logger.info("Admin getting orders by status: {} - page: {}, size: {}", orderStatus, page, size);
            
            PaginatedResponse<OrderDTO> orders = orderService.getAllOrdersForAdmin(
                    null, orderStatus, null, null, null, 
                    page, size, sortBy, sortDirection, null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orders retrieved successfully");
            response.put("orders", orders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting orders by status {} for admin: {}", orderStatus, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Search orders by user ID (Admin only)
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUserId(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "createdAt") String sortBy,
                                                               @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            logger.info("Admin getting orders for user: {} - page: {}, size: {}", userId, page, size);
            
            PaginatedResponse<OrderDTO> orders = orderService.getAllOrdersForAdmin(
                    userId, null, null, null, null, 
                    page, size, sortBy, sortDirection, null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orders retrieved successfully");
            response.put("orders", orders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting orders for user {} (admin): {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 