import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { UserService } from '../services/user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    
    const isLoggedIn = this.userService.isLoggedIn();
    
    console.log('🔒 [Auth Guard] Checking authentication for route:', state.url);
    console.log('🔒 [Auth Guard] User logged in:', isLoggedIn);
    
    if (isLoggedIn) {
      return true;
    } else {
      console.log('🔒 [Auth Guard] User not logged in, redirecting to login');
      
      // Store the attempted URL for redirecting after login
      sessionStorage.setItem('redirectUrl', state.url);
      
      // Redirect to login page
      this.router.navigate(['/login']);
      return false;
    }
  }
} 