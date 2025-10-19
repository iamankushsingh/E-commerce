import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { SalesReport, SalesFilters } from '../model/sales.model';

@Injectable({
  providedIn: 'root'
})
export class SalesService {
  private readonly baseUrl = 'http://localhost:8088/api/analytics';

  // Fallback data in case analytics service is unavailable
  private fallbackSalesData: SalesReport = {
    totalRevenue: 15847.92,
    totalOrders: 47,
    averageOrderValue: 337.18,
    topProducts: [
      {
        productId: '1',
        productName: 'Wireless Bluetooth Headphones',
        totalSales: 4499.85,
        unitsSold: 15
      },
      {
        productId: '2',
        productName: 'Smart Fitness Watch',
        totalSales: 3999.80,
        unitsSold: 20
      },
      {
        productId: '3',
        productName: 'Organic Cotton T-Shirt',
        totalSales: 2999.00,
        unitsSold: 100
      },
      {
        productId: '4',
        productName: 'Premium Coffee Beans',
        totalSales: 2249.10,
        unitsSold: 90
      },
      {
        productId: '5',
        productName: 'Eco-Friendly Water Bottle',
        totalSales: 1049.75,
        unitsSold: 30
      }
    ],
    revenueByMonth: [
      { month: 'Jan', revenue: 2450.00, orders: 8 },
      { month: 'Feb', revenue: 2890.50, orders: 12 },
      { month: 'Mar', revenue: 1950.75, orders: 6 },
      { month: 'Apr', revenue: 3200.25, orders: 10 },
      { month: 'May', revenue: 2750.00, orders: 9 },
      { month: 'Jun', revenue: 1850.42, orders: 5 },
      { month: 'Jul', revenue: 3950.80, orders: 15 },
      { month: 'Aug', revenue: 4805.20, orders: 18 }
    ],
    ordersByStatus: [
      { status: 'delivered', count: 28, percentage: 59.6 },
      { status: 'shipped', count: 12, percentage: 25.5 },
      { status: 'processing', count: 5, percentage: 10.6 },
      { status: 'pending', count: 2, percentage: 4.3 }
    ]
  };

  constructor(private http: HttpClient) {}

  getSalesReport(filters?: SalesFilters): Observable<SalesReport> {
    console.log('🔍 [SalesService] Fetching sales report from analytics service');
    
    return this.http.get<SalesReport>(`${this.baseUrl}/sales-report`)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.warn('⚠️ [SalesService] Analytics service unavailable, using fallback data:', error.message);
          // Return fallback data if analytics service is not available
          return of(this.fallbackSalesData);
        }),
        map((report: SalesReport) => {
          console.log('✅ [SalesService] Received sales report:', {
            totalRevenue: report.totalRevenue,
            totalOrders: report.totalOrders,
            topProductsCount: report.topProducts?.length || 0
          });
          return report;
        })
      );
  }

  getDashboardStats(): Observable<any> {
    console.log('📊 [SalesService] Fetching dashboard stats from analytics service');
    
    return this.http.get<any>(`${this.baseUrl}/dashboard-stats`)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.warn('⚠️ [SalesService] Dashboard stats unavailable:', error.message);
          // Return fallback stats
          return of({
            totalRevenue: this.fallbackSalesData.totalRevenue,
            totalOrders: this.fallbackSalesData.totalOrders,
            averageOrderValue: this.fallbackSalesData.averageOrderValue,
            topProductsCount: this.fallbackSalesData.topProducts.length
          });
        })
      );
  }

  checkAnalyticsServiceHealth(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/health`)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          return of({ status: 'DOWN', error: error.message });
        })
      );
  }

  // Legacy methods for compatibility - now using analytics service data
  getRevenueData(filters?: SalesFilters): Observable<any[]> {
    return this.getSalesReport(filters).pipe(
      map(report => report.revenueByMonth.map(item => ({
        name: item.month,
        value: item.revenue
      })))
    );
  }

  getOrderStatusData(): Observable<any[]> {
    return this.getSalesReport().pipe(
      map(report => report.ordersByStatus.map(item => ({
        name: item.status,
        value: item.count
      })))
    );
  }

  getTopProductsData(): Observable<any[]> {
    return this.getSalesReport().pipe(
      map(report => report.topProducts.slice(0, 5).map(item => ({
        name: item.productName,
        value: item.totalSales
      })))
    );
  }
}
