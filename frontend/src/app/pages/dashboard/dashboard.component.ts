import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SalesService } from '../../services/sales.service';
import { SalesReport } from '../../model/sales.model';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  salesReport?: SalesReport;
  loading = true;
  analyticsServiceStatus = 'CHECKING';
  lastUpdated?: Date;
  private refreshSubscription?: Subscription;
  private healthCheckSubscription?: Subscription;

  constructor(private salesService: SalesService) {}

  ngOnInit(): void {
    this.checkAnalyticsHealth();
    this.loadSalesReport();
    this.setupAutoRefresh();
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
    if (this.healthCheckSubscription) {
      this.healthCheckSubscription.unsubscribe();
    }
  }

  checkAnalyticsHealth(): void {
    this.salesService.checkAnalyticsServiceHealth().subscribe({
      next: (health) => {
        this.analyticsServiceStatus = health.status === 'UP' ? 'UP' : 'DOWN';
      },
      error: () => {
        this.analyticsServiceStatus = 'DOWN';
      }
    });
  }

  loadSalesReport(): void {
    this.loading = true;
    console.log('📊 [Dashboard] Loading sales report...');
    
    this.salesService.getSalesReport().subscribe({
      next: (report) => {
        this.salesReport = report;
        this.loading = false;
        this.lastUpdated = new Date();
        console.log('✅ [Dashboard] Sales report loaded successfully');
      },
      error: (error) => {
        console.error('❌ [Dashboard] Error loading sales report:', error);
        this.loading = false;
        // The service already provides fallback data
      }
    });
  }

  refreshData(): void {
    console.log('🔄 [Dashboard] Manual refresh triggered');
    this.checkAnalyticsHealth();
    this.loadSalesReport();
  }

  setupAutoRefresh(): void {
    // Refresh data every 5 minutes
    this.refreshSubscription = interval(5 * 60 * 1000).subscribe(() => {
      console.log('⏰ [Dashboard] Auto-refreshing data...');
      this.loadSalesReport();
    });
  }

  /**
   * Calculate bar height percentage for revenue chart
   */
  getBarHeight(revenue: number): number {
    if (!this.salesReport?.revenueByMonth || this.salesReport.revenueByMonth.length === 0) {
      return 0;
    }
    
    const maxRevenue = Math.max(...this.salesReport.revenueByMonth.map(r => r.revenue));
    return maxRevenue > 0 ? (revenue / maxRevenue) * 100 : 0;
  }

  /**
   * Get the count of top products safely
   */
  getTopProductsCount(): number {
    return this.salesReport?.topProducts?.length || 0;
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      delivered: '#22c55e',
      shipped: '#3b82f6',
      processing: '#f59e0b',
      pending: '#ef4444',
      cancelled: '#ef4444'
    };
    return colors[status] || '#6b7280';
  }

  getServiceStatusColor(): string {
    const colors: Record<string, string> = {
      'UP': '#22c55e',
      'DOWN': '#ef4444',
      'CHECKING': '#f59e0b'
    };
    return colors[this.analyticsServiceStatus] || '#6b7280';
  }

  formatLastUpdated(): string {
    if (!this.lastUpdated) return 'Never';
    return this.lastUpdated.toLocaleTimeString();
  }
}
