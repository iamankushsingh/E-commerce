import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService, CartItem } from '../../services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cart.html',
  styleUrls: ['./cart.scss']
})
export class CartComponent {
  couponCode: string = '';
  appliedDiscount: number = 0;
  couponMessage: string = '';
  couponApplied: boolean = false;
  returnPolicyExpanded: boolean = false;
  deliveryChargesExpanded: boolean = false;

  constructor(public cartService: CartService, private router: Router) {}

  /**
   * TrackBy function for cart items to improve performance
   */
  trackByItemId(index: number, item: CartItem): number {
    return item.id;
  }

  removeItem(cartItemId: number): void {
    this.cartService.removeFromCart(cartItemId).subscribe({
      next: (success) => {
        if (success) {
          this.resetCoupon();
          console.log('Item removed from cart successfully');
        } else {
          console.error('Failed to remove item from cart');
        }
      },
      error: (error) => {
        console.error('Error removing item from cart:', error);
      }
    });
  }

  clearCart(): void {
    if (confirm('Are you sure you want to clear all items from your cart?')) {
      this.cartService.clearCart().subscribe({
        next: (success) => {
          if (success) {
            this.resetCoupon();
            console.log('Cart cleared successfully');
          } else {
            console.error('Failed to clear cart');
          }
        },
        error: (error) => {
          console.error('Error clearing cart:', error);
        }
      });
    }
  }

  updateQuantity(cartItemId: number, change: number): void {
    const currentItems = this.cartService.cartItems();
    const item = currentItems.find(cartItem => cartItem.id === cartItemId);
    if (item) {
      const newQuantity = item.quantity + change;
      if (newQuantity > 0) {
        this.cartService.updateQuantity(cartItemId, newQuantity).subscribe({
          next: (success) => {
            if (success) {
              this.resetCoupon();
              console.log('Quantity updated successfully');
            } else {
              console.error('Failed to update quantity');
            }
          },
          error: (error) => {
            console.error('Error updating quantity:', error);
          }
        });
      } else {
        // Remove item if quantity becomes 0
        this.removeItem(cartItemId);
      }
    }
  }

  getSubtotal(): number {
    return this.cartService.getSubtotal();
  }

  getDeliveryCharge(): number {
    return this.cartService.getDeliveryCharge();
  }

  /**
   * Get tax amount from cart service
   */
  getTax(): number {
    return this.cartService.getTax();
  }

  getTotal(): number {
    return this.cartService.getTotal(this.appliedDiscount);
  }

  onCheckout(): void {
    if (this.cartService.cartItems().length === 0) {
      alert('Your cart is empty. Please add items before checking out.');
      return;
    }
    
    // Validate cart before checkout
    this.cartService.validateCartForCheckout().subscribe({
      next: (isValid) => {
        if (isValid) {
          this.router.navigate(['/checkout']);
        } else {
          alert('Some items in your cart are no longer available. Please review your cart.');
        }
      },
      error: (error) => {
        console.error('Error validating cart:', error);
        alert('Unable to proceed to checkout. Please try again.');
      }
    });
  }

  applyCoupon(): void {
    if (this.couponCode.trim()) {
      const result = this.cartService.applyCoupon(this.couponCode.trim().toUpperCase());
      this.couponMessage = result.message;
      
      if (result.success) {
        this.appliedDiscount = result.discount;
        this.couponApplied = true;
        // Store discount info for checkout
        this.cartService.setDiscountInfo(result.discount, this.couponCode.trim().toUpperCase());
      } else {
        this.appliedDiscount = 0;
        this.couponApplied = false;
        this.cartService.clearDiscountInfo();
      }
    } else {
      this.couponMessage = 'Please enter a coupon code';
      this.couponApplied = false;
    }
  }

  resetCoupon(): void {
    this.appliedDiscount = 0;
    this.couponApplied = false;
    this.couponMessage = '';
    this.couponCode = '';
    this.cartService.clearDiscountInfo();
  }

  /**
   * Handle image loading errors with category-based fallbacks
   */
  onImageError(event: any): void {
    // Set fallback image based on a default placeholder or category-based images
    const fallbackImages = [
      'https://images.unsplash.com/photo-1498049794561-7780e7231661?q=80&w=400&auto=format&fit=crop', // Electronics
      'https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=400&auto=format&fit=crop', // Clothing
      'https://images.unsplash.com/photo-1506459225024-1428097a7e18?q=80&w=400&auto=format&fit=crop', // Food
      'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=400&auto=format&fit=crop', // Home
      'assets/placeholder.png'
    ];
    
    // Use a random fallback or the placeholder
    const randomFallback = fallbackImages[Math.floor(Math.random() * (fallbackImages.length - 1))];
    event.target.src = randomFallback || 'assets/placeholder.png';
  }

  toggleReturnPolicy(): void {
    this.returnPolicyExpanded = !this.returnPolicyExpanded;
  }

  toggleDeliveryCharges(): void {
    this.deliveryChargesExpanded = !this.deliveryChargesExpanded;
  }
}
