package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testOrderDTO() {
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setOrderNumber("ORD-001");
        dto.setOrderStatus(OrderStatus.PENDING);
        dto.setPaymentStatus(PaymentStatus.PENDING);
        dto.setTotalAmount(new BigDecimal("100.00"));
        dto.setTaxAmount(new BigDecimal("10.00"));
        dto.setShippingAmount(new BigDecimal("5.00"));
        dto.setDiscountAmount(new BigDecimal("0.00"));
        dto.setFinalAmount(new BigDecimal("115.00"));
        dto.setShippingAddress("123 Main St");
        dto.setBillingAddress("123 Main St");
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setPaymentMethod("Credit Card");
        dto.setTransactionId("TXN123");
        dto.setCouponCode("SAVE10");
        dto.setNotes("Test notes");
        dto.setEstimatedDeliveryDate(LocalDateTime.now());
        dto.setActualDeliveryDate(LocalDateTime.now());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setTotalItems(2);

        OrderItemDTO itemDTO = new OrderItemDTO();
        dto.setOrderItems(Arrays.asList(itemDTO));

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getUserId());
        assertEquals("ORD-001", dto.getOrderNumber());
        assertEquals(OrderStatus.PENDING, dto.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, dto.getPaymentStatus());
        assertEquals(new BigDecimal("100.00"), dto.getTotalAmount());
        assertEquals(new BigDecimal("10.00"), dto.getTaxAmount());
        assertEquals(new BigDecimal("5.00"), dto.getShippingAmount());
        assertEquals(new BigDecimal("0.00"), dto.getDiscountAmount());
        assertEquals(new BigDecimal("115.00"), dto.getFinalAmount());
        assertEquals("123 Main St", dto.getShippingAddress());
        assertEquals("123 Main St", dto.getBillingAddress());
        assertEquals("1234567890", dto.getPhoneNumber());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Credit Card", dto.getPaymentMethod());
        assertEquals("TXN123", dto.getTransactionId());
        assertEquals("SAVE10", dto.getCouponCode());
        assertEquals("Test notes", dto.getNotes());
        assertNotNull(dto.getEstimatedDeliveryDate());
        assertNotNull(dto.getActualDeliveryDate());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertEquals(2, dto.getTotalItems());
        assertEquals(1, dto.getOrderItems().size());
        assertNotNull(dto.toString());
    }

    @Test
    void testOrderItemDTO() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(1L);
        dto.setProductId(1L);
        dto.setProductName("Product 1");
        dto.setProductDescription("Description");
        dto.setUnitPrice(new BigDecimal("50.00"));
        dto.setQuantity(2);
        dto.setTotalPrice(new BigDecimal("100.00"));
        dto.setProductImageUrl("http://image.url");
        dto.setProductCategory("Electronics");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getProductId());
        assertEquals("Product 1", dto.getProductName());
        assertEquals("Description", dto.getProductDescription());
        assertEquals(new BigDecimal("50.00"), dto.getUnitPrice());
        assertEquals(2, dto.getQuantity());
        assertEquals(new BigDecimal("100.00"), dto.getTotalPrice());
        assertEquals("http://image.url", dto.getProductImageUrl());
        assertEquals("Electronics", dto.getProductCategory());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertNotNull(dto.toString());
    }
    
    @Test
    void testOrderItemDTOConstructor() {
        LocalDateTime now = LocalDateTime.now();
        OrderItemDTO dto = new OrderItemDTO(1L, 100L, "Test Product", "Test Description",
                new BigDecimal("50.00"), 2, new BigDecimal("100.00"), 
                "http://image.url", "Electronics", now, now);
        
        assertEquals(1L, dto.getId());
        assertEquals(100L, dto.getProductId());
        assertEquals("Test Product", dto.getProductName());
        assertEquals("Test Description", dto.getProductDescription());
        assertEquals(new BigDecimal("50.00"), dto.getUnitPrice());
        assertEquals(2, dto.getQuantity());
        assertEquals(new BigDecimal("100.00"), dto.getTotalPrice());
        assertEquals("http://image.url", dto.getProductImageUrl());
        assertEquals("Electronics", dto.getProductCategory());
    }

    @Test
    void testCreateOrderDTO() {
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setShippingAddress("123 Main St");
        dto.setBillingAddress("123 Main St");
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setPaymentMethod("Credit Card");
        dto.setTaxAmount(new BigDecimal("10.00"));
        dto.setShippingAmount(new BigDecimal("5.00"));
        dto.setDiscountAmount(new BigDecimal("0.00"));
        dto.setCouponCode("SAVE10");
        dto.setNotes("Test notes");

        assertEquals("123 Main St", dto.getShippingAddress());
        assertEquals("123 Main St", dto.getBillingAddress());
        assertEquals("1234567890", dto.getPhoneNumber());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Credit Card", dto.getPaymentMethod());
        assertEquals(new BigDecimal("10.00"), dto.getTaxAmount());
        assertEquals(new BigDecimal("5.00"), dto.getShippingAmount());
        assertEquals(new BigDecimal("0.00"), dto.getDiscountAmount());
        assertEquals("SAVE10", dto.getCouponCode());
        assertEquals("Test notes", dto.getNotes());
    }

    @Test
    void testUpdateOrderStatusDTO() {
        UpdateOrderStatusDTO dto = new UpdateOrderStatusDTO();
        dto.setOrderStatus(OrderStatus.SHIPPED);
        dto.setPaymentStatus(PaymentStatus.COMPLETED);
        dto.setTransactionId("TXN123");
        dto.setNotes("Shipped");
        dto.setEstimatedDeliveryDate(LocalDateTime.now());
        dto.setActualDeliveryDate(LocalDateTime.now());

        assertEquals(OrderStatus.SHIPPED, dto.getOrderStatus());
        assertEquals(PaymentStatus.COMPLETED, dto.getPaymentStatus());
        assertEquals("TXN123", dto.getTransactionId());
        assertEquals("Shipped", dto.getNotes());
        assertNotNull(dto.getEstimatedDeliveryDate());
        assertNotNull(dto.getActualDeliveryDate());
        assertNotNull(dto.toString());
    }
    
    @Test
    void testUpdateOrderStatusDTOConstructor() {
        UpdateOrderStatusDTO dto = new UpdateOrderStatusDTO(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, dto.getOrderStatus());
    }

    @Test
    void testCartDTO() {
        CartDTO dto = new CartDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setTotalAmount(new BigDecimal("100.00"));
        dto.setTotalItems(2);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        CartItemDTO itemDTO = new CartItemDTO();
        dto.setCartItems(Arrays.asList(itemDTO));

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getUserId());
        assertEquals(new BigDecimal("100.00"), dto.getTotalAmount());
        assertEquals(2, dto.getTotalItems());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertEquals(1, dto.getCartItems().size());
    }

    @Test
    void testCartItemDTO() {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(1L);
        dto.setProductId(1L);
        dto.setProductName("Product 1");
        dto.setQuantity(2);
        dto.setUnitPrice(new BigDecimal("50.00"));
        dto.setTotalPrice(new BigDecimal("100.00"));
        dto.setProductImageUrl("http://image.url");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getProductId());
        assertEquals("Product 1", dto.getProductName());
        assertEquals(2, dto.getQuantity());
        assertEquals(new BigDecimal("50.00"), dto.getUnitPrice());
        assertEquals(new BigDecimal("100.00"), dto.getTotalPrice());
        assertEquals("http://image.url", dto.getProductImageUrl());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }

    @Test
    void testAddToCartDTO() {
        AddToCartDTO dto = new AddToCartDTO();
        dto.setProductId(1L);
        dto.setQuantity(2);

        assertEquals(1L, dto.getProductId());
        assertEquals(2, dto.getQuantity());
    }

    @Test
    void testUpdateCartItemDTO() {
        UpdateCartItemDTO dto = new UpdateCartItemDTO();
        dto.setQuantity(5);

        assertEquals(5, dto.getQuantity());
    }

    @Test
    void testProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setName("Product 1");
        dto.setDescription("Description");
        dto.setPrice(new BigDecimal("50.00"));
        dto.setCategory("Electronics");
        dto.setStock(100);
        dto.setImageUrl("http://image.url");
        dto.setStatus("ACTIVE");

        assertEquals(1L, dto.getId());
        assertEquals("Product 1", dto.getName());
        assertEquals(100, dto.getStock());
    }

    @Test
    void testPaginatedResponse() {
        OrderDTO orderDTO = new OrderDTO();
        PaginatedResponse<OrderDTO> response = new PaginatedResponse<>(
                Arrays.asList(orderDTO), 0, 10, 1L, 1, true, false, 1);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(1, response.getNumberOfElements());
    }

    @Test
    void testPaginatedResponseSetters() {
        PaginatedResponse<OrderDTO> response = new PaginatedResponse<>();
        response.setContent(Arrays.asList(new OrderDTO()));
        response.setPage(1);
        response.setSize(20);
        response.setTotalElements(50L);
        response.setTotalPages(3);
        response.setFirst(false);
        response.setLast(false);
        response.setNumberOfElements(20);

        assertEquals(1, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(50L, response.getTotalElements());
    }
}

