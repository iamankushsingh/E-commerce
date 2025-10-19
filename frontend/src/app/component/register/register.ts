import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
  standalone: true
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  showConfirmPassword = false;
  termsAccepted = false;
  selectedAvatar = '';
  
  avatarOptions = [
    { name: 'Avatar 1', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix&backgroundColor=b6e3f4&mood=happy' },
    { name: 'Avatar 2', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Emma&backgroundColor=c0aede&mood=happy' },
    { name: 'Avatar 3', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack&backgroundColor=ffd5dc&mood=happy' },
    { name: 'Avatar 4', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily&backgroundColor=ffdfbf&mood=happy' },
    { name: 'Avatar 5', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Noah&backgroundColor=d1d4f9&mood=happy' },
    { name: 'Avatar 6', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Zoe&backgroundColor=c5e1a5&mood=happy' },
    { name: 'Avatar 7', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lucas&backgroundColor=ffc1cc&mood=happy' },
    { name: 'Avatar 8', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ava&backgroundColor=ffe4b5&mood=happy' },
    { name: 'Avatar 9', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Leo&backgroundColor=e6f3ff&mood=happy' },
    { name: 'Avatar 10', url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ruby&backgroundColor=f0e6ff&mood=happy' }
  ];

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private userService: UserService
  ) {
    this.registerForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.pattern(/^[a-zA-Z\s]+$/)]],
      email: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)]],
      avatar: [''],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      address: ['', [Validators.required, Validators.minLength(10)]]
    }, { 
      validators: this.passwordMatchValidator 
    });
  }

  ngOnInit() {
    // Clear form and reset state when component initializes
    this.clearForm();
    // Set a default avatar
    this.selectAvatar(this.avatarOptions[0].url);
  }

  clearForm() {
    this.registerForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = false;
    this.showPassword = false;
    this.showConfirmPassword = false;
    this.termsAccepted = false;
    // Set default avatar instead of empty
    this.selectedAvatar = this.avatarOptions[0].url;
    this.registerForm.patchValue({ avatar: this.avatarOptions[0].url });
  }

  selectAvatar(url: string) {
    this.selectedAvatar = url;
    this.registerForm.patchValue({ avatar: url });
  }

  passwordMatchValidator(control: AbstractControl): {[key: string]: any} | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onTermsChange(event: any) {
    this.termsAccepted = event.target.checked;
  }

  onSubmit() {
    if (this.registerForm.valid && this.termsAccepted) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      
      const formData = this.registerForm.value;
      const nameParts = formData.name.trim().split(' ');
      const firstName = nameParts[0] || formData.name;
      const lastName = nameParts.length > 1 ? nameParts.slice(1).join(' ') : '';
      
      const userData = {
        username: formData.email.split('@')[0], // Generate username from email
        email: formData.email,
        password: formData.password,
        firstName: firstName,
        lastName: lastName || undefined, // Don't send lastName if empty
        avatar: formData.avatar,
        phoneNumber: formData.phoneNumber,
        address: formData.address
      };
      
      console.log('Registering user with data:', userData);
      
      this.userService.register(userData).subscribe({
        next: (response) => {
          console.log('Registration response:', response);
          this.isLoading = false;
          if (response.success) {
            this.successMessage = response.message;
            
            // Redirect to login page after successful registration
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
          } else {
            this.errorMessage = response.message;
          }
        },
        error: (error) => {
          console.log('Registration error:', error);
          this.isLoading = false;
          this.errorMessage = 'Registration failed. Please try again.';
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      
      // Show error if terms are not accepted
      if (!this.termsAccepted) {
        this.errorMessage = 'Please accept the terms and conditions to continue.';
      }
    }
  }
} 