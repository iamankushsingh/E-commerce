import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private userService: UserService,
    private cartService: CartService
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    // Clear form and reset state when component initializes
    this.clearForm();
  }

  clearForm() {
    this.loginForm.reset();
    this.errorMessage = '';
    this.isLoading = false;
    this.showPassword = false;
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
      const { email, password } = this.loginForm.value;
      
      this.userService.login(email, password).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success && response.user) {
            
            console.log('🔒 [Login] Login successful, refreshing cart for new user');
            
            // Refresh cart with new user context
            this.cartService.refreshCartAfterAuth();
            
            // Check if there's a redirect URL stored (from auth guard)
            const redirectUrl = sessionStorage.getItem('redirectUrl');
            
            if (redirectUrl) {
              // Clear the stored redirect URL
              sessionStorage.removeItem('redirectUrl');
              console.log('🔒 [Login] Redirecting to original destination:', redirectUrl);
              this.router.navigate([redirectUrl]);
            } else {
              // Redirect based on user role - check both uppercase and lowercase
              const userRole = response.user.role;
              if (userRole === 'ADMIN' || userRole === 'admin') {
                console.log('Admin user detected, redirecting to dashboard');
                this.router.navigate(['/admin/dashboard']);
              } else {
                console.log('Regular user detected, redirecting to home');
                this.router.navigate(['/home']);
              }
            }
          } else {
            this.errorMessage = response.message || 'Login failed. Please try again.';
          }
        },
        error: () => {
          this.isLoading = false;
          this.errorMessage = 'Login failed. Please try again.';
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
    }
  }
} 