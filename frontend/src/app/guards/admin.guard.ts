import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { UserService } from '../services/user.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  
  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    
    const isLoggedIn = this.userService.isLoggedIn();
    const currentUser = this.userService.getCurrentUser();
    
    console.log('🔒 [Admin Guard] Checking admin access for route:', state.url);
    console.log('🔒 [Admin Guard] User logged in:', isLoggedIn);
    console.log('🔒 [Admin Guard] Current user:', currentUser);
    console.log('🔒 [Admin Guard] User role:', currentUser?.role);
    
    if (!isLoggedIn) {
      console.log('🔒 [Admin Guard] User not logged in, redirecting to login');
      sessionStorage.setItem('redirectUrl', state.url);
      this.router.navigate(['/login']);
      return false;
    }
    
    // Check if user has ADMIN role (support both uppercase and lowercase)
    const isAdmin = currentUser?.role === 'ADMIN' || currentUser?.role === 'admin';
    
    if (isAdmin) {
      console.log('🔒 [Admin Guard] Admin access granted');
      return true;
    } else {
      console.log('🔒 [Admin Guard] User is not admin, redirecting to home');
      alert('Access denied. Admin privileges required.');
      this.router.navigate(['/home']);
      return false;
    }
  }
}

