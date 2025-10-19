import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { OrderService } from '../../services/order';
import { CartService } from '../../services/cart.service';
import { ProductService } from '../../services/product.service';
import { UserService } from '../../services/user.service';
import { Order, OrderStatus } from '../../model/interfaces';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-user-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-orders.component.html',
  styleUrls: ['./user-orders.component.scss']
})
export class UserOrdersComponent implements OnInit {
  orders: Order[] = [];
  isLoading = true;
  error = '';
  currentUserId = 1; // Default fallback, will be updated from auth service

  // Check if user is logged in
  get isUserLoggedIn(): boolean {
    return this.userService.isLoggedIn();
  }

  constructor(
    private orderService: OrderService,
    private router: Router,
    private cartService: CartService,
    private productService: ProductService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Check if user is logged in and get user ID
    if (!this.userService.isLoggedIn()) {
      this.isLoading = false;
      this.error = '';
      this.orders = []; // Show empty state for non-logged-in users
      return;
    }

    const currentUser = this.userService.getCurrentUser();
    if (currentUser && currentUser.id) {
      this.currentUserId = Number(currentUser.id);
    }
    
    this.loadUserOrders();
  }

  loadUserOrders(): void {
    this.isLoading = true;
    this.error = '';

    const currentUser = this.userService.getCurrentUser();
    const authState = JSON.parse(localStorage.getItem('authState') || '{}');
    
    console.log('🔍 [User Orders] Current user from service:', currentUser);
    console.log('🔍 [User Orders] Auth state from localStorage:', authState);
    console.log('🔍 [User Orders] Using user ID for query:', this.currentUserId);

    // Try to get orders from backend first, fallback to mock data if needed
    this.orderService.getUserOrdersFromBackend(this.currentUserId).subscribe({
      next: (response: any) => {
        console.log('📦 [User Orders] Backend response:', response);
        if (response.success && response.orders) {
          // Backend response successful
          const rawOrders = response.orders.content || response.orders;
          console.log('📦 [User Orders] Raw orders from backend:', rawOrders);
          this.orders = this.mapBackendOrdersToFrontend(rawOrders);
          console.log('📦 [User Orders] Mapped orders for display:', this.orders);
        } else {
          // Backend response failed, use mock data
          console.log('⚠️ [User Orders] Backend failed, loading mock data');
          this.loadMockOrders();
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.warn('Backend unavailable, using mock data:', error);
        // Backend failed, use mock data as fallback
        this.loadMockOrders();
      }
    });
  }

  private loadMockOrders(): void {
    // Fallback to mock orders
    this.orderService.getAllOrders().subscribe({
      next: (allOrders: Order[]) => {
        this.orders = allOrders.filter((order: Order) => order.userId === this.currentUserId.toString());
        this.isLoading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load orders. Please try again.';
        this.isLoading = false;
        console.error('Error loading mock orders:', error);
      }
    });
  }

  // Map backend order DTOs to frontend Order model
  private mapBackendOrdersToFrontend(backendOrders: any[]): Order[] {
    return backendOrders.map((backendOrder: any) => ({
      id: backendOrder.id?.toString() || backendOrder.orderId?.toString() || '',
      userId: backendOrder.userId?.toString(),
      items: backendOrder.orderItems?.map((item: any) => ({
        id: item.id?.toString(),
        name: item.productName || item.name,
        image: item.productImageUrl || item.image || '/assets/placeholder.png',
        price: item.unitPrice || item.price || 0,
        quantity: item.quantity || 1,
        size: item.size || 'N/A',
        color: item.color || 'N/A'
      })) || [],
      totalAmount: backendOrder.finalAmount || backendOrder.totalAmount || 0,
      discountAmount: backendOrder.discountAmount || 0,
      couponCode: backendOrder.couponCode || undefined,
      orderStatus: (backendOrder.orderStatus?.toLowerCase() || backendOrder.status?.toLowerCase() || 'pending') as any,
      orderDate: new Date(backendOrder.createdAt || backendOrder.orderDate || new Date()),
      shippingAddress: this.mapShippingAddress(backendOrder.shippingAddress),
      trackingNumber: backendOrder.trackingNumber
    } as Order));
  }

  private mapShippingAddress(address: any): any {
    if (typeof address === 'string') {
      // If address is just a string, create a simple address object
      return {
        firstName: 'Customer',
        lastName: '',
        address: address,
        city: '',
        country: '',
        zipcode: '',
        phone: ''
      };
    }
    return address || {};
  }

  viewOrderDetails(orderId: string): void {
    this.router.navigate(['/order-confirmation', orderId]);
  }

  trackOrder(orderId: string): void {
    this.router.navigate(['/order-tracking', orderId]);
  }

  getOrderStatusText(status: string | OrderStatus): string {
    const statusMap: Record<string, string> = {
      'pending': 'Order Placed',
      'confirmed': 'Confirmed',
      'processing': 'Processing',
      'shipped': 'Shipped',
      'delivered': 'Delivered',
      'cancelled': 'Cancelled'
    };
    return statusMap[status?.toLowerCase()] || status?.toString() || 'Unknown';
  }

  getOrderStatusColor(status: string | OrderStatus): string {
    const colorMap: Record<string, string> = {
      'pending': '#f59e0b',
      'confirmed': '#3b82f6',
      'processing': '#8b5cf6',
      'shipped': '#10b981',
      'delivered': '#059669',
      'cancelled': '#ef4444'
    };
    return colorMap[status?.toLowerCase()] || '#6b7280';
  }

  getOrderTotal(order: Order): number {
    return order.items.reduce((total, item) => total + (item.price * item.quantity), 0);
  }

  formatDate(date: Date | string): string {
    if (!date) return '';
    const orderDate = typeof date === 'string' ? new Date(date) : date;
    return orderDate.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  }

  canCancelOrder(order: Order): boolean {
    return order.orderStatus === 'pending' || order.orderStatus === 'confirmed';
  }

  cancelOrder(order: Order): void {
    if (confirm('Are you sure you want to cancel this order?')) {
      // Try backend API first, fallback to local update
      this.orderService.cancelOrderBackend(this.currentUserId, order.id).subscribe({
        next: (response: any) => {
          if (response.success) {
            order.orderStatus = 'cancelled';
            alert('Order cancelled successfully!');
            // Reload orders to get updated status
            this.loadUserOrders();
          } else {
            alert('Failed to cancel order. Please try again.');
          }
        },
        error: (error: any) => {
          console.warn('Backend cancel failed, updating locally:', error);
          // Fallback: update locally
          order.orderStatus = 'cancelled';
          alert('Order cancelled (local update)');
        }
      });
    }
  }

  reorderItems(order: Order): void {
    if (!order.items || order.items.length === 0) {
      alert('No items to reorder');
      return;
    }

    // Get product details and add each item to cart
    const addToCartPromises = order.items.map(item => {
      // First try to get product details
      return this.productService.getProduct(String(item.productId || item.id)).pipe(
        catchError((error: any) => {
          console.warn(`Product ${item.productId} not found, using order item data:`, error);
          return of(null);
        })
      ).toPromise().then((productResponse: any) => {
        // Add to cart using cart service
        const productId = Number(item.productId || item.id);
        const quantity = item.quantity || 1;
        const productName = item.name;
        const unitPrice = item.price;
        const productImageUrl = item.image || '/assets/placeholder.png';

        return this.cartService.addToCart(
          productId, 
          quantity, 
          productName, 
          unitPrice, 
          productImageUrl
        ).toPromise();
      });
    });

    // Execute all add to cart operations
    Promise.all(addToCartPromises).then(results => {
      const successCount = results.filter((result: any) => result === true).length;
      const totalItems = order.items.length;

      if (successCount === totalItems) {
        alert(`All ${totalItems} items added to cart successfully!`);
      } else if (successCount > 0) {
        alert(`${successCount} out of ${totalItems} items added to cart.`);
      } else {
        alert('Failed to add items to cart. Please try again.');
      }

      if (successCount > 0) {
        this.router.navigate(['/cart']);
      }
    }).catch(error => {
      console.error('Error during reorder:', error);
      alert('Error adding items to cart. Please try again.');
    });
  }
} 