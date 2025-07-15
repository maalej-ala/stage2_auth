import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, SignupRequest } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

   private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  signupForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor() {
    this.signupForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      agreeToTerms: [false, [Validators.requiredTrue]]
    }, { validators: this.passwordMatchValidator });
  }

  // Custom validator to check if passwords match
  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password');
    const confirmPassword = formGroup.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      if (confirmPassword?.errors) {
        delete confirmPassword.errors['passwordMismatch'];
        if (Object.keys(confirmPassword.errors).length === 0) {
          confirmPassword.setErrors(null);
        }
      }
      return null;
    }
  }

  onSubmit() {
    if (this.signupForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      
      const { firstName, lastName, email, password } = this.signupForm.value;
      
      const signupData: SignupRequest = {
        firstName,
        lastName,
        email,
        password
      };

      console.log('Signup with:', signupData);
      
      // Call authentication service
      this.authService.signup(signupData).subscribe({
        next: (response) => {
          console.log('Signup successful!', response);
          this.successMessage = 'Account created successfully! Redirecting to dashboard...';
          
          // Redirect to dashboard after successful signup and auto-login
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 2000);
          
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Signup failed:', error);
          this.errorMessage = error.message || 'Signup failed. Please try again.';
          this.isLoading = false;
        }
      });

      // Alternative: Signup without auto-login (redirect to login page)
      // this.authService.signup(signupData).subscribe({
      //   next: (response) => {
      //     console.log('Signup successful!', response);
      //     this.successMessage = 'Account created successfully! Please login to continue.';
      //     
      //     setTimeout(() => {
      //       this.router.navigate(['/login'], { 
      //         queryParams: { email: email } // Pre-fill login form
      //       });
      //     }, 2000);
      //     
      //     this.isLoading = false;
      //   },
      //   error: (error) => {
      //     console.error('Signup failed:', error);
      //     this.errorMessage = error.message || 'Signup failed. Please try again.';
      //     this.isLoading = false;
      //   }
      // });
    } else {
      // Mark all fields as touched to show validation errors
      this.markFormGroupTouched();
    }
  }

  onLoginRedirect(event: Event) {
    event.preventDefault();
    console.log('Redirect to login clicked');
    this.router.navigate(['/login']);
  }

  // Helper method to mark all form fields as touched
  private markFormGroupTouched() {
    Object.keys(this.signupForm.controls).forEach(key => {
      const control = this.signupForm.get(key);
      control?.markAsTouched();
    });
  }

  // Helper method to get form control errors for display
  getFieldError(fieldName: string): string {
    const field = this.signupForm.get(fieldName);
    if (field?.errors && field?.touched) {
      if (field.errors['required']) {
        return `${this.getFieldDisplayName(fieldName)} is required`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['minlength']) {
        return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['passwordMismatch']) {
        return 'Passwords do not match';
      }
      if (field.errors['requiredTrue']) {
        return 'You must agree to the terms and conditions';
      }
    }
    return '';
  }

  // Helper method to get display name for fields
  private getFieldDisplayName(fieldName: string): string {
    const displayNames: Record<string, string> = {
      firstName: 'First name',
      lastName: 'Last name',
      email: 'Email',
      password: 'Password',
      confirmPassword: 'Confirm password',
      agreeToTerms: 'Terms agreement'
    };
    return displayNames[fieldName] || fieldName;
  }

  // Helper method to check if field has error
  hasFieldError(fieldName: string): boolean {
    const field = this.signupForm.get(fieldName);
    return !!(field?.errors && field?.touched);
  }

  // Helper method to check if field is valid
  isFieldValid(fieldName: string): boolean {
    const field = this.signupForm.get(fieldName);
    return !!(field?.valid && field?.touched);
  }
}