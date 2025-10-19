import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { User } from '../../model/user.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
  imports: [CommonModule, ReactiveFormsModule],
  standalone: true
})
export class ProfileComponent implements OnInit {
  profileForm: FormGroup;
  passwordForm: FormGroup;
  user: User | null = null;
  activeTab: 'profile' | 'password' = 'profile';
  isLoading = false;
  isPasswordLoading = false;
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmNewPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService
  ) {
    this.profileForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.pattern(/^[a-zA-Z\s]+$/)]],
      email: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      address: ['', [Validators.required, Validators.minLength(10)]]
    });

    this.passwordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmNewPassword: ['', [Validators.required]]
    }, { 
      validators: this.passwordMatchValidator 
    });
  }

  ngOnInit() {
    this.userService.authState$.subscribe(authState => {
      this.user = authState.user;
      if (this.user) {
        // Construct full name properly - only use lastName if it's not empty and not 'User'
        const lastName = this.user.lastName && this.user.lastName !== 'User' ? this.user.lastName : '';
        const fullName = lastName ? `${this.user.firstName} ${lastName}` : this.user.firstName;
        
        this.profileForm.patchValue({
          name: fullName,
          email: this.user.email,
          phoneNumber: this.user.phoneNumber || '',
          address: this.user.address || ''
        });
      }
    });
  }

  passwordMatchValidator(control: any): any {
    const newPassword = control.get('newPassword');
    const confirmNewPassword = control.get('confirmNewPassword');
    
    if (newPassword && confirmNewPassword && newPassword.value !== confirmNewPassword.value) {
      confirmNewPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }

  setActiveTab(tab: 'profile' | 'password') {
    this.activeTab = tab;
  }

  getInitials(): string {
    if (!this.user?.firstName) return 'U';
    // Only use lastName initial if it exists and is not 'User'
    const lastNameInitial = (this.user.lastName && this.user.lastName !== 'User') ? this.user.lastName[0] : '';
    return `${this.user.firstName[0]}${lastNameInitial}`.toUpperCase();
  }

  toggleCurrentPasswordVisibility() {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPasswordVisibility() {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmNewPasswordVisibility() {
    this.showConfirmNewPassword = !this.showConfirmNewPassword;
  }

  updateProfile() {
    if (this.profileForm.valid) {
      this.isLoading = true;
      
      // Parse the full name into firstName and lastName
      const fullName = this.profileForm.value.name.trim();
      const nameParts = fullName.split(' ');
      const firstName = nameParts[0];
      const lastName = nameParts.length > 1 ? nameParts.slice(1).join(' ') : '';
      
      const updateData = {
        firstName: firstName,
        lastName: lastName || null, // Send null instead of empty string for consistency
        email: this.profileForm.value.email,
        phoneNumber: this.profileForm.value.phoneNumber || null,
        address: this.profileForm.value.address || null
      };
      
      console.log('Sending update data:', updateData); // Debug log
      
      this.userService.updateProfile(updateData).subscribe({
        next: (success) => {
          this.isLoading = false;
          if (success) {
            alert('Profile updated successfully!');
          } else {
            alert('Failed to update profile. Please try again.');
          }
        },
        error: () => {
          this.isLoading = false;
          alert('An error occurred. Please try again.');
        }
      });
    }
  }

  changePassword() {
    if (this.passwordForm.valid) {
      this.isPasswordLoading = true;
      
      const { currentPassword, newPassword } = this.passwordForm.value;
      
      this.userService.changePassword(currentPassword, newPassword).subscribe({
        next: (response) => {
          this.isPasswordLoading = false;
          if (response.success) {
            alert(response.message);
            this.resetPasswordForm();
          } else {
            alert(response.message);
          }
        },
        error: () => {
          this.isPasswordLoading = false;
          alert('An error occurred. Please try again.');
        }
      });
    }
  }

  resetForm() {
    if (this.user) {
      // Construct full name properly - only use lastName if it's not empty and not 'User'
      const lastName = this.user.lastName && this.user.lastName !== 'User' ? this.user.lastName : '';
      const fullName = lastName ? `${this.user.firstName} ${lastName}` : this.user.firstName;
      
      this.profileForm.patchValue({
        name: fullName,
        email: this.user.email,
        phoneNumber: this.user.phoneNumber || '',
        address: this.user.address || ''
      });
    }
  }

  resetPasswordForm() {
    this.passwordForm.reset();
  }
} 