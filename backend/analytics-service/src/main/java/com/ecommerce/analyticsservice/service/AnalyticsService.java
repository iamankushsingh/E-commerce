package com.ecommerce.analyticsservice.service;

import com.ecommerce.analyticsservice.client.OrderServiceClient;
import com.ecommerce.analyticsservice.client.ProductServiceClient;
import com.ecommerce.analyticsservice.client.UserServiceClient;
import com.ecommerce.analyticsservice.dto.*;
import com.ecommerce.analyticsservice.dto.external.OrderDTO;
import com.ecommerce.analyticsservice.dto.external.OrderItemDTO;
import com.ecommerce.analyticsservice.dto.external.PaginatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    public AnalyticsService(OrderServiceClient orderServiceClient,
                          ProductServiceClient productServiceClient,
                          UserServiceClient userServiceClient) {
        this.orderServiceClient = orderServiceClient;
        this.productServiceClient = productServiceClient;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Generate comprehensive sales report
     */
    public Mono<SalesReportDTO> generateSalesReport() {
        logger.info("Generating sales report");

        return fetchAllOrders()
                .map(this::calculateSalesReport)
                .doOnSuccess(report -> logger.info("Generated sales report with total revenue: {}", report.getTotalRevenue()))
                .doOnError(error -> logger.error("Error generating sales report: {}", error.getMessage()));
    }

    /**
     * Fetch all orders from order service
     */
    private Mono<List<OrderDTO>> fetchAllOrders() {
        List<OrderDTO> allOrders = new ArrayList<>();
        
        return fetchOrdersRecursively(0, 100, allOrders)
                .then(Mono.just(allOrders));
    }

    /**
     * Recursively fetch all orders with pagination
     */
    private Mono<Void> fetchOrdersRecursively(int page, int size, List<OrderDTO> allOrders) {
        return orderServiceClient.getAllOrdersForAdmin(page, size)
                .flatMap(paginatedResponse -> {
                    if (paginatedResponse.getContent() != null) {
                        allOrders.addAll(paginatedResponse.getContent());
                    }
                    
                    // If there are more pages, recursively fetch them
                    if (!paginatedResponse.isLast() && paginatedResponse.getContent() != null && !paginatedResponse.getContent().isEmpty()) {
                        return fetchOrdersRecursively(page + 1, size, allOrders);
                    } else {
                        return Mono.<Void>empty();
                    }
                })
                .onErrorResume(error -> {
                    logger.warn("Error fetching orders page {}: {}", page, error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Calculate sales report from orders
     */
    private SalesReportDTO calculateSalesReport(List<OrderDTO> orders) {
        logger.info("Calculating sales report from {} orders", orders.size());

        // Filter out cancelled orders for revenue calculations
        List<OrderDTO> validOrders = orders.stream()
                .filter(order -> !isOrderCancelled(order.getOrderStatus()))
                .collect(Collectors.toList());

        // Calculate total revenue
        BigDecimal totalRevenue = validOrders.stream()
                .map(order -> order.getFinalAmount() != null ? order.getFinalAmount() : order.getTotalAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total orders
        long totalOrders = validOrders.size();

        // Calculate average order value
        BigDecimal averageOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate top products
        List<TopProductDTO> topProducts = calculateTopProducts(validOrders);

        // Calculate revenue by month
        List<RevenueDataDTO> revenueByMonth = calculateRevenueByMonth(validOrders);

        // Calculate order status distribution (including all orders)
        List<OrderStatusDataDTO> ordersByStatus = calculateOrdersByStatus(orders);

        return new SalesReportDTO(totalRevenue, totalOrders, averageOrderValue, 
                                topProducts, revenueByMonth, ordersByStatus);
    }

    /**
     * Calculate top selling products
     */
    private List<TopProductDTO> calculateTopProducts(List<OrderDTO> orders) {
        Map<Long, ProductSales> productSalesMap = new HashMap<>();

        for (OrderDTO order : orders) {
            if (order.getOrderItems() != null) {
                for (OrderItemDTO item : order.getOrderItems()) {
                    Long productId = item.getProductId();
                    if (productId != null) {
                        ProductSales sales = productSalesMap.computeIfAbsent(productId, k -> 
                                new ProductSales(item.getProductName(), BigDecimal.ZERO, 0));
                        
                        sales.totalSales = sales.totalSales.add(
                                item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO);
                        sales.unitsSold += (item.getQuantity() != null ? item.getQuantity() : 0);
                    }
                }
            }
        }

        return productSalesMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().totalSales.compareTo(e1.getValue().totalSales))
                .limit(5)
                .map(entry -> new TopProductDTO(
                        entry.getKey().toString(),
                        entry.getValue().productName != null ? entry.getValue().productName : "Unknown Product",
                        entry.getValue().totalSales,
                        entry.getValue().unitsSold
                ))
                .collect(Collectors.toList());
    }

    /**
     * Calculate revenue by month
     */
    private List<RevenueDataDTO> calculateRevenueByMonth(List<OrderDTO> orders) {
        Map<String, MonthlyRevenue> monthlyRevenueMap = new HashMap<>();

        for (OrderDTO order : orders) {
            if (order.getCreatedAt() != null) {
                String monthKey = order.getCreatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                
                MonthlyRevenue monthlyRevenue = monthlyRevenueMap.computeIfAbsent(monthKey, k -> 
                        new MonthlyRevenue(BigDecimal.ZERO, 0));
                
                BigDecimal orderAmount = order.getFinalAmount() != null ? order.getFinalAmount() : order.getTotalAmount();
                if (orderAmount != null) {
                    monthlyRevenue.revenue = monthlyRevenue.revenue.add(orderAmount);
                }
                monthlyRevenue.orders++;
            }
        }

        // Sort by month order and return
        String[] monthOrder = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        return Arrays.stream(monthOrder)
                .filter(monthlyRevenueMap::containsKey)
                .map(month -> {
                    MonthlyRevenue data = monthlyRevenueMap.get(month);
                    return new RevenueDataDTO(month, data.revenue, data.orders);
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate order status distribution
     */
    private List<OrderStatusDataDTO> calculateOrdersByStatus(List<OrderDTO> orders) {
        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> normalizeStatus(order.getOrderStatus()),
                        Collectors.counting()
                ));

        long totalOrders = orders.size();

        return statusCounts.entrySet().stream()
                .map(entry -> new OrderStatusDataDTO(
                        entry.getKey().toLowerCase(),
                        entry.getValue().intValue(),
                        totalOrders > 0 ? (entry.getValue() * 100.0 / totalOrders) : 0.0
                ))
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
    }

    /**
     * Normalize order status
     */
    private String normalizeStatus(String status) {
        if (status == null) return "unknown";
        String upperStatus = status.toUpperCase();
        switch (upperStatus) {
            case "PENDING": return "pending";
            case "CONFIRMED":
            case "PROCESSING": return "processing";
            case "SHIPPED": return "shipped";
            case "DELIVERED": return "delivered";
            case "CANCELLED": return "cancelled";
            default: return "unknown";
        }
    }

    /**
     * Check if order is cancelled
     */
    private boolean isOrderCancelled(String status) {
        return status != null && "CANCELLED".equalsIgnoreCase(status.trim());
    }

    // Inner classes for calculations
    private static class ProductSales {
        String productName;
        BigDecimal totalSales;
        Integer unitsSold;

        ProductSales(String productName, BigDecimal totalSales, Integer unitsSold) {
            this.productName = productName;
            this.totalSales = totalSales;
            this.unitsSold = unitsSold;
        }
    }

    private static class MonthlyRevenue {
        BigDecimal revenue;
        Integer orders;

        MonthlyRevenue(BigDecimal revenue, Integer orders) {
            this.revenue = revenue;
            this.orders = orders;
        }
    }
} 