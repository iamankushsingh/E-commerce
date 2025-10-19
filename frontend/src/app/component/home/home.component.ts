import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductListingService } from '../../services/product-listing.service';
import { Product } from '../../model/product-listing.model';
import { ProductCardListingComponent } from '../product-card-listing/product-card-listing.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, ProductCardListingComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  featuredProducts = signal<Product[]>([]);
  categories = signal<string[]>([]);

  constructor(private productService: ProductListingService) {}

  ngOnInit(): void {
    this.loadFeaturedProducts();
    this.loadCategories();
  }

  private loadFeaturedProducts(): void {
    // Use the new getFeaturedProducts method for better performance
    this.productService.getFeaturedProducts(8).subscribe(products => {
      this.featuredProducts.set(products);
    });
  }

  private loadCategories(): void {
    this.productService.getCategories().subscribe(categories => {
      this.categories.set(categories);
    });
  }

  onAddToCart(event: {product: Product, quantity: number}): void {
    // Handle add to cart from featured products
  }

  onAddToWishlist(event: {product: Product, inWishlist: boolean}): void {
    // Handle add to wishlist from featured products
  }

  getCategoryImage(category: string): string {
    const categoryImages = {
      'Electronics': 'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=600&auto=format&fit=crop',
      'Clothing': 'https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=600&auto=format&fit=crop',
      'Food & Beverage': 'https://images.unsplash.com/photo-1506459225024-1428097a7e18?q=80&w=600&auto=format&fit=crop',
      'Home & Garden': 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=600&auto=format&fit=crop'
    };
    return categoryImages[category as keyof typeof categoryImages] || 'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=600&auto=format&fit=crop';
  }

  getCategoryDescription(category: string): string {
    const descriptions = {
      'Electronics': 'Latest gadgets and tech accessories',
      'Clothing': 'Fashion and style for every occasion',
      'Food & Beverage': 'Premium quality food and drinks',
      'Home & Garden': 'Beautiful items for your living space'
    };
    return descriptions[category as keyof typeof descriptions] || 'Quality products at great prices';
  }
}
