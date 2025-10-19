import { Component, inject, OnInit, OnDestroy, computed, effect } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { UserService } from '../../services/user.service';
import { User } from '../../model/user.model';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header implements OnInit, OnDestroy {
  cartItemCount = computed(() => this.cartService.cartItemCount());
  wishlistItemCount = computed(() => {
    const collections = this.wishlistService.collections();
    return collections.reduce((total, col) => total + col.items.length, 0);
  });

  currentUser: User | null = null;
  isLoggedIn = false;
  private authSubscription?: Subscription;

  constructor(
    private router: Router,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private userService: UserService
  ) {}

  ngOnInit() {
    // Cart count is now computed from signals
    // Subscribe to authentication state
    this.authSubscription = this.userService.authState$.subscribe(authState => {
      this.isLoggedIn = authState.isLoggedIn;
      this.currentUser = authState.user;
    });
    
    // Also check initial state immediately
    const initialState = this.userService.isLoggedIn();
    this.isLoggedIn = initialState;
    this.currentUser = this.userService.getCurrentUser();
  }

  ngOnDestroy() {
    // Unsubscribe from authentication state
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  goToCart() {
    this.router.navigate(['/cart']);
  }

  goToWishlist() {
    this.router.navigate(['/wishlist']);
  }

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin');
  }

  // Check if current route should hide navbar (login/register pages)
  shouldHideNavbar(): boolean {
    const hiddenRoutes = ['/login', '/register'];
    return hiddenRoutes.some(route => this.router.url.startsWith(route));
  }

  // Method to get current cart count
  getCartCount(): number {
    return this.cartItemCount();
  }

  // Method to get current wishlist count
  getWishlistCount(): number {
    return this.wishlistItemCount();
  }

  // Get user name for display
  getUserName(): string {
    if (this.currentUser) {
      return this.currentUser.firstName || this.currentUser.username || 'User';
    }
    return '';
  }

  // Logout functionality
  logout(): void {
    this.userService.logout();
  }
}
