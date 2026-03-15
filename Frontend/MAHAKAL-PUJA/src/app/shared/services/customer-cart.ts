import { Injectable, computed, signal } from '@angular/core';

export interface CustomerCartItem {
  productId: number;
  variantId?: number | null;
  quantity: number;
  name: string;
  sellerName: string;
  imageUrl: string | null;
  price: number;
  finalPrice: number;
  variantLabel?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class CustomerCartService {
  private readonly storageKey = 'mahakal-marketplace-cart';
  readonly items = signal<CustomerCartItem[]>(this.read());
  readonly itemCount = computed(() => this.items().reduce((sum, item) => sum + item.quantity, 0));
  readonly totalAmount = computed(() => this.items().reduce((sum, item) => sum + item.finalPrice * item.quantity, 0));

  add(item: CustomerCartItem): void {
    const existing = this.items().find((entry) => entry.productId === item.productId && entry.variantId === item.variantId);
    if (existing) {
      this.updateQuantity(item.productId, item.variantId ?? null, existing.quantity + item.quantity);
      return;
    }

    this.commit([...this.items(), item]);
  }

  updateQuantity(productId: number, variantId: number | null, quantity: number): void {
    const next = this.items().map((item) => {
      if (item.productId === productId && (item.variantId ?? null) === variantId) {
        return { ...item, quantity: Math.max(1, quantity) };
      }
      return item;
    });
    this.commit(next);
  }

  remove(productId: number, variantId: number | null): void {
    this.commit(this.items().filter((item) => !(item.productId === productId && (item.variantId ?? null) === variantId)));
  }

  clear(): void {
    this.commit([]);
  }

  private commit(items: CustomerCartItem[]): void {
    this.items.set(items);
    localStorage.setItem(this.storageKey, JSON.stringify(items));
  }

  private read(): CustomerCartItem[] {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as CustomerCartItem[];
    } catch {
      localStorage.removeItem(this.storageKey);
      return [];
    }
  }
}