package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Cart cart;
    private Order order;

    @BeforeEach
    void setUp() {
        // Set up test cart
        cart = new Cart(1L);
        cart = entityManager.persist(cart);

        // Set up test order
        order = new Order(1L, "ORD-TEST-001", new BigDecimal("100.00"), "123 Test St");
        order = entityManager.persist(order);

        entityManager.flush();
    }

    @Test
    void testCartRepository_FindByUserId() {
        Optional<Cart> found = cartRepository.findByUserId(1L);
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void testCartRepository_FindByUserIdWithCartItems() {
        CartItem item = new CartItem(100L, "Test Product", new BigDecimal("50.00"), 2, "http://test.url");
        cart.addCartItem(item);
        entityManager.persist(item);
        entityManager.flush();

        Optional<Cart> found = cartRepository.findByUserIdWithCartItems(1L);
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getCartItems().size());
    }

    @Test
    void testCartRepository_GetTotalItemsByUserId() {
        CartItem item1 = new CartItem(100L, "Product 1", new BigDecimal("50.00"), 2, "url");
        CartItem item2 = new CartItem(101L, "Product 2", new BigDecimal("30.00"), 3, "url");
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        Integer total = cartRepository.getTotalItemsByUserId(1L);
        assertEquals(5, total);
    }

    @Test
    void testCartItemRepository_FindByCartIdAndProductId() {
        CartItem item = new CartItem(100L, "Test Product", new BigDecimal("50.00"), 2, "url");
        cart.addCartItem(item);
        entityManager.persist(item);
        entityManager.flush();

        Optional<CartItem> found = cartItemRepository.findByCartIdAndProductId(cart.getId(), 100L);
        assertTrue(found.isPresent());
        assertEquals("Test Product", found.get().getProductName());
    }

    @Test
    void testOrderRepository_FindByUserId() {
        Page<Order> orders = orderRepository.findByUserId(1L, PageRequest.of(0, 10));
        assertNotNull(orders);
        assertEquals(1, orders.getTotalElements());
    }

    @Test
    void testOrderRepository_FindByOrderNumber() {
        Optional<Order> found = orderRepository.findByOrderNumber("ORD-TEST-001");
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void testOrderRepository_FindByIdWithOrderItems() {
        OrderItem item = new OrderItem(100L, "Product", "Desc", new BigDecimal("50.00"), 2, "url", "Category");
        order.addOrderItem(item);
        entityManager.persist(item);
        entityManager.flush();

        Optional<Order> found = orderRepository.findByIdWithOrderItems(order.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getOrderItems().size());
    }

    @Test
    void testOrderRepository_CountByUserId() {
        int count = orderRepository.countByUserId(1L);
        assertEquals(1, count);
    }

    @Test
    void testOrderRepository_FindByUserIdAndOrderStatus() {
        Page<Order> orders = orderRepository.findByUserIdAndOrderStatus(1L, OrderStatus.PENDING, PageRequest.of(0, 10));
        assertNotNull(orders);
        assertTrue(orders.getTotalElements() > 0);
    }

    @Test
    void testOrderRepository_FindByOrderStatus() {
        Page<Order> orders = orderRepository.findByOrderStatus(OrderStatus.PENDING, PageRequest.of(0, 10));
        assertNotNull(orders);
        assertTrue(orders.getTotalElements() > 0);
    }

    @Test
    void testOrderRepository_FindOrdersBetweenDates() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        
        Page<Order> orders = orderRepository.findOrdersBetweenDates(start, end, PageRequest.of(0, 10));
        assertNotNull(orders);
        assertTrue(orders.getTotalElements() > 0);
    }

    @Test
    void testOrderItemRepository_Save() {
        OrderItem item = new OrderItem(100L, "Product", "Description", new BigDecimal("50.00"), 2, "url", "Category");
        order.addOrderItem(item);
        
        OrderItem saved = orderItemRepository.save(item);
        
        assertNotNull(saved.getId());
        assertEquals("Product", saved.getProductName());
    }

    @Test
    void testOrderItemRepository_FindById() {
        OrderItem item = new OrderItem(100L, "Product", "Description", new BigDecimal("50.00"), 2, "url", "Category");
        order.addOrderItem(item);
        OrderItem saved = entityManager.persist(item);
        entityManager.flush();

        Optional<OrderItem> found = orderItemRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Product", found.get().getProductName());
    }
}

