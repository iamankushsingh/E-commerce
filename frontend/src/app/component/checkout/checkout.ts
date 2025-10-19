import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CartItem, ShippingAddress, PaymentDetails, CreateOrderRequest } from '../../model/interfaces';
import { Order } from '../../model/order.model';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.html',
  styleUrls: ['./checkout.scss']
})
export class Checkout implements OnInit, OnDestroy {
  // Current step in checkout process
  currentStep = 1;
  maxSteps = 3;
  
  // Checkout form data
  checkoutData = {
    // Address
    firstName: '',
    lastName: '',
    address: '',
    apartment: '',
    city: '',
    country: '',
    zipcode: '',
    phone: '',
    saveContactInfo: false,
    
    // Shipping
    shippingMethod: 'standard',
    
    // Payment
    paymentMethod: 'card',
    cardholderName: '',
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvc: '',
    upiId: '',
    paypalEmail: '',
    saveCard: false
  };

  // Cart and order data
  cartItems: any[] = [];
  appliedDiscount = 0;
  couponCode = '';
  isProcessingOrder = false;
  orderProcessed = false;
  createdOrder: Order | null = null;

  // Form validation
  validationErrors: { [key: string]: string } = {};
  isFormValid = false;

  // Subscription management
  private subscription = new Subscription();

  constructor(
    private router: Router,
    private cartService: CartService,
    private orderService: OrderService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.loadCartItems();
    this.generateYearOptions();
    
    // Pre-populate form with user data if available
    this.populateUserData();
  }

  private populateUserData(): void {
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) {
      console.log('👤 Pre-populating form with user data:', currentUser);
      if (currentUser.firstName && !this.checkoutData.firstName) {
        this.checkoutData.firstName = currentUser.firstName;
      }
      if (currentUser.lastName && !this.checkoutData.lastName) {
        this.checkoutData.lastName = currentUser.lastName;
      }
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private loadCartItems(): void {
    this.cartItems = this.cartService.getCartItems();
    // Get discount info from cart service
    const discountInfo = this.cartService.getDiscountInfo();
    this.appliedDiscount = discountInfo.appliedDiscount || 0;
    this.couponCode = discountInfo.couponCode || '';
    console.log('💰 [Checkout] Loaded discount info:', { appliedDiscount: this.appliedDiscount, couponCode: this.couponCode });
  }

  // Step navigation
  nextStep(): void {
    console.log('nextStep called, currentStep:', this.currentStep, 'paymentMethod:', this.checkoutData.paymentMethod);
    
    if (this.validateCurrentStep()) {
      console.log('Validation passed');
      if (this.currentStep < this.maxSteps) {
        this.currentStep++;
      } else if (this.currentStep === this.maxSteps) {
        console.log('Processing order...');
        this.processOrder();
      }
    } else {
      console.log('Validation failed, errors:', this.validationErrors);
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.clearValidationErrors();
    }
  }

  goToStep(step: number): void {
    if (step <= this.currentStep || this.orderProcessed) {
      this.currentStep = step;
    }
  }

  // Shipping and payment selection
  selectShipping(method: string): void {
    this.checkoutData.shippingMethod = method;
  }

  selectPayment(method: string): void {
    this.checkoutData.paymentMethod = method;
    this.clearPaymentFields();
  }

  private clearPaymentFields(): void {
    this.checkoutData.cardholderName = '';
    this.checkoutData.cardNumber = '';
    this.checkoutData.expiryMonth = '';
    this.checkoutData.expiryYear = '';
    this.checkoutData.cvc = '';
    this.checkoutData.upiId = '';
    this.checkoutData.paypalEmail = '';
  }

  // Form validation
  private validateCurrentStep(): boolean {
    this.clearValidationErrors();
    
    switch (this.currentStep) {
      case 1:
        return this.validateShippingInfo();
      case 2:
        return this.validateShippingMethod();
      case 3:
        return this.validatePaymentInfo();
      default:
        return true;
    }
  }

  private validateShippingInfo(): boolean {
    let isValid = true;

    const firstName = this.checkoutData.firstName.trim();
    const lastName = this.checkoutData.lastName.trim();
    const address = this.checkoutData.address.trim();
    const city = this.checkoutData.city.trim();
    const country = this.checkoutData.country.trim();
    const zipcode = this.checkoutData.zipcode.trim();
    const phone = (this.checkoutData.phone || '').trim();

    if (!firstName) {
      this.validationErrors['firstName'] = '*First name is required';
      isValid = false;
    }

    if (!lastName) {
      this.validationErrors['lastName'] = '*Last name is required';
      isValid = false;
    }

    if (!address) {
      this.validationErrors['address'] = '*Address is required';
      isValid = false;
    }

    if (!city) {
      this.validationErrors['city'] = '*City is required';
      isValid = false;
    }

    if (!country) {
      this.validationErrors['country'] = '*Country is required';
      isValid = false;
    }

    if (!zipcode) {
      this.validationErrors['zipcode'] = '*Postal/ZIP code is required';
      isValid = false;
    } else if (!this.isValidPostalCode(country, zipcode)) {
      this.validationErrors['zipcode'] = '*Please enter a valid postal/ZIP code';
      isValid = false;
    }

    if (phone && /^\+?[\d\s\-\(\)]{10,}$/.test(phone) === false) {
      this.validationErrors['phone'] = '*Please enter a valid phone number';
      isValid = false;
    }

    return isValid;
  }

  private isValidPostalCode(country: string, code: string): boolean {
    switch (country) {
      case 'United States':
        return /^\d{5}(-\d{4})?$/.test(code);
      case 'Canada':
        return /^[A-Za-z]\d[A-Za-z][ -]?\d[A-Za-z]\d$/.test(code);
      case 'United Kingdom':
        return /^[A-Za-z0-9]{5,8}$/.test(code.replace(/\s/g, ''));
      case 'Australia':
        return /^\d{4}$/.test(code);
      case 'India':
        return /^\d{6}$/.test(code);
      default:
        return code.length >= 3;
    }
  }

  private validateShippingMethod(): boolean {
    return !!this.checkoutData.shippingMethod;
  }

  private validatePaymentInfo(): boolean {
    let isValid = true;

    if (this.checkoutData.paymentMethod === 'card') {
      if (!this.checkoutData.cardholderName.trim()) {
        this.validationErrors['cardholderName'] = 'Cardholder name is required';
        isValid = false;
      }

      if (!this.checkoutData.cardNumber.trim()) {
        this.validationErrors['cardNumber'] = 'Card number is required';
        isValid = false;
      } else if (!/^\d{4}\s\d{4}\s\d{4}\s\d{4}$/.test(this.checkoutData.cardNumber)) {
        this.validationErrors['cardNumber'] = 'Please enter a valid card number (1234 5678 9012 3456)';
        isValid = false;
      }

      if (!this.checkoutData.expiryMonth) {
        this.validationErrors['expiryMonth'] = 'Expiry month is required';
        isValid = false;
      }

      if (!this.checkoutData.expiryYear) {
        this.validationErrors['expiryYear'] = 'Expiry year is required';
        isValid = false;
      }

      if (!this.checkoutData.cvc.trim()) {
        this.validationErrors['cvc'] = 'CVC is required';
        isValid = false;
      } else if (!/^\d{3,4}$/.test(this.checkoutData.cvc)) {
        this.validationErrors['cvc'] = 'Please enter a valid CVC (3-4 digits)';
        isValid = false;
      }
    } else if (this.checkoutData.paymentMethod === 'upi') {
      if (!this.checkoutData.upiId.trim()) {
        this.validationErrors['upiId'] = 'UPI ID is required';
        isValid = false;
      } else if (!/^[\w\.-]+@[\w\.-]+$/.test(this.checkoutData.upiId)) {
        this.validationErrors['upiId'] = 'Please enter a valid UPI ID';
        isValid = false;
      }
    } else if (this.checkoutData.paymentMethod === 'paypal') {
      if (!this.checkoutData.paypalEmail.trim()) {
        this.validationErrors['paypalEmail'] = 'PayPal email is required';
        isValid = false;
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.checkoutData.paypalEmail)) {
        this.validationErrors['paypalEmail'] = 'Please enter a valid email address';
        isValid = false;
      }
    }

    return isValid;
  }

  private clearValidationErrors(): void {
    this.validationErrors = {};
  }

  // Order processing
  processOrder(): void {
    console.log('processOrder called');
    console.log('Cart items:', this.cartItems.length);
    console.log('Checkout data:', this.checkoutData);
    
    if (!this.validateCurrentStep() || this.isProcessingOrder) {
      console.log('Process order blocked - validation failed or already processing');
      return;
    }

    console.log('Starting order processing...');
    this.isProcessingOrder = true;

    // Get actual user information with debugging
    const currentUser = this.userService.getCurrentUser();
    const authState = JSON.parse(localStorage.getItem('authState') || '{}');
    console.log('🔍 Current user from service:', currentUser);
    console.log('🔍 Auth state from localStorage:', authState);
    
    // Try multiple ways to get the user email
    let userEmail = currentUser?.email;
    if (!userEmail && authState.user?.email) {
      userEmail = authState.user.email;
      console.log('📧 Using email from localStorage:', userEmail);
    }
    if (!userEmail) {
      userEmail = 'user@example.com';
      console.log('⚠️ Falling back to default email');
    }
    
    const userName = currentUser ? `${currentUser.firstName || ''} ${currentUser.lastName || ''}`.trim() : `${this.checkoutData.firstName} ${this.checkoutData.lastName}`;
    
    console.log('📧 Final email being used for order:', userEmail);
    console.log('👤 User name being used:', userName);

    // Prepare order data
    const fullAddress = `${this.checkoutData.firstName} ${this.checkoutData.lastName}\n${this.checkoutData.address}${this.checkoutData.apartment ? '\n' + this.checkoutData.apartment : ''}\n${this.checkoutData.city}, ${this.checkoutData.country} ${this.checkoutData.zipcode}`;
    
    const orderData: CreateOrderRequest = {
      shippingAddress: fullAddress,
      billingAddress: fullAddress,
      phoneNumber: this.checkoutData.phone,
      email: userEmail,
      notes: `Payment Method: ${this.checkoutData.paymentMethod}${this.couponCode ? ` | Coupon: ${this.couponCode}` : ''}`,
      paymentMethod: this.checkoutData.paymentMethod,
      taxAmount: this.calculateTax(),
      shippingAmount: this.calculateShipping(),
      discountAmount: this.appliedDiscount,
      couponCode: this.couponCode || undefined
    };

    // Create the order
    console.log('Calling orderService.createBackendOrder with data:', orderData);
    this.orderService.createBackendOrder(orderData).subscribe({
      next: (response: any) => {
        console.log('Order created successfully:', response);
        this.createdOrder = response.order;
        
        // Mark as processed
        this.orderProcessed = true;
        this.isProcessingOrder = false;

        // Clear the cart
        this.cartService.clearCart().subscribe({
          next: (success) => {
            console.log('Cart cleared:', success);
          },
          error: (error) => {
            console.error('Error clearing cart:', error);
          }
        });

        // Show success message and redirect after delay
        setTimeout(() => {
          // Pass the order data directly to avoid backend lookup issues
          this.router.navigate(['/order-confirmation'], { 
            state: { 
              order: response.order,
              fromCheckout: true 
            }
          });
        }, 3000);
      },
      error: (error) => {
        console.error('Order processing failed:', error);
        console.error('Error details:', error.error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        this.isProcessingOrder = false;
        this.validationErrors['payment'] = 'Payment processing failed. Please try again.';
      }
    });
  }

  private maskCardNumber(cardNumber: string): string {
    const cleaned = cardNumber.replace(/\s/g, '');
    return `**** **** **** ${cleaned.slice(-4)}`;
  }

  // Calculation methods
  calculateSubtotal(): number {
    return this.cartService.getSubtotal();
  }

  calculateTax(): number {
    return this.cartService.getTax();
  }

  calculateShipping(): number {
    const shippingRates = {
      'standard': 0, // Free for orders over $50
      'express': 15.99,
      'overnight': 29.99
    } as const;

    const subtotal = this.calculateSubtotal();
    if (this.checkoutData.shippingMethod === 'standard' && subtotal >= 50) {
      return 0;
    }

    return (shippingRates as any)[this.checkoutData.shippingMethod] || 0;
  }

  calculateTotal(): number {
    return this.calculateSubtotal() + this.calculateTax() + this.calculateShipping() - this.appliedDiscount;
  }

  removeCartItem(cartItemId: number): void {
    this.cartService.removeFromCart(cartItemId).subscribe({
      next: (success) => {
        if (success) {
          this.loadCartItems();
          console.log('Item removed from cart');
        }
      },
      error: (error) => {
        console.error('Error removing item from cart:', error);
      }
    });
  }

  // Utility methods
  onCardNumberInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.formatCardNumber(input.value);
  }

  onCvcInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.formatCVC(input.value);
  }

  formatCardNumber(value: string): void {
    // Remove all non-digits and format as groups of 4
    const cleaned = value.replace(/\D/g, '');
    const formatted = cleaned.replace(/(\d{4})(?=\d)/g, '$1 ');
    this.checkoutData.cardNumber = formatted.slice(0, 19); // Max length with spaces
  }

  formatCVC(value: string): void {
    // Only allow digits, max 4 characters
    this.checkoutData.cvc = value.replace(/\D/g, '').slice(0, 4);
  }

  private generateYearOptions(): void {
    const currentYear = new Date().getFullYear();
    this.yearOptions = Array.from({ length: 10 }, (_, i) => currentYear + i);
  }

  yearOptions: number[] = [];
  monthOptions = [
    { value: '01', label: '01 - January' },
    { value: '02', label: '02 - February' },
    { value: '03', label: '03 - March' },
    { value: '04', label: '04 - April' },
    { value: '05', label: '05 - May' },
    { value: '06', label: '06 - June' },
    { value: '07', label: '07 - July' },
    { value: '08', label: '08 - August' },
    { value: '09', label: '09 - September' },
    { value: '10', label: '10 - October' },
    { value: '11', label: '11 - November' },
    { value: '12', label: '12 - December' }
  ];

  hasValidationError(field: string): boolean {
    return !!this.validationErrors[field];
  }

  getValidationError(field: string): string {
    return this.validationErrors[field] || '';
  }
}
