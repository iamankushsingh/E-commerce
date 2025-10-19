import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../services/product.service';
import { Product, ProductFilters } from '../../../model/product.model';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  categories: string[] = [];
  loading = true;
  
  filters: ProductFilters = {};
  showDeleteModal = false;
  productToDelete?: Product;

  // Pagination properties
  currentPage = 0; // Backend uses 0-based pagination
  pageSize = 12; // Show 12 products per page
  totalProducts = 0;
  totalPages = 0;

  // Debounced search subject
  private searchSubject = new Subject<string>();

  constructor(private productService: ProductService) {
    // Set up debounced search
    this.searchSubject.pipe(
      debounceTime(500), // Wait 500ms after user stops typing
      distinctUntilChanged() // Only emit if the value actually changed
    ).subscribe((searchTerm) => {
      this.filters.search = searchTerm;
      this.currentPage = 0; // Reset to first page when searching
      this.loadProducts();
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getProductsPaginated(this.filters, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.products = response.data;
        this.filteredProducts = response.data;
        this.totalProducts = response.total;
        this.totalPages = response.totalPages;
        this.loading = false;
        
        console.log('📊 [Admin Products] Loaded page:', {
          currentPage: this.currentPage + 1, // Display 1-based for user
          totalPages: this.totalPages,
          totalProducts: this.totalProducts,
          productsOnPage: this.products.length
        });
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  applyFilters(): void {
    this.loadProducts();
  }

  // Handle search input changes with debouncing
  onSearchChange(searchTerm: string): void {
    this.searchSubject.next(searchTerm);
  }

  // Handle non-search filter changes (immediate)
  applyNonSearchFilters(): void {
    this.currentPage = 0; // Reset to first page when filters change
    this.loadProducts();
  }

  clearFilters(): void {
    this.filters = {};
    this.currentPage = 0; // Reset to first page
    this.loadProducts();
  }

  // Pagination methods
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadProducts();
    }
  }

  getPageNumbers(): number[] {
    const pages = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages, start + 5);
    
    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }

  openDeleteModal(product: Product): void {
    this.productToDelete = product;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.productToDelete = undefined;
  }

  confirmDelete(): void {
    if (this.productToDelete) {
      this.productService.deleteProduct(this.productToDelete.id).subscribe({
        next: () => {
          this.loadProducts();
          this.closeDeleteModal();
        },
        error: (error) => {
          console.error('Error deleting product:', error);
          this.closeDeleteModal();
        }
      });
    }
  }

  getStatusColor(status: string): string {
    return status === 'active' ? '#22c55e' : '#ef4444';
  }

  getStockStatus(stock: number): { text: string; color: string } {
    if (stock === 0) {
      return { text: 'Out of Stock', color: '#ef4444' };
    } else if (stock < 10) {
      return { text: 'Low Stock', color: '#f59e0b' };
    } else {
      return { text: 'In Stock', color: '#22c55e' };
    }
  }

  onImageError(event: any, category: string): void {
    // Set fallback image based on category (same as user product listing)
    const fallbackImages = {
      'Electronics': 'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=400&auto=format&fit=crop',
      'Clothing': 'https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=400&auto=format&fit=crop', 
      'Food & Beverage': 'https://images.unsplash.com/photo-1506459225024-1428097a7e18?q=80&w=400&auto=format&fit=crop',
      'Home & Garden': 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=400&auto=format&fit=crop'
    };
    
    const fallback = fallbackImages[category as keyof typeof fallbackImages] || 
                    'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=400&auto=format&fit=crop';
                    
    event.target.src = fallback;
  }
}
