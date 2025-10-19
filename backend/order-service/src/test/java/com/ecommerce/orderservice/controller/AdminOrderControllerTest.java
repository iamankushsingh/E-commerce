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

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDTO testOrderDTO;
    private UpdateOrderStatusDTO updateOrderStatusDTO;
    private PaginatedResponse<OrderDTO> paginatedResponse;
    private OrderService.OrderStatisticsDTO statistics;

    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setOrderNumber("ORD-12345");
        testOrderDTO.setUserId(1L);
        testOrderDTO.setOrderStatus(OrderStatus.PENDING);
        testOrderDTO.setPaymentStatus(PaymentStatus.PENDING);
        testOrderDTO.setTotalAmount(new BigDecimal("100.00"));
        testOrderDTO.setCreatedAt(LocalDateTime.now());

        updateOrderStatusDTO = new UpdateOrderStatusDTO();
        updateOrderStatusDTO.setOrderStatus(OrderStatus.SHIPPED);
        updateOrderStatusDTO.setPaymentStatus(PaymentStatus.COMPLETED);
        updateOrderStatusDTO.setTransactionId("TXN123");

        paginatedResponse = new PaginatedResponse<>(
                Arrays.asList(testOrderDTO), 0, 10, 1L, 1, true, false, 1);

        statistics = new OrderService.OrderStatisticsDTO();
        statistics.setTotalOrders(100L);
        statistics.setPendingOrders(10);
        statistics.setProcessingOrders(20);
        statistics.setShippedOrders(30);
        statistics.setDeliveredOrders(35);
        statistics.setCancelledOrders(5);
    }

    @Test
    void testGetAllOrders() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(orderService).getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), eq(0), eq(10), anyString(), anyString(), any());
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderByIdForAdmin(anyLong())).thenReturn(testOrderDTO);

        mockMvc.perform(get("/api/admin/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));

        verify(orderService).getOrderByIdForAdmin(1L);
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        testOrderDTO.setOrderStatus(OrderStatus.SHIPPED);
        testOrderDTO.setPaymentStatus(PaymentStatus.COMPLETED);
        when(orderService.updateOrderStatus(anyLong(), any(UpdateOrderStatusDTO.class)))
                .thenReturn(testOrderDTO);

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOrderStatusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));

        verify(orderService).updateOrderStatus(eq(1L), any(UpdateOrderStatusDTO.class));
    }

    @Test
    void testGetOrderStatistics() throws Exception {
        when(orderService.getOrderStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/admin/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(100))
                .andExpect(jsonPath("$.pendingOrders").value(10))
                .andExpect(jsonPath("$.processingOrders").value(20));

        verify(orderService).getOrderStatistics();
    }

    @Test
    void testGetAllOrdersWithFilters() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderStatus", "PENDING")
                        .param("paymentStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(orderService).getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), eq(0), eq(10), anyString(), anyString(), any());
    }

    @Test
    void testUpdateOrderStatus_WithError() throws Exception {
        when(orderService.updateOrderStatus(anyLong(), any(UpdateOrderStatusDTO.class)))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(put("/api/admin/orders/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOrderStatusDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));

        verify(orderService).updateOrderStatus(eq(999L), any(UpdateOrderStatusDTO.class));
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/orders/status/PENDING")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").exists());

        verify(orderService).getAllOrdersForAdmin(
                eq(null), eq(OrderStatus.PENDING), eq(null), eq(null), eq(null), 
                eq(0), eq(10), eq("createdAt"), eq("desc"), eq(null));
    }

    @Test
    void testGetOrdersByStatus_WithError() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/admin/orders/status/PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"));
    }

    @Test
    void testGetOrdersByUserId() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/orders/users/1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").exists());

        verify(orderService).getAllOrdersForAdmin(
                eq(1L), eq(null), eq(null), eq(null), eq(null), 
                eq(0), eq(10), eq("createdAt"), eq("desc"), eq(null));
    }

    @Test
    void testGetOrdersByUserId_WithError() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/admin/orders/users/999")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testGetAllOrders_WithError() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/api/admin/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Service unavailable"));
    }

    @Test
    void testGetOrderById_WithError() throws Exception {
        when(orderService.getOrderByIdForAdmin(anyLong()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/admin/orders/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));

        verify(orderService).getOrderByIdForAdmin(999L);
    }

    @Test
    void testGetOrderStatistics_WithError() throws Exception {
        when(orderService.getOrderStatistics())
                .thenThrow(new RuntimeException("Statistics calculation error"));

        mockMvc.perform(get("/api/admin/orders/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Statistics calculation error"));

        verify(orderService).getOrderStatistics();
    }

    @Test
    void testGetAllOrders_SuccessResponse() throws Exception {
        when(orderService.getAllOrdersForAdmin(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/admin/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").exists());
    }

    @Test
    void testGetOrderById_SuccessResponse() throws Exception {
        when(orderService.getOrderByIdForAdmin(anyLong())).thenReturn(testOrderDTO);

        mockMvc.perform(get("/api/admin/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.order").exists());
    }

    @Test
    void testUpdateOrderStatus_SuccessResponse() throws Exception {
        when(orderService.updateOrderStatus(anyLong(), any(UpdateOrderStatusDTO.class)))
                .thenReturn(testOrderDTO);

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOrderStatusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"))
                .andExpect(jsonPath("$.order").exists());
    }

    @Test
    void testGetOrderStatistics_SuccessResponse() throws Exception {
        when(orderService.getOrderStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/admin/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order statistics retrieved successfully"))
                .andExpect(jsonPath("$.statistics").exists());
    }
}

