import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../services/order';
import { Order, OrderFilters, Address } from '../../../model/order.model';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit, OnDestroy {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  loading = true;
  
  filters: OrderFilters = {};
  selectedOrder?: Order;
  showOrderDetails = false;
  updatingStatus = false;

  // Store scroll position when modal opens
  private scrollPosition = 0;

  // Debounced search subject
  private searchSubject = new Subject<string>();

  constructor(private orderService: OrderService) {
    // Set up debounced search
    this.searchSubject.pipe(
      debounceTime(500), // Wait 500ms after user stops typing
      distinctUntilChanged() // Only emit if the value actually changed
    ).subscribe((searchTerm) => {
      this.filters.search = searchTerm;
      this.loadOrders();
    });
  }

  ngOnInit(): void {
    this.loadOrders();
  }

  ngOnDestroy(): void {
    // Clean up modal classes if component is destroyed while modal is open
    if (this.showOrderDetails) {
      this.restoreScrollPosition();
    }
  }

  loadOrders(): void {
    this.loading = true;
    
    // Prepare date filters - convert non-empty strings to Date objects
    const dateFrom = this.filters.dateFrom && this.filters.dateFrom.trim() !== '' ? new Date(this.filters.dateFrom) : undefined;
    const dateTo = this.filters.dateTo && this.filters.dateTo.trim() !== '' ? new Date(this.filters.dateTo) : undefined;
    
    this.orderService.getAllOrdersForAdmin(
      undefined, // userId
      this.filters.status, // orderStatus
      undefined, // paymentStatus
      dateFrom, // startDate
      dateTo, // endDate
      0, // page
      50, // size
      'createdAt', // sortBy
      'desc', // sortDirection
      this.filters.search // search
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.orders) {
          // Map backend data to frontend format
          this.orders = this.mapBackendOrdersToFrontend(response.orders.content || response.orders);
          this.filteredOrders = this.orders;
        } else {
          this.orders = [];
          this.filteredOrders = [];
        }
        this.loading = false;
      },
      error: (error: any) => {
        console.error('❌ [Admin Orders] API Error:', error);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.loadOrders();
  }

  // Handle search input changes with debouncing
  onSearchChange(searchTerm: string): void {
    this.searchSubject.next(searchTerm);
  }

  // Handle non-search filter changes (immediate)
  applyNonSearchFilters(): void {
    this.loadOrders();
  }

  clearFilters(): void {
    this.filters = {};
    this.loadOrders();
  }

  viewOrderDetails(order: Order): void {
    this.selectedOrder = order;
    this.showOrderDetails = true;
    this.preventBackgroundScroll();
    
    // Debug logging to help identify data issues
    console.log('📋 [Admin Orders] Viewing order details:', {
      orderId: order.id,
      orderNumber: order.orderNumber,
      totalAmount: order.totalAmount,
      finalAmount: order.finalAmount,
      itemsCount: order.items?.length || 0,
      items: order.items?.map(item => ({
        name: item.productName || item.name,
        quantity: item.quantity,
        unitPrice: item.unitPrice || item.price,
        totalPrice: item.totalPrice || item.total,
        rawItem: item // Include raw item for debugging
      }))
    });
  }

  closeOrderDetails(): void {
    this.showOrderDetails = false;
    this.selectedOrder = undefined;
    this.restoreScrollPosition();
  }

  // Close modal on Escape key press
  @HostListener('keydown.escape', ['$event'])
  onEscapeKey(event: Event): void {
    if (this.showOrderDetails) {
      this.closeOrderDetails();
    }
  }

  /**
   * Prevent background scrolling when modal opens
   */
  private preventBackgroundScroll(): void {
    // Store current scroll position
    this.scrollPosition = window.pageYOffset || document.documentElement.scrollTop;
    
    // Add modal-open class to prevent scrolling
    document.body.classList.add('modal-open');
    document.documentElement.classList.add('modal-open');
    
    // Set the body's top position to maintain scroll position visually
    document.body.style.top = `-${this.scrollPosition}px`;
  }

  /**
   * Restore background scrolling when modal closes
   */
  private restoreScrollPosition(): void {
    // Remove modal-open class to re-enable scrolling
    document.body.classList.remove('modal-open');
    document.documentElement.classList.remove('modal-open');
    
    // Reset body top position
    document.body.style.top = '';
    
    // Restore scroll position
    window.scrollTo(0, this.scrollPosition);
  }

  updateOrderStatus(orderId: string, newStatus: string): void {
    this.updatingStatus = true;
    this.orderService.updateOrderStatusAdmin(orderId, newStatus).subscribe({
      next: () => {
        this.loadOrders();
        if (this.selectedOrder && this.selectedOrder.id === orderId) {
          this.selectedOrder.status = newStatus as any;
          this.selectedOrder.orderStatus = newStatus as any;
        }
        this.updatingStatus = false;
      },
      error: (error: any) => {
        console.error('Error updating order status:', error);
        this.updatingStatus = false;
      }
    });
  }

  getStatusColor(status: string): string {
    const upperStatus = status?.toUpperCase();
    const colors: Record<string, string> = {
      'PENDING': '#f59e0b',
      'CONFIRMED': '#3b82f6',
      'PROCESSING': '#3b82f6',
      'SHIPPED': '#8b5cf6',
      'DELIVERED': '#22c55e',
      'CANCELLED': '#ef4444',
      'RETURNED': '#ef4444'
    };
    return colors[upperStatus] || '#6b7280';
  }

  getNextStatus(currentStatus: string): string | null {
    const upperStatus = currentStatus?.toUpperCase();
    const statusFlow: Record<string, string | null> = {
      'PENDING': 'CONFIRMED',
      'CONFIRMED': 'PROCESSING',
      'PROCESSING': 'SHIPPED',
      'SHIPPED': 'DELIVERED',
      'DELIVERED': null,
      'CANCELLED': null,
      'RETURNED': null
    };
    return statusFlow[upperStatus] || null;
  }

  canAdvanceStatus(status: string): boolean {
    return this.getNextStatus(status) !== null;
  }

  formatDate(dateInput: string | Date | null | undefined): string {
    let date: Date;
    if (!dateInput) {
      date = new Date();
    } else {
      date = typeof dateInput === 'string' ? new Date(dateInput) : dateInput;
    }
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatAddress(address: string | Address): string {
    if (typeof address === 'string') {
      return address.replace(/\n/g, ', ');
    } else if (address && typeof address === 'object') {
      // Format Address object
      const parts = [
        address.street,
        address.city,
        address.state,
        address.zipCode,
        address.country
      ].filter(Boolean);
      return parts.join(', ');
    }
    return 'No address provided';
  }

  // Helper method to get order status display text
  getStatusDisplayText(status: string): string {
    const upperStatus = status?.toUpperCase();
    const statusMap: Record<string, string> = {
      'PENDING': 'Pending',
      'CONFIRMED': 'Confirmed', 
      'PROCESSING': 'Processing',
      'SHIPPED': 'Shipped',
      'DELIVERED': 'Delivered',
      'CANCELLED': 'Cancelled',
      'RETURNED': 'Returned'
    };
    return statusMap[upperStatus] || status;
  }

  // Helper method to get payment status display text
  getPaymentStatusDisplayText(status: string): string {
    const upperStatus = status?.toUpperCase();
    const statusMap: Record<string, string> = {
      'PENDING': 'Pending',
      'PROCESSING': 'Processing',
      'COMPLETED': 'Completed',
      'FAILED': 'Failed',
      'REFUNDED': 'Refunded',
      'PARTIALLY_REFUNDED': 'Partially Refunded'
    };
    return statusMap[upperStatus] || status;
  }

  // Helper method to get the effective status from either status or orderStatus
  getEffectiveStatus(order: Order): string {
    return order.orderStatus || order.status || 'pending';
  }

  // Map backend order DTOs to frontend Order model
  private mapBackendOrdersToFrontend(backendOrders: any[]): Order[] {
    return backendOrders.map((backendOrder: any) => ({
      id: backendOrder.id?.toString() || backendOrder.orderId?.toString() || '',
      orderNumber: backendOrder.orderNumber,
      userId: backendOrder.userId?.toString(),
      customerName: backendOrder.customerName || `User #${backendOrder.userId}`,
      customerEmail: backendOrder.customerEmail || backendOrder.email,
      email: backendOrder.email,
      items: this.mapBackendOrderItems(backendOrder.orderItems || backendOrder.items || []),
      totalAmount: backendOrder.finalAmount || backendOrder.totalAmount || 0,
      finalAmount: backendOrder.finalAmount,
      discountAmount: backendOrder.discountAmount || 0,
      couponCode: backendOrder.couponCode || undefined,
      status: backendOrder.orderStatus?.toLowerCase() || backendOrder.status?.toLowerCase() || 'pending',
      orderStatus: backendOrder.orderStatus?.toLowerCase() || backendOrder.status?.toLowerCase() || 'pending',
      orderDate: new Date(backendOrder.createdAt || backendOrder.orderDate || new Date()),
      createdAt: new Date(backendOrder.createdAt || backendOrder.orderDate || new Date()),
      shippingAddress: backendOrder.shippingAddress || '',
      paymentMethod: backendOrder.paymentMethod || 'N/A',
      trackingNumber: backendOrder.trackingNumber
    } as Order));
  }

  // Map backend order items to frontend format
  private mapBackendOrderItems(backendItems: any[]): any[] {
    return backendItems.map((backendItem: any) => ({
      id: backendItem.id?.toString() || '',
      productId: backendItem.productId?.toString() || '',
      productName: backendItem.productName || backendItem.name || 'Unknown Product',
      name: backendItem.productName || backendItem.name || 'Unknown Product', // Alias for template compatibility
      quantity: backendItem.quantity || 0,
      price: backendItem.unitPrice || backendItem.price || 0,
      unitPrice: backendItem.unitPrice || backendItem.price || 0, // Alias for template compatibility
      total: backendItem.totalPrice || backendItem.total || (backendItem.unitPrice || backendItem.price || 0) * (backendItem.quantity || 0),
      totalPrice: backendItem.totalPrice || backendItem.total || (backendItem.unitPrice || backendItem.price || 0) * (backendItem.quantity || 0), // Alias for template compatibility
      productImageUrl: backendItem.productImageUrl || backendItem.imageUrl || '',
      productCategory: backendItem.productCategory || backendItem.category || '',
      productDescription: backendItem.productDescription || backendItem.description || ''
    }));
  }
}
