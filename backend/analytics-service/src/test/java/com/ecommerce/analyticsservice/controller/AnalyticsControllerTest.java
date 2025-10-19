package com.ecommerce.analyticsservice.controller;

import com.ecommerce.analyticsservice.dto.*;
import com.ecommerce.analyticsservice.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;

@WebFluxTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AnalyticsService analyticsService;

    private SalesReportDTO salesReportDTO;

    @BeforeEach
    void setUp() {
        TopProductDTO topProduct = new TopProductDTO("1", "Product 1", new BigDecimal("500.00"), 10);
        RevenueDataDTO revenueData = new RevenueDataDTO("Jan", new BigDecimal("1000.00"), 5);
        OrderStatusDataDTO statusData = new OrderStatusDataDTO("delivered", 10, 50.0);

        salesReportDTO = new SalesReportDTO(
                new BigDecimal("1000.00"),
                20L,
                new BigDecimal("50.00"),
                Arrays.asList(topProduct),
                Arrays.asList(revenueData),
                Arrays.asList(statusData)
        );
    }

    @Test
    void testGetSalesReport_Success() {
        when(analyticsService.generateSalesReport()).thenReturn(Mono.just(salesReportDTO));

        webTestClient.get()
                .uri("/api/analytics/sales-report")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalRevenue").isEqualTo(1000.00)
                .jsonPath("$.totalOrders").isEqualTo(20)
                .jsonPath("$.averageOrderValue").isEqualTo(50.00)
                .jsonPath("$.topProducts[0].productName").isEqualTo("Product 1")
                .jsonPath("$.revenueByMonth[0].month").isEqualTo("Jan")
                .jsonPath("$.ordersByStatus[0].status").isEqualTo("delivered");
    }

    @Test
    void testGetSalesReport_EmptyData() {
        SalesReportDTO emptyReport = new SalesReportDTO(
                BigDecimal.ZERO,
                0L,
                BigDecimal.ZERO,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(analyticsService.generateSalesReport()).thenReturn(Mono.just(emptyReport));

        webTestClient.get()
                .uri("/api/analytics/sales-report")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalRevenue").isEqualTo(0)
                .jsonPath("$.totalOrders").isEqualTo(0)
                .jsonPath("$.topProducts").isEmpty();
    }

    @Test
    void testGetSalesReport_ServiceError() {
        when(analyticsService.generateSalesReport())
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/analytics/sales-report")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}

