import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WishlistService, WishlistItem } from '../../services/wishlist.service';
import { CartService } from '../../services/cart.service';

@Component({
	selector: 'app-wishlist',
	standalone: true,
	imports: [CommonModule, FormsModule],
	templateUrl: './wishlist.component.html',
	styleUrls: ['./wishlist.component.css']
})
export class WishlistComponent {
	showAddCollectionForm = false;
	newCollectionName = '';

	constructor(public wishlistService: WishlistService, private cartService: CartService) {}

	toggleAddCollection(): void {
		this.showAddCollectionForm = !this.showAddCollectionForm;
		this.newCollectionName = '';
	}

	saveCollection(): void {
		if (!this.newCollectionName.trim()) return;
		
		this.wishlistService.createCollectionSync(this.newCollectionName);
		this.showAddCollectionForm = false;
		this.newCollectionName = '';
	}

	deleteCollection(collectionId: number): void {
		if (confirm('Delete this collection?')) {
			this.wishlistService.deleteCollectionSync(collectionId);
		}
	}

	selectCollection(collectionId: number): void {
		const pendingItem = this.wishlistService.pendingItem();
		if (!pendingItem) return;
		
		this.wishlistService.addItemToCollectionSync(collectionId, pendingItem);
		alert('Item added to collection');
	}

	cancelPendingItem(): void {
		this.wishlistService.clearPendingItem();
	}

	addToCartFromWishlist(collectionId: number, item: WishlistItem): void {
		// Handle both old and new item format
		const productId = item.productId || item.id;
		const productName = item.productName || (item as any).name;
		const imageUrl = item.imageUrl || (item as any).image;
		
		// Add item to cart using new service method
		this.cartService.addToCart(
			productId,
			1,
			productName,
			item.price,
			imageUrl
		).subscribe({
			next: (success) => {
				if (success) {
					alert(`${productName} added to cart!`);
				} else {
					alert('Failed to add item to cart');
				}
			},
			error: (error) => {
				console.error('Error adding item to cart:', error);
				alert('Error adding item to cart');
			}
		});
	}

	removeFromWishlist(collectionId: number, item: WishlistItem): void {
		const itemId = item.id;
		const category = item.category || (item as any).size;
		
		this.wishlistService.removeItemFromCollectionSync(collectionId, itemId, category);
	}
} 