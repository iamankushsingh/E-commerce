import { Routes } from '@angular/router';
import { RegisterComponent } from './component/register/register';
import { LoginComponent } from './component/login/login';
import { Checkout } from './component/checkout/checkout';
import { CartComponent } from './component/cart/cart';
import { WishlistComponent } from './component/wishlist/wishlist.component';
import { OrderConfirmation } from './component/order-confirmation/order-confirmation';
import { OrderTrackingComponent } from './component/order-tracking/order-tracking';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ProductsComponent } from './component/admin/products/products.component';
import { ProductFormComponent } from './component/admin/product-form/product-form.component';
import { UsersComponent } from './component/admin/users/users.component';
import { OrdersComponent } from './component/admin/orders/orders.component';
import { ProductsListingComponent } from './pages/products-listing/products-listing.component';
import { ProductDetailListingComponent } from './pages/product-detail-listing/product-detail-listing.component';
import { HomeComponent } from './component/home/home.component';
import { ProfileComponent } from './component/profile/profile';
import { UserOrdersComponent } from './component/user-orders/user-orders.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  
  // Protected user routes - require authentication
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'my-orders', component: UserOrdersComponent, canActivate: [AuthGuard] },
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'wishlist', component: WishlistComponent, canActivate: [AuthGuard] },
  { path: 'checkout', component: Checkout, canActivate: [AuthGuard] },
  { path: 'order-confirmation/:id', component: OrderConfirmation, canActivate: [AuthGuard] },
  { path: 'order-confirmation', component: OrderConfirmation, canActivate: [AuthGuard] },
  { path: 'order-tracking/:id', component: OrderTrackingComponent, canActivate: [AuthGuard] },
  
  // Public routes
  { path: 'home', component: HomeComponent },
  { path: 'product-listing', component: ProductsListingComponent },
  { path: 'product-listing/:id', component: ProductDetailListingComponent },

  // Admin routes - require authentication AND admin role
  { path: 'admin', redirectTo: '/admin/dashboard', pathMatch: 'full' },
  { 
    path: 'admin/dashboard', 
    component: DashboardComponent,
    canActivate: [AdminGuard]
  },
  { 
    path: 'admin/products', 
    component: ProductsComponent,
    canActivate: [AdminGuard]
  },
  { 
    path: 'admin/products/new', 
    component: ProductFormComponent,
    canActivate: [AdminGuard]
  },
  { 
    path: 'admin/products/edit/:id', 
    component: ProductFormComponent,
    canActivate: [AdminGuard]
  },
  { 
    path: 'admin/users', 
    component: UsersComponent,
    canActivate: [AdminGuard]
  },
  { 
    path: 'admin/orders', 
    component: OrdersComponent,
    canActivate: [AdminGuard]
  },

];
