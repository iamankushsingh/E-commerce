import { Component, OnInit, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router} from '@angular/router';
import { Product } from '../../model/product-listing.model';
import { ProductListingService } from '../../services/product-listing.service';
import { CartListingService } from '../../services/cart-listing.service';

@Component({
	selector: 'app-product-detail-listing',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './product-detail-listing.component.html',
	styleUrls: ['./product-detail-listing.component.css']
})
export class ProductDetailListingComponent implements OnInit {
	product = signal<Product | null>(null);
	quantity = signal(1);
	isLoading = true;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private productService: ProductListingService,
		private cartService: CartListingService
	) {
		// Set up effect to sync with cart service
		effect(() => {
			const prod = this.product();
			if (prod) {
				this.quantity.set(this.cartService.getCartQuantity(prod.id) || 1);
			}
		});
	}

	ngOnInit(): void {
		const id = this.route.snapshot.paramMap.get('id');
		if (id) {
			this.loadProduct(id);
		}
	}

	loadProduct(id: string): void {
		this.productService.getProduct(id).subscribe({
			next: (product) => {
				this.product.set(product || null);
				this.isLoading = false;
				// Sync quantity with cart service
				if (product) {
					this.quantity.set(this.cartService.getCartQuantity(product.id) || 1);
				}
			},
			error: (error) => {
				console.error('Error loading product:', error);
				this.product.set(null);
				this.isLoading = false;
			}
		});
	}

	increaseQuantity(): void {
		const current = this.quantity();
		const prod = this.product();
		if (prod && current < prod.stock) {
			const newQuantity = current + 1;
			this.quantity.set(newQuantity);
			this.cartService.addToCart(prod, newQuantity);
		}
	}

	decreaseQuantity(): void {
		const current = this.quantity();
		const prod = this.product();
		if (prod) {
			if (current > 1) {
				const newQuantity = current - 1;
				this.quantity.set(newQuantity);
				this.cartService.addToCart(prod, newQuantity);
			} else if (current === 1) {
				// Remove from cart - go back to Add to Cart button
				this.quantity.set(1);
				this.cartService.addToCart(prod, 0);
			}
		}
	}

	goBack(): void {
		this.router.navigate(['/product-listing']);
	}

	addToCart(): void {
		const prod = this.product();
		if (prod && prod.stock > 0) {
			this.quantity.set(1);
			this.cartService.addToCart(prod, 1);
		}
	}

	addToWishlist(): void {
		const prod = this.product();
		if (prod) {
			this.cartService.toggleWishlist(prod);
		}
	}

	isInCart(): boolean {
		const prod = this.product();
		return prod ? this.cartService.isInCart(prod.id) : false;
	}

	isInWishlist(): boolean {
		const prod = this.product();
		return prod ? this.cartService.isInWishlist(prod.id) : false;
	}

	onImageError(event: any): void {
		// Fallback images based on category
		const prod = this.product();
		if (prod) {
			const fallbackImages = {
				'Electronics': 'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=400&auto=format&fit=crop',
				'Clothing': 'https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=400&auto=format&fit=crop',
				'Food & Beverage': 'https://images.unsplash.com/photo-1506459225024-1428097a7e18?q=80&w=400&auto=format&fit=crop',
				'Home & Garden': 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=400&auto=format&fit=crop'
			};
			
			const fallback = fallbackImages[prod.category as keyof typeof fallbackImages] || 
							'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=400&auto=format&fit=crop';
							
			event.target.src = fallback;
		}
	}

	getBriefDescription(): string {
		const prod = this.product();
		if (!prod) return '';

		// Generate category-specific brief descriptions
		const categoryDescriptions: Record<string, string> = {
			'Electronics': `This ${prod.name.toLowerCase()} is a premium electronic device designed for modern users. It combines cutting-edge technology with user-friendly features to deliver exceptional performance and reliability.`,
			'Clothing': `This ${prod.name.toLowerCase()} is crafted with high-quality materials and attention to detail. Perfect for both casual and formal occasions, it offers comfort, style, and durability.`,
			'Food & Beverage': `This premium ${prod.name.toLowerCase()} is carefully selected for its superior quality and taste. Made with natural ingredients and traditional methods to ensure the best flavor experience.`,
			'Home & Garden': `This ${prod.name.toLowerCase()} is designed to enhance your living space with both functionality and aesthetic appeal. Built to last with quality materials and thoughtful design.`
		};

		return categoryDescriptions[prod.category] || `This ${prod.name.toLowerCase()} offers excellent quality and value. ${prod.description}`;
	}
} 