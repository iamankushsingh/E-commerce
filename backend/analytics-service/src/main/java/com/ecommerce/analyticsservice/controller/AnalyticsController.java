package com.ecommerce.analyticsservice.controller;

import com.ecommerce.analyticsservice.dto.SalesReportDTO;
import com.ecommerce.analyticsservice.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get sales report for dashboard
     * GET /api/analytics/sales-report
     */
    @GetMapping("/sales-report")
    public Mono<ResponseEntity<SalesReportDTO>> getSalesReport() {
        logger.info("Fetching sales report for dashboard");

        return analyticsService.generateSalesReport()
                .map(ResponseEntity::ok)
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(error -> {
                    logger.error("Error generating sales report: {}", error.getMessage());
                    // Return empty report on error
                    SalesReportDTO emptyReport = new SalesReportDTO();
                    return Mono.just(ResponseEntity.ok(emptyReport));
                });
    }

    /**
     * Get dashboard summary stats
     * GET /api/analytics/dashboard-stats
     */
    @GetMapping("/dashboard-stats")
    public Mono<ResponseEntity<Map<String, Object>>> getDashboardStats() {
        logger.info("Fetching dashboard statistics");

        return analyticsService.generateSalesReport()
                .map(report -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalRevenue", report.getTotalRevenue());
                    stats.put("totalOrders", report.getTotalOrders());
                    stats.put("averageOrderValue", report.getAverageOrderValue());
                    stats.put("topProductsCount", report.getTopProducts() != null ? report.getTopProducts().size() : 0);
                    return ResponseEntity.ok(stats);
                })
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(error -> {
                    logger.error("Error generating dashboard stats: {}", error.getMessage());
                    Map<String, Object> errorStats = new HashMap<>();
                    errorStats.put("error", "Unable to fetch statistics");
                    return Mono.just(ResponseEntity.ok(errorStats));
                });
    }

    /**
     * Health check endpoint
     * GET /api/analytics/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "analytics-service");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
} 