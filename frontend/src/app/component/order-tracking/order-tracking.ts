import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Order, OrderTracking, OrderStatusHistory } from '../../model/interfaces';
import { OrderService } from '../../services/order';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-order-tracking',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-tracking.html',
  styleUrls: ['./order-tracking.scss']
})
export class OrderTrackingComponent implements OnInit {
  order: Order | null = null;
  orderTracking: OrderTracking | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.loadOrderTracking();
  }

  private loadOrderTracking(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      // First try to get order from backend
      const currentUser = this.userService.getCurrentUser();
      const userId = currentUser?.id ? Number(currentUser.id) : 1;
      this.orderService.getUserOrdersFromBackend(userId, 0, 50).subscribe({
        next: (response: any) => {
          if (response.success && response.orders) {
            const orders = response.orders.content || response.orders;
            const foundOrder = orders.find((order: any) => order.id?.toString() === orderId);
            
            if (foundOrder) {
              // Map backend order to frontend format
              this.order = {
                id: foundOrder.id?.toString() || foundOrder.orderId?.toString() || '',
                userId: foundOrder.userId?.toString(),
                items: foundOrder.orderItems?.map((item: any) => ({
                  id: item.id?.toString(),
                  name: item.productName || item.name,
                  image: item.productImageUrl || item.image || '/assets/placeholder.png',
                  price: item.unitPrice || item.price || 0,
                  quantity: item.quantity || 1,
                  size: item.size || 'N/A',
                  color: item.color || 'N/A',
                  productId: item.productId || item.id
                })) || [],
                totalAmount: foundOrder.finalAmount || foundOrder.totalAmount || 0,
                orderStatus: (foundOrder.orderStatus?.toLowerCase() || foundOrder.status?.toLowerCase() || 'pending') as any,
                orderDate: new Date(foundOrder.createdAt || foundOrder.orderDate || new Date()),
                shippingAddress: this.parseShippingAddress(foundOrder.shippingAddress, foundOrder),
                trackingNumber: foundOrder.trackingNumber,
                paymentDetails: { 
                  method: foundOrder.paymentMethod || 'card',
                  cardNumber: foundOrder.paymentDetails?.cardNumber || '**** **** **** ****'
                },
                shippingCost: foundOrder.shippingAmount || 0,
                discountAmount: foundOrder.discountAmount || 0,
                couponCode: foundOrder.couponCode || undefined
              };
              
              this.orderTracking = this.orderService.getOrderTracking(orderId);
            } else {
              // Fallback to local order data
              this.loadLocalOrderData(orderId);
            }
          } else {
            // Fallback to local order data
            this.loadLocalOrderData(orderId);
          }
          this.isLoading = false;
        },
        error: (error: any) => {
          console.warn('Backend order tracking failed, using local data:', error);
          this.loadLocalOrderData(orderId);
        }
      });
    } else {
      this.router.navigate(['/']);
      this.isLoading = false;
    }
  }

  private loadLocalOrderData(orderId: string): void {
    const foundOrder = this.orderService.getOrderById(orderId);
    if (foundOrder) {
      this.order = foundOrder;
      this.orderTracking = this.orderService.getOrderTracking(orderId);
    }
    
    if (!this.order) {
      // Order not found, redirect to home
      this.router.navigate(['/']);
    }
    
    this.isLoading = false;
  }

  getEstimatedDeliveryDate(): string {
    if (this.orderTracking?.estimatedDelivery) {
      return this.orderTracking.estimatedDelivery.toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    }
    return 'TBD';
  }

  getOrderStatusText(): string {
    if (this.order) {
      return this.orderService.getOrderStatusText(this.order.orderStatus);
    }
    return '';
  }

  getOrderStatusColor(): string {
    if (this.order) {
      return this.orderService.getOrderStatusColor(this.order.orderStatus);
    }
    return '#666';
  }

  getStatusStepNumber(status: string): number {
    const statusOrder = ['pending', 'confirmed', 'processing', 'shipped', 'delivered'];
    return statusOrder.indexOf(status) + 1;
  }

  isStatusCompleted(status: string): boolean {
    if (!this.order) return false;
    const currentStatusIndex = this.getStatusOrder().indexOf(this.order.orderStatus);
    const statusIndex = this.getStatusOrder().indexOf(status);
    return statusIndex <= currentStatusIndex;
  }

  isStatusCurrent(status: string): boolean {
    return this.order?.orderStatus === status;
  }

  private getStatusOrder(): string[] {
    return ['pending', 'confirmed', 'processing', 'shipped', 'delivered'];
  }

  formatDateTime(date: Date): string {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  canCancelOrder(): boolean {
    if (!this.order) return false;
    return this.order.orderStatus === 'pending' || this.order.orderStatus === 'confirmed';
  }

  cancelOrder(): void {
    if (this.order && confirm('Are you sure you want to cancel this order?')) {
      // Get current user ID from user service
      const currentUser = this.userService.getCurrentUser();
      const currentUserId = currentUser?.id ? Number(currentUser.id) : 1; // Convert to number with fallback
      
      // Use backend API for cancelling order
      this.orderService.cancelOrderBackend(currentUserId, this.order.id).subscribe({
        next: (response: any) => {
          if (response.success) {
            this.order!.orderStatus = 'cancelled';
            alert('Your order has been cancelled successfully.');
            // Reload the tracking information
            this.loadOrderTracking();
          } else {
            alert('Failed to cancel order. Please try again.');
          }
        },
        error: (error: any) => {
          console.warn('Backend cancel failed, updating locally:', error);
          // Fallback: update locally
          this.order!.orderStatus = 'cancelled';
          alert('Order cancelled (local update)');
          this.loadOrderTracking();
        }
      });
    }
  }

  goToOrderDetails(): void {
    if (this.order) {
      this.router.navigate(['/order-confirmation', this.order.id]);
    }
  }

  getPaymentMethodDisplay(method: string): string {
    const methodMap: { [key: string]: string } = {
      'card': 'Credit/Debit Card',
      'upi': 'UPI Payment',
      'paypal': 'PayPal',
      'cod': 'Cash on Delivery'
    };
    return methodMap[method?.toLowerCase()] || method || 'N/A';
  }

  trackHistoryItem(index: number, item: OrderStatusHistory): any {
    return item.timestamp;
  }

  // Parse shipping address from backend (could be string or object)
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

    // If address is already an object, return it
    if (typeof address === 'object' && address.firstName) {
      return {
        ...address,
        phone: address.phone || order.phoneNumber || ''
      };
    }

    // If address is a string, parse it
    if (typeof address === 'string') {
      const lines = address.split('\n').filter(line => line.trim());
      return {
        firstName: lines[0]?.split(' ')[0] || 'Customer',
        lastName: lines[0]?.split(' ').slice(1).join(' ') || '',
        address: lines[1] || address,
        apartment: lines[2] && !this.containsCityPattern(lines[2]) ? lines[2] : '',
        city: this.extractCity(lines),
        country: this.extractCountry(lines),
        zipcode: this.extractZipCode(lines),
        phone: order.phoneNumber || ''
      };
    }

    return address;
  }

  private containsCityPattern(line: string): boolean {
    return /\d{5,6}|\w+,\s*\w+/.test(line);
  }

  private extractCity(lines: string[]): string {
    const cityLine = lines.find(line => this.containsCityPattern(line));
    if (cityLine) {
      return cityLine.split(',')[0]?.trim() || '';
    }
    return '';
  }

  private extractCountry(lines: string[]): string {
    const cityLine = lines.find(line => this.containsCityPattern(line));
    if (cityLine && cityLine.includes(',')) {
      const parts = cityLine.split(',');
      if (parts.length >= 2) {
        return parts[1]?.replace(/\d+/g, '').trim() || '';
      }
    }
    return '';
  }

  private extractZipCode(lines: string[]): string {
    const cityLine = lines.find(line => this.containsCityPattern(line));
    if (cityLine) {
      const match = cityLine.match(/\d{5,6}/);
      return match ? match[0] : '';
    }
    return '';
  }
} 