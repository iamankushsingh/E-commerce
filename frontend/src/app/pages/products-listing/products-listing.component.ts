import { Component, OnInit, computed, effect, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Product, ProductFilters, PaginatedResult } from '../../model/product-listing.model';
import { ProductListingService } from '../../services/product-listing.service';
import { ProductCardListingComponent } from '../../component/product-card-listing/product-card-listing.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
	selector: 'app-products-listing',
	standalone: true,
	imports: [CommonModule, FormsModule, ProductCardListingComponent],
	templateUrl: './products-listing.component.html',
	styleUrls: ['./products-listing.component.css']
})
export class ProductsListingComponent implements OnInit {
	filters: ProductFilters = { page: 1, pageSize: 6 };
	displayed = signal<Product[]>([]);
	categories = signal<string[]>([]);
	currentPage = signal<number>(1);
	totalPages = signal<number>(1);
	totalProducts = signal<number>(0);
	pageSize = 6;
	Math = Math;

	// Debounced search subject
	private searchSubject = new Subject<string>();

	constructor(
		private productService: ProductListingService,
		private route: ActivatedRoute
	) {
		// Set up debounced search
		this.searchSubject.pipe(
			debounceTime(500), // Wait 500ms after user stops typing
			distinctUntilChanged() // Only emit if the value actually changed
		).subscribe((searchTerm) => {
			this.filters.search = searchTerm;
			this.filters.page = 1; // Reset to first page when searching
			this.loadProducts();
		});
	}

	ngOnInit(): void {
		// Check for category query parameter and set the filter
		this.route.queryParams.subscribe(params => {
			if (params['category']) {
				this.filters.category = params['category'];
			}
		});
		this.load();
	}

	private load(): void {
		this.productService.getCategories().subscribe(cs => this.categories.set(cs));
		this.applyFilters();
	}

	applyFilters(): void {
		this.filters.page = 1; // Reset to first page when filters change

		this.loadProducts();
	}

	// Handle search input changes with debouncing
	onSearchChange(searchTerm: string): void {
		this.searchSubject.next(searchTerm);
	}

	// Handle non-search filter changes (immediate)
	applyNonSearchFilters(): void {
		this.filters.page = 1; // Reset to first page when filters change
		this.loadProducts();
	}

	private loadProducts(): void {
		console.log('🔍 [Products Listing] Loading products with filters:', this.filters);
		this.productService.getProducts(this.filters).subscribe((result: PaginatedResult<Product>) => {
			console.log('📊 [Products Listing] API Response:', {
				totalProducts: result.total,
				currentPage: result.page,
				totalPages: result.totalPages,
				pageSize: result.pageSize,
				dataLength: result.data.length
			});
			
			this.displayed.set(result.data);
			this.totalPages.set(result.totalPages);
			this.totalProducts.set(result.total);
			this.currentPage.set(result.page);
			
			console.log('🎯 [Products Listing] Updated signals:', {
				displayed: this.displayed().length,
				currentPage: this.currentPage(),
				totalPages: this.totalPages(),
				totalProducts: this.totalProducts()
			});
		});
	}

	clearFilters(): void {
		this.filters = { page: 1, pageSize: 6 };
		this.currentPage.set(1);
		this.loadProducts();
	}

	goToPage(page: number): void {
		console.log(`🔄 [Products Listing] goToPage called with page: ${page}, current: ${this.currentPage()}, total: ${this.totalPages()}`);
		
		if (page >= 1 && page <= this.totalPages()) {
			this.filters.page = page;
			this.currentPage.set(page);
			console.log(`✅ [Products Listing] Page change accepted. New filters:`, this.filters);
			this.loadProducts();
			
			// Improved scroll to top functionality
			setTimeout(() => {
				// Try multiple scroll targets for better compatibility
				const scrollTargets = [
					'.scrollable-content',
					'.content-area', 
					'.grid',
					'main'
				];
				
				let scrolled = false;
				for (const target of scrollTargets) {
					const element = document.querySelector(target);
					if (element && !scrolled) {
						console.log(`📜 [Products Listing] Scrolling to: ${target}`);
						element.scrollIntoView({ 
							behavior: 'smooth', 
							block: 'start',
							inline: 'nearest' 
						});
						scrolled = true;
						break;
					}
				}
				
				// Fallback: scroll window to top if no element found
				if (!scrolled) {
					console.log(`📜 [Products Listing] Fallback: Scrolling window to top`);
					window.scrollTo({ 
						top: 0, 
						behavior: 'smooth' 
					});
				}
			}, 100); // Small delay to ensure content is loaded
		} else {
			console.log(`❌ [Products Listing] Page change rejected. Page ${page} is out of bounds (1-${this.totalPages()})`);
		}
	}

	getPageNumbers(): number[] {
		const total = this.totalPages();
		const current = this.currentPage();
		const pages: number[] = [];
		
		// Show up to 5 page numbers
		const start = Math.max(1, current - 2);
		const end = Math.min(total, start + 4);
		
		for (let i = start; i <= end; i++) {
			pages.push(i);
		}
		
		return pages;
	}

	onAddToCart(event: {product: Product, quantity: number}): void {
		if (event.quantity === 0) {
			// Item removed from cart
		} else {
			// Item added or quantity updated in cart
		}
	}

	onAddToWishlist(event: {product: Product, inWishlist: boolean}): void {
		if (event.inWishlist) {
			// Item added to wishlist
		} else {
			// Item removed from wishlist
		}
	}
} 