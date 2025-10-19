package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.*;
import com.ecommerce.orderservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private CartDTO testCartDTO;
    private ProductDTO testProductDTO;
    private CreateOrderDTO createOrderDTO;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setOrderNumber("ORD-123");
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setFinalAmount(new BigDecimal("110.00"));
        testOrder.setShippingAddress("123 Main St");
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setPaymentStatus(PaymentStatus.PENDING);

        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setOrderNumber("ORD-123");
        testOrderDTO.setTotalAmount(new BigDecimal("100.00"));

        testCartDTO = new CartDTO();
        testCartDTO.setId(1L);
        testCartDTO.setUserId(1L);
        
        CartItemDTO cartItem = new CartItemDTO();
        cartItem.setProductId(1L);
        cartItem.setProductName("Test Product");
        cartItem.setQuantity(2);
        testCartDTO.setCartItems(Arrays.asList(cartItem));

        testProductDTO = new ProductDTO();
        testProductDTO.setId(1L);
        testProductDTO.setName("Test Product");
        testProductDTO.setPrice(new BigDecimal("50.00"));
        testProductDTO.setImageUrl("http://example.com/image.jpg");
        testProductDTO.setCategory("Electronics");
        testProductDTO.setDescription("Test Description");

        createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setShippingAddress("123 Main St");
        createOrderDTO.setPhoneNumber("+1234567890");
        createOrderDTO.setEmail("test@example.com");
        createOrderDTO.setPaymentMethod("Credit Card");
        createOrderDTO.setTaxAmount(new BigDecimal("10.00"));
        createOrderDTO.setShippingAmount(new BigDecimal("5.00"));
        createOrderDTO.setDiscountAmount(BigDecimal.ZERO);
    }

    @Test
    void testCreateOrderFromCart_Success() {
        when(cartService.validateCartForCheckout(1L)).thenReturn(true);
        when(cartService.getCartByUserId(1L)).thenReturn(testCartDTO);
        when(productServiceClient.getProductById(1L)).thenReturn(testProductDTO);
        when(productServiceClient.reserveProductStock(anyLong(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());
        doNothing().when(cartService).clearCart(1L);

        OrderDTO result = orderService.createOrderFromCart(1L, createOrderDTO);

        assertNotNull(result);
        verify(cartService).validateCartForCheckout(1L);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(1L);
    }

    @Test
    void testCreateOrderFromCart_ValidationFailed() {
        when(cartService.validateCartForCheckout(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(1L, createOrderDTO);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderFromCart_EmptyCart() {
        when(cartService.validateCartForCheckout(1L)).thenReturn(true);
        testCartDTO.setCartItems(Arrays.asList());
        when(cartService.getCartByUserId(1L)).thenReturn(testCartDTO);

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(1L, createOrderDTO);
        });
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findByIdWithOrderItems(1L)).thenReturn(Optional.of(testOrder));
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());

        OrderDTO result = orderService.getOrderById(1L, 1L);

        assertNotNull(result);
        assertEquals(testOrderDTO.getId(), result.getId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findByIdWithOrderItems(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(1L, 1L);
        });
    }

    @Test
    void testGetOrderById_WrongUser() {
        testOrder.setUserId(2L);
        when(orderRepository.findByIdWithOrderItems(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(1L, 1L);
        });
    }

    @Test
    void testGetOrderByNumber_Success() {
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(testOrder));
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());

        OrderDTO result = orderService.getOrderByNumber(1L, "ORD-123");

        assertNotNull(result);
    }

    @Test
    void testGetUserOrders_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);
        
        when(orderRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(orderPage);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());

        PaginatedResponse<OrderDTO> result = orderService.getUserOrders(1L, 0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testCancelOrder_Success() {
        testOrder.setOrderStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doNothing().when(productServiceClient).releaseProductStock(anyLong(), anyInt());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());

        OrderDTO result = orderService.cancelOrder(1L, 1L);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCancelOrder_CannotCancel() {
        testOrder.setOrderStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
    }

    @Test
    void testUpdateOrderStatus_Success() {
        UpdateOrderStatusDTO updateDTO = new UpdateOrderStatusDTO();
        updateDTO.setOrderStatus(OrderStatus.SHIPPED);
        updateDTO.setPaymentStatus(PaymentStatus.COMPLETED);
        updateDTO.setTransactionId("TXN-123");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());

        OrderDTO result = orderService.updateOrderStatus(1L, updateDTO);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetOrderStatistics() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByOrderStatus(OrderStatus.PENDING)).thenReturn(10);
        when(orderRepository.countByOrderStatus(OrderStatus.PROCESSING)).thenReturn(20);
        when(orderRepository.countByOrderStatus(OrderStatus.SHIPPED)).thenReturn(30);
        when(orderRepository.countByOrderStatus(OrderStatus.DELIVERED)).thenReturn(35);
        when(orderRepository.countByOrderStatus(OrderStatus.CANCELLED)).thenReturn(5);

        OrderService.OrderStatisticsDTO stats = orderService.getOrderStatistics();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalOrders());
        assertEquals(10, stats.getPendingOrders());
        assertEquals(20, stats.getProcessingOrders());
        assertEquals(30, stats.getShippedOrders());
        assertEquals(35, stats.getDeliveredOrders());
        assertEquals(5, stats.getCancelledOrders());
    }

    @Test
    void testGetAllOrdersForAdmin_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);
        
        when(orderRepository.findOrdersWithFiltersAndSearch(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(orderPage);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(new OrderItemDTO());

        PaginatedResponse<OrderDTO> result = orderService.getAllOrdersForAdmin(
                null, null, null, null, null, 0, 10, "createdAt", "desc", null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void testCountByUserId() {
        when(orderRepository.countByUserId(1L)).thenReturn(5);

        int count = orderRepository.countByUserId(1L);

        assertEquals(5, count);
    }

    @Test
    void testCreateOrderFromCart_StockReservationFailure() {
        when(cartService.validateCartForCheckout(1L)).thenReturn(true);
        when(cartService.getCartByUserId(1L)).thenReturn(testCartDTO);
        when(productServiceClient.getProductById(1L)).thenReturn(testProductDTO);
        when(productServiceClient.reserveProductStock(anyLong(), anyInt())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(1L, createOrderDTO);
        });
    }

    @Test
    void testUpdateOrderStatus_NotFound() {
        UpdateOrderStatusDTO updateDTO = new UpdateOrderStatusDTO();
        updateDTO.setOrderStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus(1L, updateDTO);
        });
    }

    @Test
    void testCancelOrder_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
    }

    @Test
    void testGetOrderByNumber_NotFound() {
        when(orderRepository.findByOrderNumber("ORD-999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByNumber(1L, "ORD-999");
        });
    }

    @Test
    void testGetOrderByNumber_WrongUser() {
        testOrder.setUserId(2L);
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByNumber(1L, "ORD-123");
        });
    }
}

