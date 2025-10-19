package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.*;
import com.ecommerce.orderservice.repository.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ProductServiceClient productServiceClient;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                       CartService cartService, ProductServiceClient productServiceClient,
                       ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.productServiceClient = productServiceClient;
        this.modelMapper = modelMapper;
    }

    /**
     * Create order from user's cart
     */
    public OrderDTO createOrderFromCart(Long userId, CreateOrderDTO createOrderDTO) {
        logger.info("Creating order from cart for user: {}", userId);

        // Validate cart
        if (!cartService.validateCartForCheckout(userId)) {
            throw new RuntimeException("Cart validation failed. Cannot proceed with order creation.");
        }

        // Get user's cart
        CartDTO cartDTO = cartService.getCartByUserId(userId);
        if (cartDTO.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot create order.");
        }

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(createOrderDTO.getShippingAddress());
        order.setBillingAddress(createOrderDTO.getBillingAddress() != null ? 
                               createOrderDTO.getBillingAddress() : createOrderDTO.getShippingAddress());
        order.setPhoneNumber(createOrderDTO.getPhoneNumber());
        order.setEmail(createOrderDTO.getEmail());
        order.setNotes(createOrderDTO.getNotes());
        order.setPaymentMethod(createOrderDTO.getPaymentMethod());
        order.setTaxAmount(createOrderDTO.getTaxAmount());
        order.setShippingAmount(createOrderDTO.getShippingAmount());
        order.setDiscountAmount(createOrderDTO.getDiscountAmount());
        order.setCouponCode(createOrderDTO.getCouponCode());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Calculate estimated delivery date (5-7 business days from now)
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(7));

        // Convert cart items to order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDTO cartItemDTO : cartDTO.getCartItems()) {
            // Get fresh product details
            ProductDTO product = productServiceClient.getProductById(cartItemDTO.getProductId());
            
            // Reserve stock
            if (!productServiceClient.reserveProductStock(cartItemDTO.getProductId(), cartItemDTO.getQuantity())) {
                throw new RuntimeException("Failed to reserve stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    cartItemDTO.getQuantity(),
                    product.getImageUrl(),
                    product.getCategory()
            );

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);
        order.calculateFinalAmount();

        // Save order
        order = orderRepository.save(order);
        logger.info("Order created successfully: {}", order.getOrderNumber());

        // Clear user's cart after successful order creation
        cartService.clearCart(userId);

        return convertToOrderDTO(order);
    }

    /**
     * Get order by ID for user
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long userId, Long orderId) {
        logger.info("Getting order {} for user {}", orderId, userId);

        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }

        return convertToOrderDTO(order);
    }

    /**
     * Get order by order number for user
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByNumber(Long userId, String orderNumber) {
        logger.info("Getting order {} for user {}", orderNumber, userId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }

        return convertToOrderDTO(order);
    }

    /**
     * Get user's orders with pagination
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderDTO> getUserOrders(Long userId, int page, int size, String sortBy, String sortDirection) {
        logger.info("Getting orders for user {} - page: {}, size: {}", userId, page, size);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                orderDTOs,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast(),
                orderPage.getNumberOfElements()
        );
    }

    /**
     * Cancel order (only if in PENDING or CONFIRMED status)
     */
    public OrderDTO cancelOrder(Long userId, Long orderId) {
        logger.info("Cancelling order {} for user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled at this stage: " + order.getOrderStatus());
        }

        // Release reserved stock
        for (OrderItem item : order.getOrderItems()) {
            productServiceClient.releaseProductStock(item.getProductId(), item.getQuantity());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order = orderRepository.save(order);

        logger.info("Order cancelled successfully: {}", order.getOrderNumber());
        return convertToOrderDTO(order);
    }

    // Admin methods

    /**
     * Get all orders with filters (Admin only)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderDTO> getAllOrdersForAdmin(Long userId, OrderStatus orderStatus, 
                                                           PaymentStatus paymentStatus, LocalDateTime startDate, 
                                                           LocalDateTime endDate, int page, int size, 
                                                           String sortBy, String sortDirection, String search) {
        logger.info("Getting orders for admin with filters - search: {}", search);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage = orderRepository.findOrdersWithFiltersAndSearch(
                userId, orderStatus, paymentStatus, startDate, endDate, search, pageable);

        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                orderDTOs,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast(),
                orderPage.getNumberOfElements()
        );
    }

    /**
     * Update order status (Admin only)
     */
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusDTO updateStatusDTO) {
        logger.info("Updating order status for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Update order status if provided
        if (updateStatusDTO.getOrderStatus() != null) {
            order.setOrderStatus(updateStatusDTO.getOrderStatus());
        }

        // Update payment status if provided
        if (updateStatusDTO.getPaymentStatus() != null) {
            order.setPaymentStatus(updateStatusDTO.getPaymentStatus());
        }

        // Update transaction ID if provided
        if (updateStatusDTO.getTransactionId() != null) {
            order.setTransactionId(updateStatusDTO.getTransactionId());
        }

        // Update delivery dates if provided
        if (updateStatusDTO.getEstimatedDeliveryDate() != null) {
            order.setEstimatedDeliveryDate(updateStatusDTO.getEstimatedDeliveryDate());
        }

        if (updateStatusDTO.getActualDeliveryDate() != null) {
            order.setActualDeliveryDate(updateStatusDTO.getActualDeliveryDate());
        }

        // Update notes if provided
        if (updateStatusDTO.getNotes() != null) {
            order.setNotes(updateStatusDTO.getNotes());
        }

        // Auto-set actual delivery date if status changed to DELIVERED
        if (updateStatusDTO.getOrderStatus() == OrderStatus.DELIVERED && order.getActualDeliveryDate() == null) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }

        order = orderRepository.save(order);
        logger.info("Order status updated successfully for: {}", order.getOrderNumber());

        return convertToOrderDTO(order);
    }

    /**
     * Get order by ID for admin (without user validation)
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByIdForAdmin(Long orderId) {
        logger.info("Admin getting order: {}", orderId);

        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        return convertToOrderDTO(order);
    }

    /**
     * Get order statistics (Admin only)
     */
    @Transactional(readOnly = true)
    public OrderStatisticsDTO getOrderStatistics() {
        logger.info("Getting order statistics");

        OrderStatisticsDTO stats = new OrderStatisticsDTO();
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByOrderStatus(OrderStatus.PENDING));
        stats.setProcessingOrders(orderRepository.countByOrderStatus(OrderStatus.PROCESSING));
        stats.setShippedOrders(orderRepository.countByOrderStatus(OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderRepository.countByOrderStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByOrderStatus(OrderStatus.CANCELLED));

        return stats;
    }

    // Helper methods

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        
        // Map order items
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList());
        
        orderDTO.setOrderItems(orderItemDTOs);
        orderDTO.setTotalItems(order.getTotalItems());
        
        return orderDTO;
    }

    // Inner class for statistics
    public static class OrderStatisticsDTO {
        private long totalOrders;
        private int pendingOrders;
        private int processingOrders;
        private int shippedOrders;
        private int deliveredOrders;
        private int cancelledOrders;

        // Getters and setters
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
        public int getProcessingOrders() { return processingOrders; }
        public void setProcessingOrders(int processingOrders) { this.processingOrders = processingOrders; }
        public int getShippedOrders() { return shippedOrders; }
        public void setShippedOrders(int shippedOrders) { this.shippedOrders = shippedOrders; }
        public int getDeliveredOrders() { return deliveredOrders; }
        public void setDeliveredOrders(int deliveredOrders) { this.deliveredOrders = deliveredOrders; }
        public int getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    }
} 