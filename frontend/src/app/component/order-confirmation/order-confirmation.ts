import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Order, OrderTracking } from '../../model/interfaces';
import { Order as OrderModel } from '../../model/order.model';
import { OrderService } from '../../services/order';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-confirmation.html',
  styleUrls: ['./order-confirmation.scss']
})
export class OrderConfirmation implements OnInit {
  order: OrderModel | any = null;
  orderTracking: OrderTracking | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.loadOrder();
  }

  private loadOrder(): void {
    // First try to get order from router state (passed from checkout)
    const navigation = this.router.getCurrentNavigation();
    const routerState = navigation?.extras?.state || history.state;
    
    if (routerState?.order && routerState?.fromCheckout) {
      // Order data passed directly from checkout
      this.order = routerState.order;
      console.log('📦 [Order Confirmation] Order loaded from checkout:', this.order);
      this.isLoading = false;
      return;
    }

    // Try to get order by ID from URL parameter
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      console.log('📦 [Order Confirmation] Loading order by ID:', orderId);
      
      // First try local storage/mock data
      const foundOrder = this.orderService.getOrderById(orderId);
      if (foundOrder) {
        this.order = foundOrder;
        this.orderTracking = this.orderService.getOrderTracking(orderId);
        console.log('📦 [Order Confirmation] Order loaded from local data:', this.order);
        this.isLoading = false;
        return;
      }

      // If not found locally, try to fetch from backend
      this.loadOrderFromBackend(orderId);
    } else {
      console.warn('📦 [Order Confirmation] No order ID provided');
      this.handleOrderNotFound();
    }
  }

  private loadOrderFromBackend(orderId: string): void {
    const currentUser = this.userService.getCurrentUser();
    const userId = currentUser?.id ? Number(currentUser.id) : 1;
    
    console.log('📦 [Order Confirmation] Fetching order from backend for user:', userId);
    
    this.orderService.getUserOrdersFromBackend(userId, 0, 50).subscribe({
      next: (response: any) => {
        console.log('📦 [Order Confirmation] Backend response:', response);
        if (response.success && response.orders) {
          const orders = response.orders.content || response.orders;
          const foundOrder = orders.find((order: any) => order.id?.toString() === orderId);
          
          if (foundOrder) {
            // Map backend order to frontend format
            this.order = this.mapBackendOrderToFrontend(foundOrder);
            console.log('📦 [Order Confirmation] Order loaded from backend:', this.order);
          } else {
            console.warn('📦 [Order Confirmation] Order not found in backend response');
            this.handleOrderNotFound();
          }
        } else {
          console.warn('📦 [Order Confirmation] Backend response failed');
          this.handleOrderNotFound();
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('📦 [Order Confirmation] Backend error:', error);
        this.handleOrderNotFound();
      }
    });
  }

  private mapBackendOrderToFrontend(backendOrder: any): any {
    return {
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
      orderStatus: (backendOrder.orderStatus?.toLowerCase() || backendOrder.status?.toLowerCase() || 'pending'),
      orderDate: new Date(backendOrder.createdAt || backendOrder.orderDate || new Date()),
      shippingAddress: this.parseShippingAddress(backendOrder.shippingAddress, backendOrder),
      trackingNumber: backendOrder.trackingNumber,
      paymentDetails: { 
        method: backendOrder.paymentMethod || 'card',
        cardNumber: '**** **** **** ****'
      },
      shippingCost: backendOrder.shippingAmount || 0,
      discountAmount: backendOrder.discountAmount || 0,
      couponCode: backendOrder.couponCode || undefined
    };
  }

  private parseShippingAddress(address: any, order: any): any {
    if (!address) {
      return {
        firstName: 'Customer',
        lastName: '',
        address: 'Address not provided',
        apartment: '',
        city: '',
        country: '',
        zipcode: '',
        phone: order.phoneNumber || ''
      };
    }

    if (typeof address === 'object' && address.firstName) {
      return address;
    }

    if (typeof address === 'string') {
      const lines = address.split('\n').filter(line => line.trim());
      return {
        firstName: lines[0]?.split(' ')[0] || 'Customer',
        lastName: lines[0]?.split(' ').slice(1).join(' ') || '',
        address: lines[1] || address,
        apartment: lines[2] && !lines[2].includes(',') ? lines[2] : '',
        city: this.extractCity(lines),
        country: this.extractCountry(lines),
        zipcode: this.extractZipCode(lines),
        phone: order.phoneNumber || ''
      };
    }

    return address;
  }

  private extractCity(lines: string[]): string {
    const cityLine = lines.find(line => line.includes(','));
    if (cityLine) {
      return cityLine.split(',')[0]?.trim() || '';
    }
    return '';
  }

  private extractCountry(lines: string[]): string {
    const cityLine = lines.find(line => line.includes(','));
    if (cityLine && cityLine.includes(',')) {
      const parts = cityLine.split(',');
      if (parts.length >= 2) {
        return parts[1]?.replace(/\d+/g, '').trim() || '';
      }
    }
    return '';
  }

  private extractZipCode(lines: string[]): string {
    const cityLine = lines.find(line => line.includes(','));
    if (cityLine) {
      const match = cityLine.match(/\d{5,6}/);
      return match ? match[0] : '';
    }
    return '';
  }

    private handleOrderNotFound(): void {
    console.warn('📦 [Order Confirmation] Order not found, showing error state');
    this.order = null;
    this.isLoading = false;
    // Don't redirect to home, show a proper error message instead
  }

  // Navigation methods
  goToOrderTracking(): void {
    if (this.order) {
      this.router.navigate(['/order-tracking', this.order.id]);
    }
  }

  printOrder(): void {
    window.print();
  }

  downloadOrderPDF(): void {
    // In a real app, this would generate and download a PDF
    alert('PDF download functionality would be implemented here');
  }

  // Utility methods
  formatDate(date: Date | string): string {
    if (!date) return '';
    const orderDate = typeof date === 'string' ? new Date(date) : date;
    return orderDate.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getOrderTotal(): number {
    if (!this.order) return 0;
    return this.order.totalAmount || 0;
  }

  getEstimatedDeliveryDate(): string {
    if (this.orderTracking?.estimatedDelivery) {
      return this.orderTracking.estimatedDelivery.toLocaleDateString();
    }
    
    if (!this.order) return 'TBD';
    const estimatedDays = 5; // Default estimate
    const deliveryDate = new Date(this.order.orderDate);
    deliveryDate.setDate(deliveryDate.getDate() + estimatedDays);
    return deliveryDate.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getOrderStatusText(): string {
    if (this.order) {
      const status = this.order.orderStatus || this.order.status || 'pending';
      return this.orderService.getOrderStatusText(status);
    }
    return '';
  }

  getOrderStatusColor(): string {
    if (this.order) {
      const status = this.order.orderStatus || this.order.status || 'pending';
      return this.orderService.getOrderStatusColor(status);
    }
    return '#666';
  }
} 