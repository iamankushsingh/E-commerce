import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../../../services/product.service';
import { Product } from '../../../model/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  productForm: FormGroup;
  categories: string[] = [];
  isEditMode = false;
  productId?: string;
  loading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0)]],
      category: ['', Validators.required],
      imageUrl: ['', [Validators.required, Validators.pattern('https?://.+')]],
      stock: ['', [Validators.required, Validators.min(0)]],
      status: ['active', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.productId = params['id'];
        this.loadProduct();
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

  loadProduct(): void {
    if (this.productId) {
      this.loading = true;
      this.productService.getProduct(this.productId).subscribe({
        next: (product) => {
          if (product) {
            this.productForm.patchValue({
              name: product.name,
              description: product.description,
              price: product.price,
              category: product.category,
              imageUrl: product.imageUrl,
              stock: product.stock,
              status: product.status
            });
          }
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading product:', error);
          this.loading = false;
        }
      });
    }
  }

  onSubmit(): void {
    if (this.productForm.valid) {
      this.saving = true;
      const formValue = this.productForm.value;

      const productData = {
        name: formValue.name,
        description: formValue.description,
        price: Number(formValue.price),
        category: formValue.category,
        imageUrl: formValue.imageUrl,
        stock: Number(formValue.stock),
        status: formValue.status
      };

      if (this.isEditMode && this.productId) {
        this.productService.updateProduct(this.productId, productData).subscribe({
          next: () => {
            this.saving = false;
            this.router.navigate(['/admin/products']);
          },
          error: (error) => {
            console.error('Error updating product:', error);
            this.saving = false;
          }
        });
      } else {
        this.productService.createProduct(productData).subscribe({
          next: () => {
            this.saving = false;
            this.router.navigate(['/admin/products']);
          },
          error: (error) => {
            console.error('Error creating product:', error);
            this.saving = false;
          }
        });
      }
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin/products']);
  }

  getFieldError(fieldName: string): string {
    const field = this.productForm.get(fieldName);
    if (field && field.invalid && field.touched) {
      if (field.errors?.['required']) {
        return `${fieldName} is required`;
      }
      if (field.errors?.['minlength']) {
        return `${fieldName} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors?.['min']) {
        if (fieldName === 'price') {
          return `Price must be greater than Rs 0`;
        }
        return `${fieldName} must be greater than ${field.errors['min'].min}`;
      }
      if (field.errors?.['pattern']) {
        return `${fieldName} must be a valid URL`;
      }
    }
    return '';
  }

  private markFormGroupTouched(): void {
    Object.keys(this.productForm.controls).forEach(key => {
      const control = this.productForm.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }
}
