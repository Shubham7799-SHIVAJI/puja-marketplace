import { Injectable, computed, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ProductCompareService {
  private readonly storageKey = 'mahakal-compare-products';
  readonly productIds = signal<number[]>(this.read());
  readonly count = computed(() => this.productIds().length);

  toggle(productId: number): void {
    if (this.productIds().includes(productId)) {
      this.commit(this.productIds().filter((id) => id !== productId));
      return;
    }

    this.commit([...this.productIds().slice(-2), productId]);
  }

  contains(productId: number): boolean {
    return this.productIds().includes(productId);
  }

  clear(): void {
    this.commit([]);
  }

  private commit(ids: number[]): void {
    this.productIds.set(ids);
    localStorage.setItem(this.storageKey, JSON.stringify(ids));
  }

  private read(): number[] {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as number[];
    } catch {
      localStorage.removeItem(this.storageKey);
      return [];
    }
  }
}