export interface Product {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  description: string;
  category: string;
  sizes: string[];
  colors: string[];
  images: string[];
  rating: number;
  reviewCount: number;
  inStock: boolean;
  discount?: number;
}

export interface CartItem {
  id: string;
  productId: string;
  name: string;
  price: number;
  size: string;
  color: string;
  quantity: number;
  image: string;
}

export interface Order {
  id: string;
  userId: string;
  items: CartItem[];
  shippingAddress: ShippingAddress;
  paymentDetails: PaymentDetails;
  orderStatus: OrderStatus;
  orderDate: Date;
  totalAmount: number;
  shippingCost: number;
  discountAmount?: number;
  couponCode?: string;
  trackingNumber?: string;
}

export interface ShippingAddress {
  firstName: string;
  lastName: string;
  address: string;
  apartment?: string;
  city: string;
  country: string;
  zipcode: string;
  phone?: string;
}

export interface PaymentDetails {
  method: 'card' | 'upi' | 'paypal' | 'cod';
  cardNumber?: string;
  cardholderName?: string;
  expiryMonth?: string;
  expiryYear?: string;
  cvc?: string;
  upiId?: string;
  paypalEmail?: string;
}

export type OrderStatus = 'pending' | 'confirmed' | 'processing' | 'shipped' | 'delivered' | 'cancelled';

export interface OrderTracking {
  orderId: string;
  status: OrderStatus;
  statusHistory: OrderStatusHistory[];
  estimatedDelivery?: Date;
  trackingNumber?: string;
}

export interface OrderStatusHistory {
  status: OrderStatus;
  timestamp: Date;
  description: string;
  location?: string;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  addresses: ShippingAddress[];
}

export interface CreateOrderRequest {
  shippingAddress: string;
  billingAddress?: string;
  phoneNumber?: string;
  email?: string;
  notes?: string;
  paymentMethod: string;
  taxAmount: number;
  shippingAmount: number;
  discountAmount?: number;
  couponCode?: string;
} 