import { Component, EventEmitter, Input, Output, signal, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product } from '../../model/product-listing.model';
import { Router, RouterLink } from '@angular/router';
import { CartListingService } from '../../services/cart-listing.service';
import { CartService, CartItem } from '../../services/cart.service';
import { WishlistService, WishlistItem, WishlistCollection } from '../../services/wishlist.service';
import { UserService } from '../../services/user.service';

@Component({
	selector: 'app-product-card-listing',
	standalone: true,
	imports: [CommonModule, RouterLink],
	templateUrl: './product-card-listing.component.html',
	styleUrls: ['./product-card-listing.component.css']
})
export class ProductCardListingComponent implements OnInit {
	@Input() product!: Product
	@Output() addToCart = new EventEmitter<{product: Product, quantity: number}>();
	@Output() addToWishlist = new EventEmitter<{product: Product, inWishlist: boolean}>();

	quantity = signal(1);

	constructor(
		private cartListingService: CartListingService,
		private mainCartService: CartService,
		private wishlistService: WishlistService,
		private router: Router,
		private userService: UserService
	) {
		// Remove effect to prevent double updates
	}

	ngOnInit(): void {
		// Initialize state from main cart service (backend)
		if (this.product) {
			const cartItems = this.mainCartService.cartItems();
			const existingItem = cartItems.find((item: CartItem) => item.productId === parseInt(this.product.id));
			if (existingItem) {
				this.quantity.set(existingItem.quantity);
			}
		}
	}

	isInCart(): boolean {
		if (!this.product) return false;
		const cartItems = this.mainCartService.cartItems();
		return cartItems.some((item: CartItem) => item.productId === parseInt(this.product.id));
	}

	isInWishlist(): boolean {
		if (!this.product) return false;
		const collections = this.wishlistService.collections();
		return collections.some((collection: WishlistCollection) => 
			collection.items.some((item: WishlistItem) => 
				(item.productId && item.productId.toString() === this.product.id) ||
				(item.id && item.id.toString() === this.product.id)
			)
		);
	}

	addToCartAction(): void {
		console.log('🛒 [Product Card] Adding to cart:', this.product.name);
		
		// Check if user is logged in
		if (!this.userService.isLoggedIn()) {
			console.log('🔐 [Product Card] User not logged in, redirecting to login');
			// Store the current URL to redirect back after login
			sessionStorage.setItem('redirectUrl', this.router.url);
			this.router.navigate(['/login']);
			return;
		}
		
		// Add to main cart service (backend)
		this.addToMainCart();
		
		// Also add to cart listing service for backward compatibility
		this.cartListingService.addToCart(this.product, this.quantity());
		
		// Emit event for parent components
		this.addToCart.emit({
			product: this.product,
			quantity: this.quantity()
		});
	}

	addToCartWithQuantity(): void {
		// Check if user is logged in
		if (!this.userService.isLoggedIn()) {
			console.log('🔐 [Product Card] User not logged in, redirecting to login');
			// Store the current URL to redirect back after login
			sessionStorage.setItem('redirectUrl', this.router.url);
			this.router.navigate(['/login']);
			return;
		}
		
		if (this.product.stock > 0) {
			this.quantity.set(1);
			this.cartListingService.addToCart(this.product, 1);
			this.addToMainCart();
		}
	}

	increaseQuantity(): void {
		const current = this.quantity();
		if (current < this.product.stock) {
			const newQuantity = current + 1;
			this.quantity.set(newQuantity);
			this.updateCartQuantity(newQuantity);
		}
	}

	decreaseQuantity(): void {
		const current = this.quantity();
		if (current > 1) {
			const newQuantity = current - 1;
			this.quantity.set(newQuantity);
			this.updateCartQuantity(newQuantity);
		} else if (current === 1) {
			// Remove from cart - go back to Add to Cart button
			this.quantity.set(1);
			this.cartListingService.addToCart(this.product, 0);
		}
	}

	private addToMainCart(): void {
		const productId = parseInt(this.product.id);
		const quantity = this.quantity();
		
		console.log('🛒 [Product Card] Calling main cart service with:', {
			productId,
			quantity,
			name: this.product.name,
			price: this.product.price,
			imageUrl: this.product.imageUrl
		});

		this.mainCartService.addToCart(
			productId,
			quantity,
			this.product.name,
			this.product.price,
			this.product.imageUrl
		).subscribe({
			next: (success) => {
				console.log('🛒 [Product Card] Add to cart result:', success);
			},
			error: (error) => {
				console.error('❌ [Product Card] Error adding item to cart:', error);
			}
		});
	}

	private updateCartQuantity(newQuantity: number): void {
		// Update both cart services
		this.cartListingService.addToCart(this.product, newQuantity);
		
		const productId = parseInt(this.product.id);
		this.mainCartService.addToCart(
			productId,
			newQuantity,
			this.product.name,
			this.product.price,
			this.product.imageUrl
		).subscribe({
			next: (success) => {
				console.log('🛒 [Product Card] Quantity updated:', newQuantity);
			},
			error: (error) => {
				console.error('❌ [Product Card] Error updating quantity:', error);
				// Revert quantity on error
				const cartItems = this.mainCartService.cartItems();
				const existingItem = cartItems.find((item: CartItem) => item.productId === productId);
				if (existingItem) {
					this.quantity.set(existingItem.quantity);
				}
			}
		});
	}

	toggleWishlist(): void {
		const isCurrentlyInWishlist = this.isInWishlist();

		if (isCurrentlyInWishlist) {
			// Remove from all collections where this product exists
			const collections = this.wishlistService.collections();
			collections.forEach((collection: WishlistCollection) => {
				const itemToRemove = collection.items.find((item: WishlistItem) => 
					(item.productId && item.productId.toString() === this.product.id) ||
					(item.id && item.id.toString() === this.product.id)
				);
				if (itemToRemove) {
					this.wishlistService.removeItemFromCollectionSync(
						collection.id, 
						itemToRemove.id, 
						itemToRemove.category || (itemToRemove as any).size
					);
				}
			});
		} else {
			// Create wishlist item from product with new format
			const wishlistItem: WishlistItem = {
				id: parseInt(this.product.id),
				productId: parseInt(this.product.id),
				productName: this.product.name,
				price: this.product.price,
				category: this.product.category,
				imageUrl: this.product.imageUrl,
				// Also include old format for backward compatibility
				name: this.product.name,
				size: this.product.category,
				image: this.product.imageUrl
			} as any;

			// Set as pending item and redirect to wishlist page
			this.wishlistService.setPendingItem(wishlistItem);
			
			// Navigate to wishlist page so user can choose/create collection
			this.router.navigate(['/wishlist']);
		}

		// Also toggle in the cart listing service for backward compatibility
		this.cartListingService.toggleWishlist(this.product);
	}

	onImageError(event: any) {
		// Set fallback image based on category
		const fallbackImages = {
			'Electronics': '/assets/placeholder-electronics.png',
			'Clothing': '/assets/placeholder-clothing.png',
			'Books': '/assets/placeholder-books.png',
			'default': '/assets/placeholder.png'
		};
		
		event.target.src = fallbackImages[this.product?.category as keyof typeof fallbackImages] || fallbackImages.default;
	}
} 