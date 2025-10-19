export interface SalesReport {
  totalRevenue: number;
  totalOrders: number;
  averageOrderValue: number;
  topProducts: TopProduct[];
  revenueByMonth: RevenueData[];
  ordersByStatus: OrderStatusData[];
}

export interface TopProduct {
  productId: string;
  productName: string;
  totalSales: number;
  unitsSold: number;
}

export interface RevenueData {
  month: string;
  revenue: number;
  orders: number;
}

export interface OrderStatusData {
  status: string;
  count: number;
  percentage: number;
}

export interface SalesFilters {
  dateFrom?: Date;
  dateTo?: Date;
  category?: string;
}
