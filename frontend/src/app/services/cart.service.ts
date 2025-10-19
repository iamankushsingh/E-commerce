import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, map, BehaviorSubject } from 'rxjs';
import { UserService } from './user.service';

export interface CartItem {
	id: number;
	productId: number;
	productName: string;
	unitPrice: number;
	quantity: number;
	totalPrice: number;
	productImageUrl: string;
	createdAt?: string;
	updatedAt?: string;
}

export interface Cart {
	id: number;
	userId: number;
	cartItems: CartItem[];
	totalAmount: number;
	totalItems: number;
	createdAt: string;
	updatedAt: string;
}

export interface AddToCartRequest {
	productId: number;
	quantity: number;
	productName?: string;
	unitPrice?: number;
	productImageUrl?: string;
}

export interface UpdateCartItemRequest {
	quantity: number;
}

export interface ApiResponse<T> {
	success: boolean;
	message: string;
	cart?: T;
	itemsCount?: number;
	isValid?: boolean;
}

@Injectable({
	providedIn: 'root'
})
export class CartService {
	private http = inject(HttpClient);
	private userService = inject(UserService);
	private readonly apiUrl = 'http://localhost:8087/api/cart';
	
	private readonly cartSignal = signal<Cart | null>(null);
	private readonly cartItemsSignal = computed(() => 
		this.cartSignal()?.cartItems || []
	);

	readonly cartItems = this.cartItemsSignal;
	readonly cartItemCount = computed(() =>
		this.cartSignal()?.totalItems || 0
	);
	readonly cartTotal = computed(() =>
		this.cartSignal()?.totalAmount || 0
	);

	constructor() {
		// Only load cart if user is logged in
		if (this.userService.isLoggedIn()) {
			this.loadCart();
		}
		
		// Subscribe to auth state changes to clear cart on logout
		this.userService.authState$.subscribe(authState => {
			if (!authState.isLoggedIn) {
				// User logged out, clear cart data
				console.log('🛒 [Cart Service] User logged out, clearing cart');
				this.cartSignal.set(null);
				this.clearDiscountInfo();
			}
		});
	}

	/**
	 * Load cart from backend
	 */
	private loadCart(): void {
		const userId = this.getCurrentUserId();
		if (!userId) {
			console.warn('🛒 [Cart Service] No user ID available, cannot load cart');
			this.cartSignal.set(null);
			return;
		}
		
		console.log('🛒 [Cart Service] Loading cart for user ID:', userId);
		console.log('🛒 [Cart Service] Current user from service:', this.userService.getCurrentUser());
		console.log('🛒 [Cart Service] User logged in?', this.userService.isLoggedIn());
		
		this.http.get<ApiResponse<Cart>>(`${this.apiUrl}/${userId}`)
			.pipe(
				catchError(error => {
					console.error('🛒 [Cart Service] Error loading cart:', error);
					console.error('🛒 [Cart Service] Error status:', error.status);
					console.error('🛒 [Cart Service] Error message:', error.message);
					return of({ success: false, message: 'Failed to load cart', cart: null });
				})
			)
			.subscribe(response => {
				console.log('🛒 [Cart Service] Load cart response:', response);
				if (response.success && response.cart) {
					this.cartSignal.set(response.cart);
					console.log('🛒 [Cart Service] Cart loaded successfully:', response.cart);
					// Debug cart item images
					response.cart.cartItems.forEach((item, index) => {
						console.log(`🖼️ [Cart Service] Item ${index + 1} Image URL:`, item.productImageUrl);
					});
				} else {
					console.warn('🛒 [Cart Service] Failed to load cart or cart is empty');
				}
			});
	}

	/**
	 * Get cart items (computed signal)
	 */
	getCartItems(): CartItem[] {
		return this.cartItemsSignal();
	}

	/**
	 * Add item to cart
	 */
	addToCart(productId: number, quantity: number = 1, productName?: string, unitPrice?: number, productImageUrl?: string): Observable<boolean> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			console.error('🛒 [Cart Service] User not authenticated, cannot add to cart');
			return of(false);
		}
		
		const request: AddToCartRequest = {
			productId,
			quantity,
			productName,
			unitPrice,
			productImageUrl
		};

		console.log('🛒 [Cart Service] Adding to cart for user ID:', userId);
		console.log('🛒 [Cart Service] Request payload:', JSON.stringify(request));
		console.log('🛒 [Cart Service] API URL:', `${this.apiUrl}/${userId}/items`);
		
		return this.http.post<ApiResponse<Cart>>(`${this.apiUrl}/${userId}/items`, request)
			.pipe(
				map(response => {
					console.log('🛒 [Cart Service] API Response:', response);
					if (response.success && response.cart) {
						this.cartSignal.set(response.cart);
						console.log('🛒 [Cart Service] Cart updated successfully:', response.cart);
						return true;
					} else {
						console.error('🛒 [Cart Service] API returned unsuccessful response:', response);
						return false;
					}
				}),
				catchError(error => {
					console.error('🛒 [Cart Service] API Error:', error);
					console.error('🛒 [Cart Service] Error status:', error.status);
					console.error('🛒 [Cart Service] Error message:', error.message);
					console.error('🛒 [Cart Service] Error body:', error.error);
					return of(false);
				})
			);
	}

	/**
	 * Remove item from cart
	 */
	removeFromCart(cartItemId: number): Observable<boolean> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			console.error('User not authenticated, cannot remove from cart');
			return of(false);
		}
		
		return this.http.delete<ApiResponse<Cart>>(`${this.apiUrl}/${userId}/items/${cartItemId}`)
			.pipe(
				map(response => {
					if (response.success && response.cart) {
						this.cartSignal.set(response.cart);
						return true;
					}
					return false;
				}),
				catchError(error => {
					console.error('Error removing from cart:', error);
					return of(false);
				})
			);
	}

	/**
	 * Clear entire cart
	 */
	clearCart(): Observable<boolean> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			console.error('User not authenticated, cannot clear cart');
			return of(false);
		}
		
		return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${userId}`)
			.pipe(
				map(response => {
					if (response.success) {
						this.cartSignal.set(null);
						this.clearDiscountInfo(); // Clear discount info when cart is cleared
						return true;
					}
					return false;
				}),
				catchError(error => {
					console.error('Error clearing cart:', error);
					return of(false);
				})
			);
	}

	/**
	 * Update cart item quantity
	 */
	updateQuantity(cartItemId: number, quantity: number): Observable<boolean> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			console.error('User not authenticated, cannot update quantity');
			return of(false);
		}
		
		const request: UpdateCartItemRequest = { quantity };

		return this.http.put<ApiResponse<Cart>>(`${this.apiUrl}/${userId}/items/${cartItemId}`, request)
			.pipe(
				map(response => {
					if (response.success && response.cart) {
						this.cartSignal.set(response.cart);
						return true;
					}
					return false;
				}),
				catchError(error => {
					console.error('Error updating quantity:', error);
					return of(false);
				})
			);
	}

	/**
	 * Get cart items count
	 */
	getCartItemsCount(): Observable<number> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return of(0);
		}
		
		return this.http.get<ApiResponse<any>>(`${this.apiUrl}/${userId}/count`)
			.pipe(
				map(response => response.itemsCount || 0),
				catchError(error => {
					console.error('Error getting cart count:', error);
					return of(0);
				})
			);
	}

	/**
	 * Validate cart for checkout
	 */
	validateCartForCheckout(): Observable<boolean> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return of(false);
		}
		
		return this.http.get<ApiResponse<any>>(`${this.apiUrl}/${userId}/validate`)
			.pipe(
				map(response => response.isValid || false),
				catchError(error => {
					console.error('Error validating cart:', error);
					return of(false);
				})
			);
	}

	/**
	 * Get subtotal from current cart
	 */
	getSubtotal(): number {
		return this.cartSignal()?.totalAmount || 0;
	}

	/**
	 * Calculate delivery charge based on subtotal
	 */
	getDeliveryCharge(): number {
		const subtotal = this.getSubtotal();
		return subtotal < 200 ? 30 : 0;
	}

	/**
	 * Calculate tax (5% of subtotal)
	 */
	getTax(): number {
		const subtotal = this.getSubtotal();
		return Math.round(subtotal * 0.05);
	}

	/**
	 * Apply coupon code and return discount details
	 */
	applyCoupon(couponCode: string): { success: boolean; discount: number; message: string } {
		const subtotal = this.getSubtotal();
		if (couponCode === 'ABOVE400' && subtotal >= 400) {
			return { success: true, discount: 40, message: 'Coupon applied! Rs 40 discount' };
		} else if (couponCode === 'ABOVE1000' && subtotal >= 1000) {
			return { success: true, discount: 100, message: 'Coupon applied! Rs 100 discount' };
		} else if (couponCode === 'ABOVE400' && subtotal < 400) {
			return { success: false, discount: 0, message: 'Minimum order of Rs 400 required for this coupon' };
		} else if (couponCode === 'ABOVE1000' && subtotal < 1000) {
			return { success: false, discount: 0, message: 'Minimum order of Rs 1000 required for this coupon' };
		} else {
			return { success: false, discount: 0, message: 'Invalid coupon code' };
		}
	}

	  /**
   * Get total amount including taxes and delivery, minus discount
   */
  getTotal(discount: number = 0): number {
    return this.getSubtotal() + this.getDeliveryCharge() + this.getTax() - discount;
  }

  /**
   * Store discount information for checkout
   */
  setDiscountInfo(discount: number, couponCode: string): void {
    sessionStorage.setItem('appliedDiscount', discount.toString());
    sessionStorage.setItem('couponCode', couponCode);
  }

  /**
   * Get discount information for checkout
   */
  getDiscountInfo(): { appliedDiscount: number; couponCode: string } {
    const discount = parseFloat(sessionStorage.getItem('appliedDiscount') || '0');
    const couponCode = sessionStorage.getItem('couponCode') || '';
    return { appliedDiscount: discount, couponCode };
  }

  /**
   * Clear discount information
   */
  clearDiscountInfo(): void {
    sessionStorage.removeItem('appliedDiscount');
    sessionStorage.removeItem('couponCode');
  }

	/**
	 * Refresh cart data from backend
	 */
	refreshCart(): void {
		this.loadCart();
	}



	/**
	 * Refresh cart when user authentication state changes
	 */
	refreshCartAfterAuth(): void {
		console.log('🛒 [Cart Service] Refreshing cart after authentication change');
		const newUserId = this.getCurrentUserId();
		console.log('🛒 [Cart Service] Loading cart for user ID:', newUserId);
		this.loadCart();
	}

	/**
	 * Get current user ID from authentication service
	 */
	getCurrentUserId(): number | null {
		const currentUser = this.userService.getCurrentUser();
		console.log('🛒 [Cart Service] Getting current user ID - currentUser:', currentUser);
		
		if (currentUser?.id) {
			const userId = Number(currentUser.id);
			console.log('🛒 [Cart Service] Final user ID:', userId);
			return userId;
		}
		
		console.warn('🛒 [Cart Service] No authenticated user found');
		return null;
	}
	
	/**
	 * Clear all cart data (call on logout)
	 */
	clearCartData(): void {
		console.log('🧹 [Cart Service] Clearing cart data');
		this.cartSignal.set(null);
		this.clearDiscountInfo();
	}
} 