import { Injectable, signal } from '@angular/core';
import { Product } from '../model/product-listing.model';

export interface CartItem {
	product: Product;
	quantity: number;
}

@Injectable({
	providedIn: 'root'
})
export class CartListingService {
	private cartItems = signal<CartItem[]>([]);
	private wishlistItems = signal<Product[]>([]);

	// Cart methods
	getCartItems() {
		return this.cartItems();
	}

	addToCart(product: Product, quantity: number): void {
		const items = this.cartItems();
		const existingItemIndex = items.findIndex(item => item.product.id === product.id);
		
		if (quantity === 0) {
			// Remove item from cart
			if (existingItemIndex > -1) {
				const newItems = [...items];
				newItems.splice(existingItemIndex, 1);
				this.cartItems.set(newItems);
			}
		} else {
			if (existingItemIndex > -1) {
				// Update existing item quantity
				const newItems = [...items];
				newItems[existingItemIndex].quantity = quantity;
				this.cartItems.set(newItems);
			} else {
				// Add new item to cart
				this.cartItems.set([...items, { product, quantity }]);
			}
		}
	}

	getCartQuantity(productId: string): number {
		const items = this.cartItems();
		const item = items.find(item => item.product.id === productId);
		return item ? item.quantity : 0;
	}

	isInCart(productId: string): boolean {
		return this.getCartQuantity(productId) > 0;
	}

	// Wishlist methods
	getWishlistItems() {
		return this.wishlistItems();
	}

	toggleWishlist(product: Product): boolean {
		const items = this.wishlistItems();
		const existingIndex = items.findIndex(item => item.id === product.id);
		
		if (existingIndex > -1) {
			// Remove from wishlist
			const newItems = [...items];
			newItems.splice(existingIndex, 1);
			this.wishlistItems.set(newItems);
			return false;
		} else {
			// Add to wishlist
			this.wishlistItems.set([...items, product]);
			return true;
		}
	}

	isInWishlist(productId: string): boolean {
		const items = this.wishlistItems();
		return items.some(item => item.id === productId);
	}

	// Get totals
	getCartTotal(): number {
		return this.cartItems().reduce((total, item) => total + (item.product.price * item.quantity), 0);
	}

	getCartItemCount(): number {
		return this.cartItems().reduce((total, item) => total + item.quantity, 0);
	}

	getWishlistCount(): number {
		return this.wishlistItems().length;
	}
} 