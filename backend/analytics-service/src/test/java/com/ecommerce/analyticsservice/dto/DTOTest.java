package com.ecommerce.analyticsservice.dto;

import com.ecommerce.analyticsservice.dto.external.OrderDTO;
import com.ecommerce.analyticsservice.dto.external.OrderItemDTO;
import com.ecommerce.analyticsservice.dto.external.PaginatedResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testSalesReportDTO() {
        SalesReportDTO dto = new SalesReportDTO();
        dto.setTotalRevenue(new BigDecimal("10000.00"));
        dto.setTotalOrders(100L);
        dto.setAverageOrderValue(new BigDecimal("100.00"));
        
        RevenueDataDTO revenueData = new RevenueDataDTO();
        dto.setRevenueByMonth(Arrays.asList(revenueData));
        
        TopProductDTO topProduct = new TopProductDTO();
        dto.setTopProducts(Arrays.asList(topProduct));
        
        OrderStatusDataDTO statusData = new OrderStatusDataDTO();
        dto.setOrdersByStatus(Arrays.asList(statusData));

        assertEquals(new BigDecimal("10000.00"), dto.getTotalRevenue());
        assertEquals(100L, dto.getTotalOrders());
        assertEquals(new BigDecimal("100.00"), dto.getAverageOrderValue());
        assertEquals(1, dto.getRevenueByMonth().size());
        assertEquals(1, dto.getTopProducts().size());
        assertEquals(1, dto.getOrdersByStatus().size());
    }

    @Test
    void testRevenueDataDTO() {
        RevenueDataDTO dto = new RevenueDataDTO();
        dto.setMonth("2024-01");
        dto.setRevenue(new BigDecimal("500.00"));
        dto.setOrders(5);

        assertEquals("2024-01", dto.getMonth());
        assertEquals(new BigDecimal("500.00"), dto.getRevenue());
        assertEquals(5, dto.getOrders());
    }

    @Test
    void testTopProductDTO() {
        TopProductDTO dto = new TopProductDTO();
        dto.setProductId("1");
        dto.setProductName("Product 1");
        dto.setTotalSales(new BigDecimal("1000.00"));
        dto.setUnitsSold(50);

        assertEquals("1", dto.getProductId());
        assertEquals("Product 1", dto.getProductName());
        assertEquals(new BigDecimal("1000.00"), dto.getTotalSales());
        assertEquals(50, dto.getUnitsSold());
    }

    @Test
    void testOrderStatusDataDTO() {
        OrderStatusDataDTO dto = new OrderStatusDataDTO();
        dto.setStatus("DELIVERED");
        dto.setCount(75);
        dto.setPercentage(75.0);

        assertEquals("DELIVERED", dto.getStatus());
        assertEquals(75, dto.getCount());
        assertEquals(75.0, dto.getPercentage());
    }

    @Test
    void testOrderDTO() {
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setOrderNumber("ORD-001");
        dto.setUserId(1L);
        dto.setOrderStatus("DELIVERED");
        dto.setTotalAmount(new BigDecimal("100.00"));
        dto.setFinalAmount(new BigDecimal("115.00"));
        dto.setCreatedAt(LocalDateTime.now());

        OrderItemDTO itemDTO = new OrderItemDTO();
        dto.setOrderItems(Arrays.asList(itemDTO));

        assertEquals(1L, dto.getId());
        assertEquals("ORD-001", dto.getOrderNumber());
        assertEquals(1L, dto.getUserId());
        assertEquals("DELIVERED", dto.getOrderStatus());
        assertEquals(1, dto.getOrderItems().size());
    }

    @Test
    void testOrderItemDTO() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(1L);
        dto.setProductId(1L);
        dto.setProductName("Product 1");
        dto.setQuantity(2);
        dto.setUnitPrice(new BigDecimal("50.00"));
        dto.setTotalPrice(new BigDecimal("100.00"));

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getProductId());
        assertEquals("Product 1", dto.getProductName());
        assertEquals(2, dto.getQuantity());
    }

    @Test
    void testPaginatedResponse() {
        PaginatedResponse<OrderDTO> response = new PaginatedResponse<>();
        response.setContent(Arrays.asList(new OrderDTO()));
        response.setPage(0);
        response.setSize(10);
        response.setTotalElements(1L);
        response.setTotalPages(1);
        response.setFirst(true);
        response.setLast(true);
        response.setNumberOfElements(1);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(1, response.getNumberOfElements());
    }
}

