package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDTO testOrderDTO;
    private CreateOrderDTO createOrderDTO;
    private PaginatedResponse<OrderDTO> paginatedResponse;

    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setOrderNumber("ORD-12345");
        testOrderDTO.setUserId(1L);
        testOrderDTO.setOrderStatus(OrderStatus.PENDING);
        testOrderDTO.setPaymentStatus(PaymentStatus.PENDING);
        testOrderDTO.setTotalAmount(new BigDecimal("100.00"));
        testOrderDTO.setTaxAmount(new BigDecimal("10.00"));
        testOrderDTO.setShippingAmount(new BigDecimal("5.00"));
        testOrderDTO.setDiscountAmount(new BigDecimal("0.00"));
        testOrderDTO.setFinalAmount(new BigDecimal("115.00"));
        testOrderDTO.setShippingAddress("123 Test St");
        testOrderDTO.setBillingAddress("123 Test St");
        testOrderDTO.setPhoneNumber("1234567890");
        testOrderDTO.setEmail("test@example.com");
        testOrderDTO.setPaymentMethod("Credit Card");
        testOrderDTO.setCreatedAt(LocalDateTime.now());

        createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setShippingAddress("123 Test St");
        createOrderDTO.setBillingAddress("123 Test St");
        createOrderDTO.setPhoneNumber("1234567890");
        createOrderDTO.setEmail("test@example.com");
        createOrderDTO.setPaymentMethod("Credit Card");
        createOrderDTO.setTaxAmount(new BigDecimal("10.00"));
        createOrderDTO.setShippingAmount(new BigDecimal("5.00"));
        createOrderDTO.setDiscountAmount(new BigDecimal("0.00"));

        paginatedResponse = new PaginatedResponse<>(
                Arrays.asList(testOrderDTO), 0, 10, 1L, 1, true, false, 1);
    }

    @Test
    void testCreateOrder() throws Exception {
        when(orderService.createOrderFromCart(anyLong(), any(CreateOrderDTO.class)))
                .thenReturn(testOrderDTO);

        mockMvc.perform(post("/api/orders/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.order.orderNumber").value("ORD-12345"));

        verify(orderService).createOrderFromCart(eq(1L), any(CreateOrderDTO.class));
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderById(anyLong(), anyLong())).thenReturn(testOrderDTO);

        mockMvc.perform(get("/api/orders/users/1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.order.id").value(1))
                .andExpect(jsonPath("$.order.orderNumber").value("ORD-12345"));

        verify(orderService).getOrderById(1L, 1L);
    }

    @Test
    void testGetOrderByNumber() throws Exception {
        when(orderService.getOrderByNumber(anyLong(), anyString())).thenReturn(testOrderDTO);

        mockMvc.perform(get("/api/orders/users/1/orders/number/ORD-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.order.orderNumber").value("ORD-12345"));

        verify(orderService).getOrderByNumber(1L, "ORD-12345");
    }

    @Test
    void testGetUserOrders() throws Exception {
        when(orderService.getUserOrders(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/orders/users/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders.content").isArray())
                .andExpect(jsonPath("$.orders.totalElements").value(1));

        verify(orderService).getUserOrders(eq(1L), eq(0), eq(10), anyString(), anyString());
    }

    @Test
    void testCancelOrder() throws Exception {
        testOrderDTO.setOrderStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(anyLong(), anyLong())).thenReturn(testOrderDTO);

        mockMvc.perform(put("/api/orders/users/1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.order.orderStatus").value("CANCELLED"));

        verify(orderService).cancelOrder(1L, 1L);
    }

    @Test
    void testCreateOrder_WithError() throws Exception {
        when(orderService.createOrderFromCart(anyLong(), any(CreateOrderDTO.class)))
                .thenThrow(new RuntimeException("Cart is empty"));

        mockMvc.perform(post("/api/orders/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cart is empty"));

        verify(orderService).createOrderFromCart(eq(1L), any(CreateOrderDTO.class));
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/orders/users/1/orders/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));

        verify(orderService).getOrderById(1L, 999L);
    }

    @Test
    void testGetOrderByNumber_NotFound() throws Exception {
        when(orderService.getOrderByNumber(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/orders/users/1/orders/number/ORD-99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));

        verify(orderService).getOrderByNumber(1L, "ORD-99999");
    }

    @Test
    void testCancelOrder_WithError() throws Exception {
        when(orderService.cancelOrder(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Order cannot be cancelled"));

        mockMvc.perform(put("/api/orders/users/1/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order cannot be cancelled"));

        verify(orderService).cancelOrder(1L, 1L);
    }

    @Test
    void testGetUserOrdersWithSorting() throws Exception {
        when(orderService.getUserOrders(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/orders/users/1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orders.content").isArray());

        verify(orderService).getUserOrders(eq(1L), eq(0), eq(10), eq("createdAt"), eq("desc"));
    }

    @Test
    void testGetUserOrders_WithError() throws Exception {
        when(orderService.getUserOrders(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/orders/users/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(orderService).getUserOrders(eq(1L), eq(0), eq(10), anyString(), anyString());
    }
}

