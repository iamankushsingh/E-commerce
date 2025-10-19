import { Injectable } from '@angular/core';
import { Observable, of, delay, throwError } from 'rxjs';
import { Order, OrderFilters } from '../model/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private orders: Order[] = [
    {
      id: 'ORD-001',
      customerName: 'John Doe',
      customerEmail: 'john.doe@example.com',
      items: [
        {
          productId: '1',
          productName: 'Wireless Bluetooth Headphones',
          quantity: 1,
          price: 299.99,
          total: 299.99
        }
      ],
      totalAmount: 299.99,
      status: 'delivered',
      orderDate: new Date('2024-08-10'),
      shippingAddress: {
        street: '123 Main St',
        city: 'New York',
        state: 'NY',
        zipCode: '10001',
        country: 'USA'
      },
      paymentMethod: 'Credit Card',
      trackingNumber: 'TRK123456789'
    },
    {
      id: 'ORD-002',
      customerName: 'Jane Smith',
      customerEmail: 'jane.smith@example.com',
      items: [
        {
          productId: '2',
          productName: 'Smart Fitness Watch',
          quantity: 1,
          price: 199.99,
          total: 199.99
        },
        {
          productId: '3',
          productName: 'Organic Cotton T-Shirt',
          quantity: 2,
          price: 29.99,
          total: 59.98
        }
      ],
      totalAmount: 259.97,
      status: 'shipped',
      orderDate: new Date('2024-08-15'),
      shippingAddress: {
        street: '456 Oak Ave',
        city: 'Los Angeles',
        state: 'CA',
        zipCode: '90210',
        country: 'USA'
      },
      paymentMethod: 'PayPal',
      trackingNumber: 'TRK987654321'
    },
    {
      id: 'ORD-003',
      customerName: 'Mike Wilson',
      customerEmail: 'mike.wilson@example.com',
      items: [
        {
          productId: '4',
          productName: 'Premium Coffee Beans',
          quantity: 3,
          price: 24.99,
          total: 74.97
        }
      ],
      totalAmount: 74.97,
      status: 'processing',
      orderDate: new Date('2024-08-17'),
      shippingAddress: {
        street: '789 Pine St',
        city: 'Chicago',
        state: 'IL',
        zipCode: '60601',
        country: 'USA'
      },
      paymentMethod: 'Credit Card'
    },
    {
      id: 'ORD-004',
      customerName: 'Sarah Jones',
      customerEmail: 'sarah.jones@example.com',
      items: [
        {
          productId: '5',
          productName: 'Eco-Friendly Water Bottle',
          quantity: 1,
          price: 34.99,
          total: 34.99
        }
      ],
      totalAmount: 34.99,
      status: 'pending',
      orderDate: new Date('2024-08-18'),
      shippingAddress: {
        street: '321 Elm St',
        city: 'Miami',
        state: 'FL',
        zipCode: '33101',
        country: 'USA'
      },
      paymentMethod: 'Credit Card'
    }
  ];

  getOrders(filters?: OrderFilters): Observable<Order[]> {
    let filteredOrders = [...this.orders];

    if (filters) {
      if (filters.status) {
        filteredOrders = filteredOrders.filter(o => o.status === filters.status);
      }
      if (filters.search) {
        const search = filters.search.toLowerCase();
        filteredOrders = filteredOrders.filter(o => 
          o.id.toLowerCase().includes(search) ||
          (o.customerName || '').toLowerCase().includes(search) ||
          (o.customerEmail || o.email || '').toLowerCase().includes(search)
        );
      }
      if (filters.dateFrom) {
        const fromDate = new Date(filters.dateFrom);
        if (!isNaN(fromDate.getTime())) {
          filteredOrders = filteredOrders.filter(o => o.orderDate >= fromDate);
        }
      }
      if (filters.dateTo) {
        const toDate = new Date(filters.dateTo);
        if (!isNaN(toDate.getTime())) {
          // Add 23:59:59 to include the entire end date
          toDate.setHours(23, 59, 59, 999);
          filteredOrders = filteredOrders.filter(o => o.orderDate <= toDate);
        }
      }
    }

    return of(filteredOrders).pipe(delay(500));
  }

  getOrder(id: string): Observable<Order | undefined> {
    const order = this.orders.find(o => o.id === id);
    return of(order).pipe(delay(300));
  }

  updateOrderStatus(id: string, status: Order['status']): Observable<Order> {
    const index = this.orders.findIndex(o => o.id === id);
    if (index === -1) {
      return throwError(() => new Error('Order not found'));
    }
    
    this.orders[index] = {
      ...this.orders[index],
      status
    };
    
    return of(this.orders[index]).pipe(delay(800));
  }

  updateTrackingNumber(id: string, trackingNumber: string): Observable<Order> {
    const index = this.orders.findIndex(o => o.id === id);
    if (index === -1) {
      return throwError(() => new Error('Order not found'));
    }
    
    this.orders[index] = {
      ...this.orders[index],
      trackingNumber
    };
    
    return of(this.orders[index]).pipe(delay(500));
  }
}
