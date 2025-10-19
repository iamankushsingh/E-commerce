import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { UserService } from './user.service';

export interface WishlistItem {
	id: number;
	productId: number;
	productName: string;
	price: number;
	category: string;
	imageUrl: string;
	addedAt?: string;
	// Backward compatibility properties
	name?: string;
	size?: string;
	image?: string;
}

export interface WishlistCollection {
	id: number;
	name: string;
	userId: number;
	items: WishlistItem[];
	createdAt?: string;
	updatedAt?: string;
}

export interface CreateCollectionRequest {
	name: string;
	userId: number;
}

export interface AddItemRequest {
	productId: number;
	productName: string;
	price: number;
	category: string;
	imageUrl: string;
}

export interface WishlistStats {
	userId: number;
	totalCollections: number;
	totalItems: number;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
	private readonly apiUrl = 'http://localhost:8089/api/wishlist';
	private readonly collectionsSignal = signal<WishlistCollection[]>([]);
	private readonly pendingItemSignal = signal<WishlistItem | null>(null);

	readonly collections = this.collectionsSignal;
	readonly pendingItem = this.pendingItemSignal;

	private collectionsSubject = new BehaviorSubject<WishlistCollection[]>([]);
	public collections$ = this.collectionsSubject.asObservable();

	constructor(private http: HttpClient, private userService: UserService) {
		// Only load collections if user is logged in
		if (this.userService.isLoggedIn()) {
			this.loadUserCollections();
		}
		
		// Subscribe to auth state changes to clear wishlist on logout
		this.userService.authState$.subscribe(authState => {
			if (!authState.isLoggedIn) {
				// User logged out, clear wishlist data
				console.log('💝 [Wishlist Service] User logged out, clearing wishlist');
				this.collectionsSignal.set([]);
				this.collectionsSubject.next([]);
				this.pendingItemSignal.set(null);
			}
		});
	}

	/**
	 * Load all collections for the current user
	 */
	loadUserCollections(): void {
		const userId = this.getCurrentUserId();
		if (!userId) {
			console.warn('No user ID available, cannot load wishlist collections');
			this.collectionsSignal.set([]);
			this.collectionsSubject.next([]);
			return;
		}
		
		this.getUserCollections(userId).subscribe({
			next: (collections) => {
				this.collectionsSignal.set(collections);
				this.collectionsSubject.next(collections);
			},
			error: (error) => {
				console.error('Error loading collections:', error);
				// Fallback to empty array on error
				this.collectionsSignal.set([]);
				this.collectionsSubject.next([]);
			}
		});
	}

	/**
	 * Get all collections for a user
	 */
	getUserCollections(userId: number): Observable<WishlistCollection[]> {
		return this.http.get<WishlistCollection[]>(`${this.apiUrl}/user/${userId}/collections`)
			.pipe(
				catchError(error => {
					console.error('Error fetching collections:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Create a new collection
	 */
	createCollection(name: string): Observable<WishlistCollection> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		const request: CreateCollectionRequest = {
			name: name,
			userId: userId
		};

		return this.http.post<WishlistCollection>(`${this.apiUrl}/collections`, request)
			.pipe(
				tap((newCollection) => {
					const currentCollections = this.collectionsSignal();
					const updatedCollections = [...currentCollections, newCollection];
					this.collectionsSignal.set(updatedCollections);
					this.collectionsSubject.next(updatedCollections);
				}),
				catchError(error => {
					console.error('Error creating collection:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Delete a collection
	 */
	deleteCollection(collectionId: number): Observable<void> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		return this.http.delete<void>(`${this.apiUrl}/collections/${collectionId}/user/${userId}`)
			.pipe(
				tap(() => {
					const currentCollections = this.collectionsSignal();
					const updatedCollections = currentCollections.filter(c => c.id !== collectionId);
					this.collectionsSignal.set(updatedCollections);
					this.collectionsSubject.next(updatedCollections);
				}),
				catchError(error => {
					console.error('Error deleting collection:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Add item to collection
	 */
	addItemToCollection(collectionId: number, item: WishlistItem): Observable<WishlistItem> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		const request: AddItemRequest = {
			productId: item.productId || item.id, // Handle both old and new format
			productName: item.productName || item.name || '',
			price: item.price,
			category: item.category || item.size || '',
			imageUrl: item.imageUrl || item.image || ''
		};

		return this.http.post<WishlistItem>(`${this.apiUrl}/collections/${collectionId}/user/${userId}/items`, request)
			.pipe(
				tap((newItem) => {
					const currentCollections = this.collectionsSignal();
					const updatedCollections = currentCollections.map(collection => {
						if (collection.id === collectionId) {
							return {
								...collection,
								items: [...collection.items, newItem]
							};
						}
						return collection;
					});
					this.collectionsSignal.set(updatedCollections);
					this.collectionsSubject.next(updatedCollections);
				}),
				catchError(error => {
					console.error('Error adding item to collection:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Remove item from collection
	 */
	removeItemFromCollection(collectionId: number, itemId: number, category?: string): Observable<void> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		return this.http.delete<void>(`${this.apiUrl}/collections/${collectionId}/user/${userId}/items/${itemId}`)
			.pipe(
				tap(() => {
					const currentCollections = this.collectionsSignal();
					const updatedCollections = currentCollections.map(collection => {
						if (collection.id === collectionId) {
							return {
								...collection,
								items: collection.items.filter(item => item.id !== itemId)
							};
						}
						return collection;
					});
					this.collectionsSignal.set(updatedCollections);
					this.collectionsSubject.next(updatedCollections);
				}),
				catchError(error => {
					console.error('Error removing item from collection:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Set pending item (for adding to collection)
	 */
	setPendingItem(item: WishlistItem): void {
		this.pendingItemSignal.set(item);
	}

	/**
	 * Clear pending item
	 */
	clearPendingItem(): void {
		this.pendingItemSignal.set(null);
	}

	/**
	 * Check if a product is in user's wishlist
	 */
	isProductInWishlist(productId: number): Observable<boolean> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		return this.http.get<{exists: boolean}>(`${this.apiUrl}/user/${userId}/product/${productId}/exists`)
			.pipe(
				map(response => response.exists),
				catchError(error => {
					console.error('Error checking product in wishlist:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Get wishlist statistics
	 */
	getWishlistStats(): Observable<WishlistStats> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		return this.http.get<WishlistStats>(`${this.apiUrl}/user/${userId}/stats`)
			.pipe(
				catchError(error => {
					console.error('Error fetching wishlist stats:', error);
					return throwError(() => error);
				})
			);
	}

	/**
	 * Get all items for user across all collections
	 */
	getAllUserItems(): Observable<WishlistItem[]> {
		const userId = this.getCurrentUserId();
		if (!userId) {
			return throwError(() => new Error('User not authenticated'));
		}
		
		return this.http.get<WishlistItem[]>(`${this.apiUrl}/user/${userId}/items`)
			.pipe(
				catchError(error => {
					console.error('Error fetching user items:', error);
					return throwError(() => error);
				})
			);
	}
	
	/**
	 * Get current user ID from authentication service
	 */
	private getCurrentUserId(): number | null {
		const currentUser = this.userService.getCurrentUser();
		if (currentUser?.id) {
			return Number(currentUser.id);
		}
		return null;
	}
	
	/**
	 * Clear all wishlist data (call on logout)
	 */
	clearWishlistData(): void {
		console.log('🧹 [Wishlist Service] Clearing wishlist data');
		this.collectionsSignal.set([]);
		this.collectionsSubject.next([]);
		this.pendingItemSignal.set(null);
	}

	// Backward compatibility methods for existing component
	createCollectionSync(name: string): void {
		this.createCollection(name).subscribe({
			next: () => {
				console.log('Collection created successfully');
			},
			error: (error) => {
				console.error('Failed to create collection:', error);
				alert('Failed to create collection. Please try again.');
			}
		});
	}

	deleteCollectionSync(collectionId: number): void {
		this.deleteCollection(collectionId).subscribe({
			next: () => {
				console.log('Collection deleted successfully');
			},
			error: (error) => {
				console.error('Failed to delete collection:', error);
				alert('Failed to delete collection. Please try again.');
			}
		});
	}

	addItemToCollectionSync(collectionId: number, item: WishlistItem): void {
		this.addItemToCollection(collectionId, item).subscribe({
			next: () => {
				console.log('Item added to collection successfully');
				this.clearPendingItem();
			},
			error: (error) => {
				console.error('Failed to add item to collection:', error);
				alert('Failed to add item to collection. Please try again.');
			}
		});
	}

	removeItemFromCollectionSync(collectionId: number, itemId: number, category?: string): void {
		this.removeItemFromCollection(collectionId, itemId, category).subscribe({
			next: () => {
				console.log('Item removed from collection successfully');
			},
			error: (error) => {
				console.error('Failed to remove item from collection:', error);
				alert('Failed to remove item from collection. Please try again.');
			}
		});
	}
} 