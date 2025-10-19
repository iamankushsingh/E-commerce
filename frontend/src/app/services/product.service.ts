import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Product, ProductFilters } from '../model/product.model';

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly baseUrl = 'http://localhost:8084/api/admin/products';

  constructor(private http: HttpClient) {}

  getProducts(filters?: ProductFilters): Observable<Product[]> {
    let params = new HttpParams();
    
    if (filters) {
      // Only add filter parameters if they are not empty strings or undefined
      if (filters.category && filters.category.trim() !== '') params = params.set('category', filters.category);
      if (filters.status && filters.status.trim() !== '') params = params.set('status', filters.status);
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
    }

    // For admin, we want all products without pagination for the current implementation
    return this.http.get<PaginatedResponse<Product>>(`${this.baseUrl}`, { params })
      .pipe(
        map(response => response.data.map(product => this.transformProduct(product))),
        catchError(this.handleError)
      );
  }

  getProductsPaginated(filters?: ProductFilters, page: number = 0, size: number = 10): Observable<PaginatedResponse<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (filters) {
      // Only add filter parameters if they are not empty strings or undefined
      if (filters.category && filters.category.trim() !== '') params = params.set('category', filters.category);
      if (filters.status && filters.status.trim() !== '') params = params.set('status', filters.status);
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
    }

    return this.http.get<PaginatedResponse<Product>>(`${this.baseUrl}`, { params })
      .pipe(
        map(response => ({
          ...response,
          data: response.data.map(product => this.transformProduct(product))
        })),
        catchError(this.handleError)
      );
  }

  getProduct(id: string): Observable<Product | undefined> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`)
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

  createProduct(product: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>): Observable<Product> {
    const createData = {
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      imageUrl: product.imageUrl || '',
      stock: product.stock,
      status: product.status
    };

    return this.http.post<Product>(`${this.baseUrl}`, createData)
      .pipe(
        map(product => this.transformProduct(product)),
        catchError(this.handleError)
      );
  }

  updateProduct(id: string, product: Partial<Product>): Observable<Product> {
    const updateData = {
      id: parseInt(id),
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      imageUrl: product.imageUrl,
      stock: product.stock,
      status: product.status
    };

    return this.http.put<Product>(`${this.baseUrl}/${id}`, updateData)
      .pipe(
        map(product => this.transformProduct(product)),
        catchError(this.handleError)
      );
  }

  deleteProduct(id: string): Observable<boolean> {
    return this.http.delete<{message: string, success: boolean}>(`${this.baseUrl}/${id}`)
      .pipe(
        map(response => response.success),
        catchError(this.handleError)
      );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/categories`)
      .pipe(catchError(this.handleError));
  }

  getProductStats(): Observable<{totalProducts: number, activeProducts: number, inactiveProducts: number}> {
    return this.http.get<{totalProducts: number, activeProducts: number, inactiveProducts: number}>(`${this.baseUrl}/stats`)
      .pipe(catchError(this.handleError));
  }

  private transformProduct(product: any): Product {
    return {
      id: product.id?.toString() || '',
      name: product.name || '',
      description: product.description || '',
      price: product.price || 0,
      category: product.category || '',
      imageUrl: product.imageUrl || '',
      stock: product.stock || 0,
      status: product.status as 'active' | 'inactive',
      createdAt: new Date(product.createdAt),
      updatedAt: new Date(product.updatedAt)
    };
  }

  private handleError(error: any): Observable<never> {
    console.error('An error occurred:', error);
    return throwError(() => new Error(error.message || 'Server error'));
  }
}
