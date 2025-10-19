package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create order from user's cart
     */
    @PostMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> createOrder(@PathVariable Long userId,
                                                          @Valid @RequestBody CreateOrderDTO createOrderDTO) {
        try {
            logger.info("Creating order for user: {}", userId);
            
            OrderDTO order = orderService.createOrderFromCart(userId, createOrderDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("order", order);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating order for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get order by ID for user
     */
    @GetMapping("/users/{userId}/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long userId,
                                                       @PathVariable Long orderId) {
        try {
            logger.info("Getting order {} for user {}", orderId, userId);
            
            OrderDTO order = orderService.getOrderById(userId, orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order retrieved successfully");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting order {} for user {}: {}", orderId, userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get order by order number for user
     */
    @GetMapping("/users/{userId}/orders/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(@PathVariable Long userId,
                                                              @PathVariable String orderNumber) {
        try {
            logger.info("Getting order {} for user {}", orderNumber, userId);
            
            OrderDTO order = orderService.getOrderByNumber(userId, orderNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order retrieved successfully");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting order {} for user {}: {}", orderNumber, userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get user's orders with pagination
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                                           @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            logger.info("Getting orders for user {} - page: {}, size: {}", userId, page, size);
            
            PaginatedResponse<OrderDTO> orders = orderService.getUserOrders(userId, page, size, sortBy, sortDirection);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orders retrieved successfully");
            response.put("orders", orders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting orders for user {}: {}", userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Cancel order
     */
    @PutMapping("/users/{userId}/orders/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long userId,
                                                          @PathVariable Long orderId) {
        try {
            logger.info("Cancelling order {} for user {}", orderId, userId);
            
            OrderDTO order = orderService.cancelOrder(userId, orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order cancelled successfully");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cancelling order {} for user {}: {}", orderId, userId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 