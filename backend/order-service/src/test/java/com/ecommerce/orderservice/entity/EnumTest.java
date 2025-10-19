package com.ecommerce.orderservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    @Test
    void testOrderStatusValues() {
        assertEquals(6, OrderStatus.values().length);
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
        assertEquals(OrderStatus.PROCESSING, OrderStatus.valueOf("PROCESSING"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.valueOf("SHIPPED"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.valueOf("DELIVERED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void testPaymentStatusValues() {
        assertEquals(4, PaymentStatus.values().length);
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.COMPLETED, PaymentStatus.valueOf("COMPLETED"));
        assertEquals(PaymentStatus.FAILED, PaymentStatus.valueOf("FAILED"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
    }

    @Test
    void testOrderStatusEnumProperties() {
        for (OrderStatus status : OrderStatus.values()) {
            assertNotNull(status.name());
            assertNotNull(status.toString());
        }
    }

    @Test
    void testPaymentStatusEnumProperties() {
        for (PaymentStatus status : PaymentStatus.values()) {
            assertNotNull(status.name());
            assertNotNull(status.toString());
        }
    }
}

