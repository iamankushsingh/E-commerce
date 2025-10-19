import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Product, ProductFilters, PaginatedResult } from '../model/product-listing.model';

@Injectable({
  providedIn: 'root'
})
export class ProductListingService {
  // Use USER endpoint, not admin endpoint
  private readonly baseUrl = 'http://localhost:8084/api/products';

  constructor(private http: HttpClient) {}

  getProducts(filters: ProductFilters = {}): Observable<PaginatedResult<Product>> {
    let params = new HttpParams();
    
    // Set pagination - Convert 1-based frontend pages to 0-based backend pages
    const frontendPage = filters.page || 1;
    const backendPage = Math.max(0, frontendPage - 1); // Convert 1-based to 0-based
    params = params.set('page', backendPage.toString());
    params = params.set('pageSize', (filters.pageSize || 6).toString());
    
    // Set filters - only add non-empty values
    if (filters.category && filters.category.trim() !== '') params = params.set('category', filters.category);
    if (filters.search && filters.search.trim() !== '') params = params.set('search', filters.search.trim());
    if (filters.minPrice !== undefined && filters.minPrice !== null && filters.minPrice !== '') {
      // Handle both string and number types from HTML number inputs
      const minPriceValue = typeof filters.minPrice === 'string' ? filters.minPrice.trim() : filters.minPrice.toString();
      const minPrice = Number(minPriceValue);
      if (!isNaN(minPrice) && minPrice >= 0) params = params.set('minPrice', minPrice.toString());
    }
    if (filters.maxPrice !== undefined && filters.maxPrice !== null && filters.maxPrice !== '') {
      // Handle both string and number types from HTML number inputs
      const maxPriceValue = typeof filters.maxPrice === 'string' ? filters.maxPrice.trim() : filters.maxPrice.toString();
      const maxPrice = Number(maxPriceValue);
      if (!isNaN(maxPrice) && maxPrice >= 0) params = params.set('maxPrice', maxPrice.toString());
    }

    return this.http.get<any>(`${this.baseUrl}`, { params })
      .pipe(
        map(response => ({
          data: response.data.map((product: any) => this.transformProduct(product)),
          total: response.total,
          page: response.page, // ✅ Backend already returns 1-based pages - no conversion needed
          pageSize: response.pageSize,
          totalPages: response.totalPages
        })),
        catchError(this.handleError)
      );
  }

  getProduct(id: string): Observable<Product | undefined> {
    return this.http.get<any>(`${this.baseUrl}/${id}`)
      .pipe(
        map(product => this.transformProduct(product)),
        catchError(error => {
          if (error.status === 404) {
            return throwError(() => new Error('Product not found'));
          }
          return this.handleError(error);
        })
      );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/categories`)
      .pipe(catchError(this.handleError));
  }

  searchProducts(searchTerm: string, filters: ProductFilters = {}): Observable<PaginatedResult<Product>> {
    return this.getProducts({ ...filters, search: searchTerm });
  }

  getProductsByCategory(category: string, filters: ProductFilters = {}): Observable<PaginatedResult<Product>> {
    return this.getProducts({ ...filters, category });
  }

  getFeaturedProducts(limit: number = 6): Observable<Product[]> {
    // For featured products, always get page 0 (first page) from backend
    return this.getProducts({ pageSize: limit, page: 1 }).pipe(
      map(result => result.data)
    );
  }

  private transformProduct(product: any): Product {
    return {
      id: product.id?.toString() || '',
      name: product.name || '',
      description: product.description || '',
      price: product.price || 0,
      category: product.category || '',
      imageUrl: this.ensureValidImageUrl(product.imageUrl || ''),
      stock: product.stock || 0,
      status: product.status as 'active' | 'inactive',
      createdAt: new Date(product.createdAt || Date.now()),
      updatedAt: new Date(product.updatedAt || Date.now())
    };
  }

  private ensureValidImageUrl(imageUrl: string): string {
    // If no image URL provided, return fallback
    if (!imageUrl || imageUrl.trim() === '') {
      return 'assets/placeholder.png';
    }

    // If it's already a full URL, return as-is
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
    }

    // If it's a relative path, make it absolute
    if (imageUrl.startsWith('/')) {
      return imageUrl;
    }

    // If it's just a filename, add assets prefix
    if (!imageUrl.startsWith('assets/')) {
      return `assets/${imageUrl}`;
    }

    return imageUrl;
  }

  private handleError(error: any): Observable<never> {
    console.error('ProductListingService error:', error);
    return throwError(() => new Error(error.message || 'Server error'));
  }
} 