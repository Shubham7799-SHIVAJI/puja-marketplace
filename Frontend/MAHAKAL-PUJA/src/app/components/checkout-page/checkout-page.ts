import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MARKETPLACE_PAYMENT_METHODS, MARKETPLACE_TEXT } from '../../shared/constants/marketplace.constants';
import { CustomerAddress, CustomerAddressPayload } from '../../shared/models/marketplace';
import { AuthSessionService } from '../../shared/services/auth-session';
import { CustomerCartService } from '../../shared/services/customer-cart';
import { MarketplaceService } from '../../shared/services/marketplace';

@Component({
  standalone: true,
  selector: 'app-checkout-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout-page.html',
  styleUrl: './checkout-page.scss',
})
export class CheckoutPage implements OnInit {
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly authSessionService = inject(AuthSessionService);
  private readonly router = inject(Router);
  readonly cartService = inject(CustomerCartService);

  readonly text = MARKETPLACE_TEXT.checkout;
  readonly commonText = MARKETPLACE_TEXT.common;
  readonly paymentMethods = MARKETPLACE_PAYMENT_METHODS;
  readonly addresses = signal<CustomerAddress[]>([]);
  readonly loading = signal(true);
  readonly placingOrder = signal(false);
  readonly successMessage = signal('');
  readonly estimatedTotal = computed(() => this.cartService.totalAmount());

  selectedAddressId: number | null = null;
  paymentMethod = 'COD';
  couponCode = '';
  showAddressForm = false;
  addressForm: CustomerAddressPayload = this.createEmptyAddress();

  async ngOnInit(): Promise<void> {
    if (!this.authSessionService.getSession()) {
      await this.router.navigate(['/signin']);
      return;
    }
    await this.loadAddresses();
  }

  async saveAddress(): Promise<void> {
    const saved = await firstValueFrom(this.marketplaceService.createAddress(this.addressForm));
    this.addresses.set([saved, ...this.addresses().filter((address) => address.id !== saved.id)]);
    this.selectedAddressId = saved.id;
    this.addressForm = this.createEmptyAddress();
    this.showAddressForm = false;
  }

  async deleteAddress(addressId: number): Promise<void> {
    await firstValueFrom(this.marketplaceService.deleteAddress(addressId));
    this.addresses.set(this.addresses().filter((address) => address.id !== addressId));
    if (this.selectedAddressId === addressId) {
      this.selectedAddressId = this.addresses()[0]?.id ?? null;
    }
  }

  async placeOrder(): Promise<void> {
    if (!this.selectedAddressId || this.cartService.items().length === 0) {
      return;
    }

    this.placingOrder.set(true);
    try {
      const response = await firstValueFrom(
        this.marketplaceService.checkout({
          addressId: this.selectedAddressId,
          paymentMethod: this.paymentMethod,
          couponCode: this.couponCode || undefined,
          items: this.cartService.items().map((item) => ({
            productId: item.productId,
            variantId: item.variantId,
            quantity: item.quantity,
          })),
        }),
      );
      this.cartService.clear();
      this.successMessage.set(`${this.text.successTitle}: ${response.orders.map((order) => order.orderCode).join(', ')}`);
    } finally {
      this.placingOrder.set(false);
    }
  }

  private async loadAddresses(): Promise<void> {
    try {
      const addresses = await firstValueFrom(this.marketplaceService.getAddresses());
      this.addresses.set(addresses);
      this.selectedAddressId = addresses.find((address) => address.defaultAddress)?.id ?? addresses[0]?.id ?? null;
    } finally {
      this.loading.set(false);
    }
  }

  private createEmptyAddress(): CustomerAddressPayload {
    return {
      fullName: '',
      phoneNumber: '',
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      pincode: '',
      country: 'India',
      landmark: '',
      deliveryInstructions: '',
      defaultAddress: false,
    };
  }
}