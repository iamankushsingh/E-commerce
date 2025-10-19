package com.ecommerce.analyticsservice.service;

import com.ecommerce.analyticsservice.client.OrderServiceClient;
import com.ecommerce.analyticsservice.client.ProductServiceClient;
import com.ecommerce.analyticsservice.client.UserServiceClient;
import com.ecommerce.analyticsservice.dto.*;
import com.ecommerce.analyticsservice.dto.external.OrderDTO;
import com.ecommerce.analyticsservice.dto.external.OrderItemDTO;
import com.ecommerce.analyticsservice.dto.external.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<OrderDTO> testOrders;
    private PaginatedResponse<OrderDTO> paginatedResponse;

    @BeforeEach
    void setUp() {
        // Create test orders
        OrderDTO order1 = new OrderDTO();
        order1.setId(1L);
        order1.setOrderNumber("ORD-001");
        order1.setTotalAmount(new BigDecimal("100.00"));
        order1.setFinalAmount(new BigDecimal("110.00"));
        order1.setOrderStatus("DELIVERED");
        order1.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

        OrderItemDTO item1 = new OrderItemDTO();
        item1.setProductId(1L);
        item1.setProductName("Product 1");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setTotalPrice(new BigDecimal("100.00"));
        order1.setOrderItems(Arrays.asList(item1));

        OrderDTO order2 = new OrderDTO();
        order2.setId(2L);
        order2.setOrderNumber("ORD-002");
        order2.setTotalAmount(new BigDecimal("200.00"));
        order2.setFinalAmount(new BigDecimal("220.00"));
        order2.setOrderStatus("PENDING");
        order2.setCreatedAt(LocalDateTime.of(2024, 2, 20, 14, 30));

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setProductId(2L);
        item2.setProductName("Product 2");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("200.00"));
        item2.setTotalPrice(new BigDecimal("200.00"));
        order2.setOrderItems(Arrays.asList(item2));

        OrderDTO order3 = new OrderDTO();
        order3.setId(3L);
        order3.setOrderNumber("ORD-003");
        order3.setTotalAmount(new BigDecimal("150.00"));
        order3.setFinalAmount(new BigDecimal("165.00"));
        order3.setOrderStatus("CANCELLED");
        order3.setCreatedAt(LocalDateTime.of(2024, 3, 10, 9, 15));

        OrderItemDTO item3 = new OrderItemDTO();
        item3.setProductId(1L);
        item3.setProductName("Product 1");
        item3.setQuantity(3);
        item3.setUnitPrice(new BigDecimal("50.00"));
        item3.setTotalPrice(new BigDecimal("150.00"));
        order3.setOrderItems(Arrays.asList(item3));

        testOrders = Arrays.asList(order1, order2, order3);

        paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setContent(testOrders);
        paginatedResponse.setPage(0);
        paginatedResponse.setSize(100);
        paginatedResponse.setTotalElements(3L);
        paginatedResponse.setTotalPages(1);
        paginatedResponse.setFirst(true);
        paginatedResponse.setLast(true);
        paginatedResponse.setNumberOfElements(3);
    }

    @Test
    void testGenerateSalesReport_Success() {
        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(paginatedResponse));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report);
                    // Total revenue should exclude cancelled orders
                    assertEquals(new BigDecimal("330.00"), report.getTotalRevenue());
                    assertEquals(2L, report.getTotalOrders());
                    assertEquals(new BigDecimal("165.00"), report.getAverageOrderValue());
                    assertNotNull(report.getTopProducts());
                    assertNotNull(report.getRevenueByMonth());
                    assertNotNull(report.getOrdersByStatus());
                })
                .verifyComplete();
    }

    @Test
    void testGenerateSalesReport_EmptyOrders() {
        PaginatedResponse<OrderDTO> emptyResponse = new PaginatedResponse<>();
        emptyResponse.setContent(Collections.emptyList());
        emptyResponse.setPage(0);
        emptyResponse.setSize(100);
        emptyResponse.setTotalElements(0L);
        emptyResponse.setTotalPages(0);
        emptyResponse.setFirst(true);
        emptyResponse.setLast(true);
        emptyResponse.setNumberOfElements(0);

        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(emptyResponse));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report);
                    assertEquals(BigDecimal.ZERO, report.getTotalRevenue());
                    assertEquals(0L, report.getTotalOrders());
                    assertEquals(BigDecimal.ZERO, report.getAverageOrderValue());
                })
                .verifyComplete();
    }

    @Test
    void testGenerateSalesReport_MultiplePages() {
        // First page
        PaginatedResponse<OrderDTO> page1 = new PaginatedResponse<>();
        page1.setContent(Arrays.asList(testOrders.get(0)));
        page1.setPage(0);
        page1.setSize(100);
        page1.setTotalElements(3L);
        page1.setTotalPages(2);
        page1.setFirst(true);
        page1.setLast(false);
        page1.setNumberOfElements(1);

        // Second page
        PaginatedResponse<OrderDTO> page2 = new PaginatedResponse<>();
        page2.setContent(Arrays.asList(testOrders.get(1), testOrders.get(2)));
        page2.setPage(1);
        page2.setSize(100);
        page2.setTotalElements(3L);
        page2.setTotalPages(2);
        page2.setFirst(false);
        page2.setLast(true);
        page2.setNumberOfElements(2);

        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(page1));
        when(orderServiceClient.getAllOrdersForAdmin(1, 100))
                .thenReturn(Mono.just(page2));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report);
                    assertEquals(2L, report.getTotalOrders());
                })
                .verifyComplete();

        verify(orderServiceClient).getAllOrdersForAdmin(0, 100);
        verify(orderServiceClient).getAllOrdersForAdmin(1, 100);
    }

    @Test
    void testGenerateSalesReport_WithError() {
        when(orderServiceClient.getAllOrdersForAdmin(anyInt(), anyInt()))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testGenerateSalesReport_TopProducts() {
        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(paginatedResponse));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report.getTopProducts());
                    assertFalse(report.getTopProducts().isEmpty());
                    // Product 2 should be top with 200.00
                    TopProductDTO topProduct = report.getTopProducts().get(0);
                    assertEquals("Product 2", topProduct.getProductName());
                    assertEquals(new BigDecimal("200.00"), topProduct.getTotalSales());
                })
                .verifyComplete();
    }

    @Test
    void testGenerateSalesReport_RevenueByMonth() {
        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(paginatedResponse));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report.getRevenueByMonth());
                    assertFalse(report.getRevenueByMonth().isEmpty());
                    // Should have January and February data (excluding cancelled March order)
                    assertTrue(report.getRevenueByMonth().size() >= 2);
                })
                .verifyComplete();
    }

    @Test
    void testGenerateSalesReport_OrdersByStatus() {
        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(paginatedResponse));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report.getOrdersByStatus());
                    assertFalse(report.getOrdersByStatus().isEmpty());
                    
                    // Check for different statuses
                    List<OrderStatusDataDTO> statusData = report.getOrdersByStatus();
                    assertTrue(statusData.stream().anyMatch(s -> "delivered".equals(s.getStatus())));
                    assertTrue(statusData.stream().anyMatch(s -> "pending".equals(s.getStatus())));
                    assertTrue(statusData.stream().anyMatch(s -> "cancelled".equals(s.getStatus())));
                })
                .verifyComplete();
    }

    @Test
    void testGenerateSalesReport_NullOrderItems() {
        OrderDTO orderWithoutItems = new OrderDTO();
        orderWithoutItems.setId(4L);
        orderWithoutItems.setOrderNumber("ORD-004");
        orderWithoutItems.setTotalAmount(new BigDecimal("50.00"));
        orderWithoutItems.setFinalAmount(new BigDecimal("55.00"));
        orderWithoutItems.setOrderStatus("DELIVERED");
        orderWithoutItems.setCreatedAt(LocalDateTime.now());
        orderWithoutItems.setOrderItems(null);

        PaginatedResponse<OrderDTO> response = new PaginatedResponse<>();
        response.setContent(Arrays.asList(orderWithoutItems));
        response.setPage(0);
        response.setSize(100);
        response.setTotalElements(1L);
        response.setTotalPages(1);
        response.setFirst(true);
        response.setLast(true);
        response.setNumberOfElements(1);

        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(response));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report);
                    assertEquals(new BigDecimal("55.00"), report.getTotalRevenue());
                })
                .verifyComplete();
    }

    @Test
    void testGenerateSalesReport_AllStatusTypes() {
        OrderDTO confirmedOrder = new OrderDTO();
        confirmedOrder.setOrderStatus("CONFIRMED");
        confirmedOrder.setTotalAmount(new BigDecimal("100.00"));
        confirmedOrder.setCreatedAt(LocalDateTime.now());

        OrderDTO processingOrder = new OrderDTO();
        processingOrder.setOrderStatus("PROCESSING");
        processingOrder.setTotalAmount(new BigDecimal("200.00"));
        processingOrder.setCreatedAt(LocalDateTime.now());

        OrderDTO shippedOrder = new OrderDTO();
        shippedOrder.setOrderStatus("SHIPPED");
        shippedOrder.setTotalAmount(new BigDecimal("150.00"));
        shippedOrder.setCreatedAt(LocalDateTime.now());

        PaginatedResponse<OrderDTO> response = new PaginatedResponse<>();
        response.setContent(Arrays.asList(confirmedOrder, processingOrder, shippedOrder));
        response.setPage(0);
        response.setSize(100);
        response.setTotalElements(3L);
        response.setTotalPages(1);
        response.setFirst(true);
        response.setLast(true);
        response.setNumberOfElements(3);

        when(orderServiceClient.getAllOrdersForAdmin(0, 100))
                .thenReturn(Mono.just(response));

        Mono<SalesReportDTO> result = analyticsService.generateSalesReport();

        StepVerifier.create(result)
                .assertNext(report -> {
                    assertNotNull(report);
                    assertEquals(3L, report.getTotalOrders());
                    
                    List<OrderStatusDataDTO> statusData = report.getOrdersByStatus();
                    assertNotNull(statusData);
                    assertTrue(statusData.stream().anyMatch(s -> "processing".equals(s.getStatus())));
                    assertTrue(statusData.stream().anyMatch(s -> "shipped".equals(s.getStatus())));
                })
                .verifyComplete();
    }
}

