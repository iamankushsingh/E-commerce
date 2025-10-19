package com.ecommerce.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
    }

    @Test
    void testParameterizedConstructor() {
        Order newOrder = new Order(1L, "ORD-123", new BigDecimal("100.00"), "123 Main St");

        assertEquals(1L, newOrder.getUserId());
        assertEquals("ORD-123", newOrder.getOrderNumber());
        assertEquals(new BigDecimal("100.00"), newOrder.getTotalAmount());
        assertEquals("123 Main St", newOrder.getShippingAddress());
        assertEquals(OrderStatus.PENDING, newOrder.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, newOrder.getPaymentStatus());
    }

    @Test
    void testCalculateFinalAmount() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setTaxAmount(new BigDecimal("10.00"));
        order.setShippingAmount(new BigDecimal("5.00"));
        order.setDiscountAmount(new BigDecimal("15.00"));

        order.calculateFinalAmount();

        assertEquals(new BigDecimal("100.00"), order.getFinalAmount());
    }

    @Test
    void testAddOrderItem() {
        OrderItem item = new OrderItem(1L, "Product", "Description", new BigDecimal("50.00"), 2, "url", "Category");
        order.addOrderItem(item);

        assertEquals(1, order.getOrderItems().size());
        assertEquals(order, item.getOrder());
        assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
    }

    @Test
    void testRemoveOrderItem() {
        OrderItem item = new OrderItem(1L, "Product", "Description", new BigDecimal("50.00"), 2, "url", "Category");
        order.addOrderItem(item);
        order.removeOrderItem(item);

        assertEquals(0, order.getOrderItems().size());
        assertNull(item.getOrder());
    }

    @Test
    void testGetTotalItems() {
        OrderItem item1 = new OrderItem(1L, "Product 1", "Desc", new BigDecimal("50.00"), 2, "url", "Cat");
        OrderItem item2 = new OrderItem(2L, "Product 2", "Desc", new BigDecimal("30.00"), 3, "url", "Cat");
        order.addOrderItem(item1);
        order.addOrderItem(item2);

        assertEquals(5, order.getTotalItems());
    }

    @Test
    void testCanBeCancelled_Pending() {
        order.setOrderStatus(OrderStatus.PENDING);
        assertTrue(order.canBeCancelled());
    }

    @Test
    void testCanBeCancelled_Confirmed() {
        order.setOrderStatus(OrderStatus.CONFIRMED);
        assertTrue(order.canBeCancelled());
    }

    @Test
    void testCanBeCancelled_Shipped() {
        order.setOrderStatus(OrderStatus.SHIPPED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    void testIsDelivered() {
        order.setOrderStatus(OrderStatus.DELIVERED);
        assertTrue(order.isDelivered());

        order.setOrderStatus(OrderStatus.PENDING);
        assertFalse(order.isDelivered());
    }

    @Test
    void testIsPaid() {
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        assertTrue(order.isPaid());

        order.setPaymentStatus(PaymentStatus.PENDING);
        assertFalse(order.isPaid());
    }

    @Test
    void testOnCreate() {
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setShippingAddress("Address");
        order.onCreate();

        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertNotNull(order.getOrderNumber());
        assertNotNull(order.getFinalAmount());
    }

    @Test
    void testOnUpdate() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.onUpdate();

        assertNotNull(order.getUpdatedAt());
        assertNotNull(order.getFinalAmount());
    }

    @Test
    void testAllOrderStatuses() {
        order.setOrderStatus(OrderStatus.PENDING);
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());

        order.setOrderStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());

        order.setOrderStatus(OrderStatus.PROCESSING);
        assertEquals(OrderStatus.PROCESSING, order.getOrderStatus());

        order.setOrderStatus(OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());

        order.setOrderStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());

        order.setOrderStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    void testAllPaymentStatuses() {
        order.setPaymentStatus(PaymentStatus.PENDING);
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        assertEquals(PaymentStatus.COMPLETED, order.getPaymentStatus());

        order.setPaymentStatus(PaymentStatus.FAILED);
        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        assertEquals(PaymentStatus.REFUNDED, order.getPaymentStatus());
    }

    @Test
    void testAllOrderSettersAndGetters() {
        order.setId(100L);
        order.setUserId(200L);
        order.setOrderNumber("TEST-001");
        order.setShippingAddress("Test Address");
        order.setBillingAddress("Billing Address");
        order.setPhoneNumber("1234567890");
        order.setEmail("test@test.com");
        order.setPaymentMethod("Card");
        order.setTransactionId("TXN-001");
        order.setCouponCode("COUPON10");
        order.setNotes("Test notes");
        
        assertEquals(100L, order.getId());
        assertEquals(200L, order.getUserId());
        assertEquals("TEST-001", order.getOrderNumber());
        assertEquals("Test Address", order.getShippingAddress());
        assertEquals("Billing Address", order.getBillingAddress());
        assertEquals("1234567890", order.getPhoneNumber());
        assertEquals("test@test.com", order.getEmail());
        assertEquals("Card", order.getPaymentMethod());
        assertEquals("TXN-001", order.getTransactionId());
        assertEquals("COUPON10", order.getCouponCode());
        assertEquals("Test notes", order.getNotes());
    }

    @Test
    void testCalculateFinalAmountWithNoDiscount() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setTaxAmount(new BigDecimal("10.00"));
        order.setShippingAmount(new BigDecimal("5.00"));
        order.setDiscountAmount(BigDecimal.ZERO);

        order.calculateFinalAmount();

        assertEquals(new BigDecimal("115.00"), order.getFinalAmount());
    }

    @Test
    void testCanBeCancelled_Processing() {
        order.setOrderStatus(OrderStatus.PROCESSING);
        assertTrue(order.canBeCancelled());
    }

    @Test
    void testCanBeCancelled_Cancelled() {
        order.setOrderStatus(OrderStatus.CANCELLED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    void testGetTotalItemsWithEmptyOrder() {
        assertEquals(0, order.getTotalItems());
    }

    @Test
    void testAddMultipleOrderItems() {
        OrderItem item1 = new OrderItem(1L, "Product 1", "Desc1", new BigDecimal("50.00"), 2, "url1", "Cat1");
        OrderItem item2 = new OrderItem(2L, "Product 2", "Desc2", new BigDecimal("30.00"), 3, "url2", "Cat2");
        OrderItem item3 = new OrderItem(3L, "Product 3", "Desc3", new BigDecimal("20.00"), 1, "url3", "Cat3");
        
        order.addOrderItem(item1);
        order.addOrderItem(item2);
        order.addOrderItem(item3);

        assertEquals(3, order.getOrderItems().size());
        assertEquals(new BigDecimal("210.00"), order.getTotalAmount());
        assertEquals(6, order.getTotalItems());
    }
}

