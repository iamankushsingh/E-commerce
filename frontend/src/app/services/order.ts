import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Order, OrderStatus, OrderTracking, OrderStatusHistory, CartItem, ShippingAddress, PaymentDetails, CreateOrderRequest } from '../model/interfaces';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private orders: Order[] = [];
  private ordersSubject = new BehaviorSubject<Order[]>(this.orders);
  public orders$ = this.ordersSubject.asObservable();

  private readonly ORDERS_STORAGE_KEY = 'ecommerce_orders';

  private readonly API_BASE_URL = 'http://localhost:8087/api/orders';

  constructor(private http: HttpClient, private userService: UserService) {
    this.loadOrdersFromStorage();
    this.initializeMockOrders();
  }

  private loadOrdersFromStorage(): void {
    try {
      const savedOrders = localStorage.getItem(this.ORDERS_STORAGE_KEY);
      if (savedOrders) {
        this.orders = JSON.parse(savedOrders).map((order: any) => ({
          ...order,
          orderDate: new Date(order.orderDate)
        }));
        this.ordersSubject.next(this.orders);
      }
    } catch (error) {
      console.error('Error loading orders from storage:', error);
    }
  }

  private saveOrdersToStorage(): void {
    try {
      localStorage.setItem(this.ORDERS_STORAGE_KEY, JSON.stringify(this.orders));
    } catch (error) {
      console.error('Error saving orders to storage:', error);
    }
  }

  private initializeMockOrders(): void {
    if (this.orders.length === 0) {
      // Add some mock orders for demonstration
      const mockOrders: Order[] = [
        {
          id: 'ORD-001',
          userId: 'user-123',
          items: [
            {
              id: 'cart-item-1',
              productId: '1',
              name: "Men's Winter Jacket",
              price: 99,
              size: 'L',
              color: 'Black',
              quantity: 1,
              image: 'DE25JFSA003_E-commerce\frontend\src\assets\jacket.jpg'
            }
          ],
          shippingAddress: {
            firstName: 'John',
            lastName: 'Doe',
            address: '123 Main St',
            city: 'New York',
            country: 'United States',
            zipcode: '10001',
            phone: '+1234567890'
          },
          paymentDetails: {
            method: 'card',
            cardNumber: '**** **** **** 1234',
            cardholderName: 'John Doe'
          },
          orderStatus: 'shipped',
          orderDate: new Date('2024-01-15'),
          totalAmount: 107.92,
          shippingCost: 0,
          trackingNumber: 'TRK123456789'
        },
        {
          id: 'ORD-002',
          userId: 'user-123',
          items: [
            {
              id: 'cart-item-2',
              productId: '3',
              name: "Cotton T-Shirt",
              price: 25,
              size: 'M',
              color: 'White',
              quantity: 2,
              image: '/assets/tshirt1.jpg'
            }
          ],
          shippingAddress: {
            firstName: 'John',
            lastName: 'Doe',
            address: '123 Main St',
            city: 'New York',
            country: 'United States',
            zipcode: '10001'
          },
          paymentDetails: {
            method: 'upi',
            upiId: 'john@upi'
          },
          orderStatus: 'processing',
          orderDate: new Date('2024-01-20'),
          totalAmount: 54.00,
          shippingCost: 0
        }
      ];

      this.orders = mockOrders;
      this.ordersSubject.next(this.orders);
      this.saveOrdersToStorage();
    }
  }

  createOrder(
    items: CartItem[],
    shippingAddress: ShippingAddress,
    paymentDetails: PaymentDetails,
    totalAmount: number,
    shippingCost: number
  ): Order {
    const orderId = this.generateOrderId();
    const trackingNumber = this.generateTrackingNumber();
    
    // Get actual user ID from authentication
    const currentUser = this.userService.getCurrentUser();
    const userId = currentUser?.id?.toString() || 'user-1';

    const newOrder: Order = {
      id: orderId,
      userId: userId,
      items: [...items],
      shippingAddress: { ...shippingAddress },
      paymentDetails: { ...paymentDetails },
      orderStatus: 'pending',
      orderDate: new Date(),
      totalAmount,
      shippingCost,
      trackingNumber
    };

    this.orders.unshift(newOrder); // Add to beginning of array (most recent first)
    this.ordersSubject.next(this.orders);
    this.saveOrdersToStorage();

    // Simulate order processing after creation
    setTimeout(() => {
      this.updateOrderStatus(orderId, 'confirmed');
    }, 2000);

    return newOrder;
  }

  /**
   * Create order via backend API
   */
  createBackendOrder(orderRequest: CreateOrderRequest): Observable<any> {
    // Get actual user ID from authentication
    const currentUser = this.userService.getCurrentUser();
    const userId = currentUser?.id ? Number(currentUser.id) : 1; // Fallback to 1 if not logged in
    
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    console.log(`Creating order for user ID: ${userId}`, { currentUser, orderRequest });
    return this.http.post(`${this.API_BASE_URL}/users/${userId}`, orderRequest, { headers });
  }

  /**
   * Get all orders for admin (from backend)
   */
  getAllOrdersForAdmin(
    userId?: number,
    orderStatus?: string,
    paymentStatus?: string,
    startDate?: Date | string,
    endDate?: Date | string,
    page: number = 0,
    size: number = 50,
    sortBy: string = 'createdAt',
    sortDirection: string = 'desc',
    search?: string
  ): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    let params = new HttpParams();
    // Only add filter parameters if they are not empty strings or undefined
    if (userId) params = params.set('userId', userId.toString());
    if (orderStatus && orderStatus.trim() !== '') params = params.set('orderStatus', orderStatus.trim());
    if (paymentStatus && paymentStatus.trim() !== '') params = params.set('paymentStatus', paymentStatus.trim());
    if (search && search.trim() !== '') {
      params = params.set('search', search.trim());
    }
    
    // Handle dates - convert strings to Date objects and validate
    if (startDate) {
      const dateObj = typeof startDate === 'string' ? new Date(startDate) : startDate;
      if (!isNaN(dateObj.getTime())) {
        params = params.set('startDate', dateObj.toISOString());
      }
    }
    if (endDate) {
      const dateObj = typeof endDate === 'string' ? new Date(endDate) : endDate;
      if (!isNaN(dateObj.getTime())) {
        params = params.set('endDate', dateObj.toISOString());
      }
    }
    
    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    params = params.set('sortBy', sortBy);
    params = params.set('sortDirection', sortDirection);

    const adminUrl = 'http://localhost:8087/api/admin/orders';

    return this.http.get(adminUrl, { headers, params });
  }

  /**
   * Update order status (admin)
   */
  updateOrderStatusAdmin(orderId: string, orderStatus: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    const adminUrl = 'http://localhost:8087/api/admin/orders';
    const updateData = { orderStatus };

    return this.http.put(`${adminUrl}/${orderId}/status`, updateData, { headers });
  }

  /**
   * Get orders for a specific user (from backend API)
   */
  getUserOrdersFromBackend(userId: number, page: number = 0, size: number = 50): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    let params = new HttpParams();
    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    params = params.set('sortBy', 'createdAt');
    params = params.set('sortDirection', 'desc');

    const userOrdersUrl = `${this.API_BASE_URL}/users/${userId}`;

    return this.http.get(userOrdersUrl, { headers, params });
  }

  /**
   * Cancel an order via backend API
   */
  cancelOrderBackend(userId: number, orderId: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    const cancelUrl = `${this.API_BASE_URL}/users/${userId}/orders/${orderId}/cancel`;

    return this.http.put(cancelUrl, {}, { headers });
  }

  getOrderById(orderId: string): Order | undefined {
    return this.orders.find(order => order.id === orderId);
  }

  getOrdersByUserId(userId: string): Order[] {
    return this.orders.filter(order => order.userId === userId);
  }

  getUserOrders(): Order[] {
    // Filter by actual current user
    const currentUser = this.userService.getCurrentUser();
    const userId = currentUser?.id?.toString() || 'user-1';
    return this.orders.filter(order => order.userId === userId);
  }

  getAllOrders(): Observable<Order[]> {
    return this.orders$;
  }

  updateOrderStatus(orderId: string, newStatus: OrderStatus): void {
    const orderIndex = this.orders.findIndex(order => order.id === orderId);
    if (orderIndex !== -1) {
      this.orders[orderIndex].orderStatus = newStatus;
      this.ordersSubject.next(this.orders);
      this.saveOrdersToStorage();
    }
  }

  getOrderTracking(orderId: string): OrderTracking | null {
    const order = this.getOrderById(orderId);
    if (!order) return null;

    const statusHistory: OrderStatusHistory[] = this.generateStatusHistory(order);

    return {
      orderId: order.id,
      status: order.orderStatus,
      statusHistory,
      estimatedDelivery: this.calculateEstimatedDelivery(order.orderDate, order.orderStatus),
      trackingNumber: order.trackingNumber
    };
  }

  private generateStatusHistory(order: Order): OrderStatusHistory[] {
    const baseDate = new Date(order.orderDate);
    const history: OrderStatusHistory[] = [];

    // Always include order placed
    history.push({
      status: 'pending',
      timestamp: new Date(baseDate),
      description: 'Order placed successfully',
      location: 'Online'
    });

    if (['confirmed', 'processing', 'shipped', 'delivered'].includes(order.orderStatus)) {
      history.push({
        status: 'confirmed',
        timestamp: new Date(baseDate.getTime() + 30 * 60 * 1000), // 30 minutes later
        description: 'Order confirmed and payment processed',
        location: 'Processing Center'
      });
    }

    if (['processing', 'shipped', 'delivered'].includes(order.orderStatus)) {
      history.push({
        status: 'processing',
        timestamp: new Date(baseDate.getTime() + 24 * 60 * 60 * 1000), // 1 day later
        description: 'Order is being prepared for shipment',
        location: 'Fulfillment Center'
      });
    }

    if (['shipped', 'delivered'].includes(order.orderStatus)) {
      history.push({
        status: 'shipped',
        timestamp: new Date(baseDate.getTime() + 2 * 24 * 60 * 60 * 1000), // 2 days later
        description: 'Package shipped and in transit',
        location: 'Distribution Hub'
      });
    }

    if (order.orderStatus === 'delivered') {
      history.push({
        status: 'delivered',
        timestamp: new Date(baseDate.getTime() + 5 * 24 * 60 * 60 * 1000), // 5 days later
        description: 'Package delivered successfully',
        location: order.shippingAddress.city
      });
    }

    return history;
  }

  private calculateEstimatedDelivery(orderDate: Date, status: OrderStatus): Date {
    const estimatedDays = status === 'delivered' ? 0 : 
                         status === 'shipped' ? 2 : 
                         status === 'processing' ? 4 : 5;
    
    return new Date(orderDate.getTime() + estimatedDays * 24 * 60 * 60 * 1000);
  }

  private generateOrderId(): string {
    const prefix = 'ORD';
    const timestamp = Date.now().toString(36).toUpperCase();
    const random = Math.random().toString(36).substring(2, 5).toUpperCase();
    return `${prefix}-${timestamp}${random}`;
  }

  private generateTrackingNumber(): string {
    const prefix = 'TRK';
    const numbers = Math.floor(Math.random() * 1000000000).toString().padStart(9, '0');
    return `${prefix}${numbers}`;
  }

  // Utility methods for order management
  getOrderStatusText(status: OrderStatus): string {
    const statusTexts: { [key in OrderStatus]: string } = {
      'pending': 'Order Placed',
      'confirmed': 'Order Confirmed',
      'processing': 'Being Prepared',
      'shipped': 'In Transit',
      'delivered': 'Delivered',
      'cancelled': 'Cancelled'
    };
    return statusTexts[status];
  }

  getOrderStatusColor(status: OrderStatus): string {
    const colors: { [key in OrderStatus]: string } = {
      'pending': '#f59e0b',
      'confirmed': '#3b82f6',
      'processing': '#8b5cf6',
      'shipped': '#06b6d4',
      'delivered': '#10b981',
      'cancelled': '#ef4444'
    };
    return colors[status];
  }

  canCancelOrder(order: Order): boolean {
    return ['pending', 'confirmed'].includes(order.orderStatus);
  }

  cancelOrder(orderId: string): boolean {
    const order = this.getOrderById(orderId);
    if (order && this.canCancelOrder(order)) {
      this.updateOrderStatus(orderId, 'cancelled');
      return true;
    }
    return false;
  }
}

// Export the interface for use in other components
export type { CreateOrderRequest }; 