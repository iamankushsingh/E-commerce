# Order Management Module

The Order Management module provides client-side creation, storage, retrieval, and tracking of orders for the storefront. It is implemented as an Angular service with a reactive API and localStorage persistence for demo purposes.

## Location

- Service: `frontend/src/app/services/order.ts`
- Related components:
  - `frontend/src/app/component/order-confirmation/` (order confirmation view)
  - `frontend/src/app/component/order-tracking/` (order tracking view)
  - `frontend/src/app/component/checkout/` (invokes order creation)

## Highlights

- Injectable service (`providedIn: 'root'`)
- Reactive stream of all orders via `orders$`
- Local persistence using `localStorage` (key: `ecommerce_orders`)
- Mock seed orders for easy development
- Utility helpers for status text/color, cancellation, and tracking

## Quick Start

```ts
import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order';
import { CartItem, ShippingAddress, PaymentDetails, Order } from '../../model/interfaces';

@Component({ selector: 'example-usage', template: '' })
export class ExampleUsageComponent implements OnInit {
	orders: Order[] = [];

	constructor(private orderService: OrderService) {}

	ngOnInit() {
		// reactively observe all orders
		this.orderService.getAllOrders().subscribe(orders => (this.orders = orders));
	}

	placeOrder(items: CartItem[], address: ShippingAddress, payment: PaymentDetails, total: number, shipping: number) {
		const created = this.orderService.createOrder(items, address, payment, total, shipping);
		console.log('Created order:', created.id);
	}
}
```

The checkout flow already uses this service to create an order and navigate to confirmation. See `frontend/src/app/component/checkout/checkout.ts`.

## Public API (OrderService)

- `orders$: Observable<Order[]>`
  - Reactive stream of all known orders (most-recent first).
- `createOrder(items, shippingAddress, paymentDetails, totalAmount, shippingCost): Order`
  - Creates and persists a new order. Generates an order ID and tracking number. Immediately emits via `orders$`. Simulates auto-advance to `confirmed` status after ~2s.
- `getOrderById(orderId: string): Order | undefined`
- `getOrdersByUserId(userId: string): Order[]`
- `getUserOrders(): Order[]`
  - Returns orders for the current user (demo uses a hardcoded user id `current-user`).
- `getAllOrders(): Observable<Order[]>`
- `updateOrderStatus(orderId: string, newStatus: OrderStatus): void`
- `getOrderTracking(orderId: string): OrderTracking | null`
  - Returns current status, status history, tracking number, and estimated delivery date (computed heuristically from order date + status).
- `canCancelOrder(order: Order): boolean`
  - Returns true for cancellable states (`pending`, `confirmed`).
- `cancelOrder(orderId: string): boolean`
  - Cancels if allowed and returns success flag.
- `getOrderStatusText(status: OrderStatus): string`
- `getOrderStatusColor(status: OrderStatus): string`

## Models (from `frontend/src/app/model/interfaces.ts`)

- `Order`
  - `id`, `userId`, `items: CartItem[]`, `shippingAddress: ShippingAddress`, `paymentDetails: PaymentDetails`, `orderStatus: OrderStatus`, `orderDate: Date`, `totalAmount: number`, `shippingCost: number`, `trackingNumber?: string`
- `CartItem`
- `ShippingAddress`
- `PaymentDetails` (one of `card | upi | paypal`; card numbers are masked for storage)
- `OrderStatus` (`pending | confirmed | processing | shipped | delivered | cancelled`)
- `OrderTracking` (status, statusHistory, trackingNumber, estimatedDelivery)

Refer to the interfaces file for exact fields.

## Persistence & Seeding

- Uses `localStorage` key `ecommerce_orders` to save/load orders.
- On first run (no saved orders), seeds two mock orders for demonstration.
- To reset, clear the key in DevTools or call a custom clear in development.

## Order Lifecycle & Tracking

- New orders start as `pending`. The service simulates automatic transition to `confirmed` ~2 seconds after creation.
- `getOrderTracking` synthesizes a status history timeline and an estimated delivery date using the order date and current status.

## Usage Patterns

- Checkout (creation): See `Checkout.processOrder()` in `component/checkout/checkout.ts`.
- Confirmation view: Reads the created order by id and shows summary.
- Tracking view: Uses `getOrderTracking(orderId)` and status helpers to render progress.

## Notes & Limitations

- This is a client-only demo implementation. Replace storage and id/tracking generation with backend APIs for production.
- Payment details are stored in masked form; never persist sensitive data in plaintext.
- `getUserOrders()` is hardcoded to `'current-user'` for demo purposes; integrate with your auth/user service to scope properly.

## Extending

- Add server synchronization by swapping `load/save` with HTTP calls.
- Replace the mock status advancement with server-driven webhooks or polling.
- Add pagination and filtering on top of `orders$` if needed for large datasets. 