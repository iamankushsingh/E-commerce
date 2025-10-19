export interface Order {
  id: string;
  orderNumber?: string; // For backend compatibility
  userId?: string; // For backend compatibility
  customerName?: string; // For admin display
  customerEmail?: string; // For admin display
  email?: string; // For backend compatibility
  items: OrderItem[];
  totalAmount: number;
  finalAmount?: number; // For backend compatibility
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
  orderStatus?: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled'; // For backend compatibility
  orderDate: Date;
  createdAt?: Date; // For backend compatibility
  shippingAddress: Address | string; // Support both object and string format
  paymentMethod: string;
  trackingNumber?: string;
}

export interface OrderItem {
  id?: string; // For backend compatibility
  productId: string;
  productName: string;
  name?: string; // Alias for productName
  quantity: number;
  price: number;
  unitPrice?: number; // Backend compatibility (BigDecimal from backend)
  total: number;
  totalPrice?: number; // Backend compatibility (BigDecimal from backend)
  productImageUrl?: string; // For backend compatibility
  imageUrl?: string; // Alternative property name
  productCategory?: string; // For backend compatibility
  category?: string; // Alternative property name
  productDescription?: string; // For backend compatibility
  description?: string; // Alternative property name
}

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface OrderFilters {
  status?: string;
  dateFrom?: string; // HTML date input returns string in 'YYYY-MM-DD' format
  dateTo?: string;   // HTML date input returns string in 'YYYY-MM-DD' format
  search?: string;
}
