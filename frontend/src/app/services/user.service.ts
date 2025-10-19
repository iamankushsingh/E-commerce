import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, catchError, of, tap, map } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { User, UserFilters } from '../model/user.model';

export interface RegisteredUser extends User {
  password: string; // Store hashed password in real app
}

export interface AuthState {
  isLoggedIn: boolean;
  user: User | null;
  token: string | null;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  user?: User;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly USER_API_URL = 'http://localhost:8085/api/users';
  private readonly ADMIN_USER_API_URL = 'http://localhost:8085/api/admin/users';

  private authState = new BehaviorSubject<AuthState>({
    isLoggedIn: false,
    user: null,
    token: null
  });

  public authState$ = this.authState.asObservable();
  
  constructor(
    private router: Router, 
    private http: HttpClient
  ) {
    // Check if user is already logged in (from localStorage)
    this.loadAuthState();
  }

  // Login user with password verification
  login(email: string, password: string): Observable<ApiResponse> {
    const loginData = { email, password };
    
    return this.http.post<any>(`${this.USER_API_URL}/login`, loginData).pipe(
      tap(response => {
        if (response.success && response.user && response.token) {
          // Use the JWT token provided by the backend
          const authData: AuthState = {
            isLoggedIn: true,
            user: response.user,
            token: response.token  // Token from backend
          };

          this.authState.next(authData);
          this.saveAuthState(authData);
          
          // Force update to ensure all subscribers are notified
          setTimeout(() => {
            this.authState.next(authData);
          }, 100);
        }
      }),
      map(response => ({
        success: response.success,
        message: response.message,
        user: response.user
      })),
      catchError(error => {
        console.error('Login error:', error);
        return of({ success: false, message: 'Login failed. Please try again.' });
      })
    );
  }

  // Register user with proper storage
  register(userData: any): Observable<ApiResponse> {
    const registrationData = {
      username: userData.username || `user_${Date.now()}`,
      email: userData.email,
      password: userData.password,
      firstName: userData.firstName,
      lastName: userData.lastName,
      avatar: userData.avatar,
      phoneNumber: userData.phoneNumber,
      address: userData.address
    };
    
    return this.http.post<ApiResponse>(`${this.USER_API_URL}/register`, registrationData).pipe(
      tap(response => {
        console.log('Registration response:', response);
      }),
      catchError(error => {
        console.error('Registration error:', error);
        return of({ 
          success: false, 
          message: error.error?.message || 'Registration failed. Please try again.' 
        });
      })
    );
  }

  // Update user profile
  updateProfile(profileData: Partial<User>): Observable<boolean> {
    const currentUser = this.getCurrentUser();
    if (!currentUser) {
      return of(false);
    }

    return this.http.put<ApiResponse>(`${this.USER_API_URL}/${currentUser.id}`, profileData).pipe(
      tap(response => {
        console.log('Profile update response:', response);
        if (response.success && response.user) {
          // Update the current auth state with the new user data
          const currentState = this.authState.value;
          const newAuthState = { ...currentState, user: response.user };
          this.authState.next(newAuthState);
          this.saveAuthState(newAuthState);
        }
      }),
      map(response => response.success),
      catchError(error => {
        console.error('Error updating profile:', error);
        return of(false);
      })
    );
  }

  // Change password with verification
  changePassword(currentPassword: string, newPassword: string): Observable<ApiResponse> {
    const currentUser = this.getCurrentUser();
    if (!currentUser) {
      return of({ success: false, message: 'User not authenticated' });
    }

    const changePasswordData = {
      currentPassword,
      newPassword
    };

    return this.http.post<ApiResponse>(`${this.USER_API_URL}/${currentUser.id}/change-password`, changePasswordData).pipe(
      tap(response => {
        console.log('Password change response:', response);
      }),
      catchError(error => {
        console.error('Password change error:', error);
        return of({ 
          success: false, 
          message: error.error?.message || 'Password change failed. Please try again.' 
        });
      })
    );
  }

  // Logout user
  logout(): void {
    console.log('🔐 [User Service] Logging out user');
    
    // Clear auth state
    const emptyState: AuthState = {
      isLoggedIn: false,
      user: null,
      token: null
    };
    
    this.authState.next(emptyState);
    localStorage.removeItem('authState');
    sessionStorage.clear(); // Clear all session storage (including cart discount info, redirect URL, etc.)
    
    // Force update to ensure all subscribers are notified
    setTimeout(() => {
      this.authState.next(emptyState);
    }, 100);
    
    console.log('🔐 [User Service] User logged out successfully');
    
    // Navigate to home page
    this.router.navigate(['/home']);
  }

  // Get current user
  getCurrentUser(): User | null {
    return this.authState.value.user;
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    return this.authState.value.isLoggedIn;
  }

  // Get auth token
  getToken(): string | null {
    return this.authState.value.token;
  }

  // Admin methods for user management
  getUsers(filters?: UserFilters): Observable<User[]> {
    let params = new HttpParams()
      .set('page', '0')
      .set('size', '1000'); // Get all users for now

    // Only add filter parameters if they are not empty strings
    if (filters?.role && filters.role.trim() !== '') {
      params = params.set('role', filters.role.toUpperCase());
    }
    if (filters?.status && filters.status.trim() !== '') {
      params = params.set('status', filters.status.toUpperCase());
    }
    if (filters?.search && filters.search.trim() !== '') {
      params = params.set('search', filters.search.trim());
    }

    return this.http.get<PaginatedResponse<User>>(`${this.ADMIN_USER_API_URL}`, { params }).pipe(
      tap(response => {
        console.log('Admin users fetched:', response);
      }),
      // Extract just the content array from the paginated response
      map(response => response.content),
      catchError(error => {
        console.error('Error fetching users:', error);
        return of([]); // Return empty array on error
      })
    );
  }

  getUser(id: string): Observable<User | undefined> {
    return this.http.get<User>(`${this.ADMIN_USER_API_URL}/${id}`).pipe(
      catchError(error => {
        console.error('Error fetching user:', error);
        return of(undefined);
      })
    );
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.http.put<ApiResponse>(`${this.ADMIN_USER_API_URL}/${id}`, user).pipe(
      tap(response => {
        console.log('User updated:', response);
      }),
      map(response => {
        if (response.success && response.user) {
          return response.user;
        } else {
          throw new Error(response.message);
        }
      }),
      catchError(error => {
        console.error('Error updating user:', error);
        throw error;
      })
    );
  }

  blockUser(id: string): Observable<User> {
    return this.http.post<ApiResponse>(`${this.ADMIN_USER_API_URL}/${id}/block`, {}).pipe(
      tap(response => {
        console.log('User blocked:', response);
      }),
      map(response => {
        if (response.success && response.user) {
          return response.user;
        } else {
          throw new Error(response.message);
        }
      }),
      catchError(error => {
        console.error('Error blocking user:', error);
        throw error;
      })
    );
  }

  unblockUser(id: string): Observable<User> {
    return this.http.post<ApiResponse>(`${this.ADMIN_USER_API_URL}/${id}/unblock`, {}).pipe(
      tap(response => {
        console.log('User unblocked:', response);
      }),
      map(response => {
        if (response.success && response.user) {
          return response.user;
        } else {
          throw new Error(response.message);
        }
      }),
      catchError(error => {
        console.error('Error unblocking user:', error);
        throw error;
      })
    );
  }

  deleteUser(id: string): Observable<boolean> {
    return this.http.delete<ApiResponse>(`${this.ADMIN_USER_API_URL}/${id}`).pipe(
      tap(response => {
        console.log('User deleted:', response);
      }),
      map(response => response.success),
      catchError(error => {
        console.error('Error deleting user:', error);
        throw error;
      })
    );
  }

  // Clear all users from localStorage (for testing)
  clearAllUsers(): void {
    localStorage.removeItem('authState');
    console.log('Auth state cleared from localStorage');
  }

  // Save auth state to localStorage
  private saveAuthState(authData: AuthState): void {
    console.log('Saving auth state to localStorage:', authData);
    localStorage.setItem('authState', JSON.stringify(authData));
  }

  // Load auth state from localStorage
  private loadAuthState(): void {
    const savedState = localStorage.getItem('authState');
    console.log('Loading auth state from localStorage:', savedState);
    if (savedState) {
      try {
        const authData: AuthState = JSON.parse(savedState);
        console.log('Parsed auth data:', authData);
        
        // If token exists, set the auth state (backend will validate token on requests)
        if (authData.token && authData.user) {
          this.authState.next(authData);
          console.log('Auth state restored from localStorage');
        } else {
          console.log('No valid auth data found, clearing auth state');
          localStorage.removeItem('authState');
        }
      } catch (error) {
        console.error('Error loading auth state:', error);
        localStorage.removeItem('authState');
      }
    }
  }
}
