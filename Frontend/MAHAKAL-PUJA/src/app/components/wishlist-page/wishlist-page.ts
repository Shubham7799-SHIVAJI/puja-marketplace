import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MARKETPLACE_TEXT } from '../../shared/constants/marketplace.constants';
import { WishlistItem } from '../../shared/models/marketplace';
import { CustomerCartService } from '../../shared/services/customer-cart';
import { MarketplaceService } from '../../shared/services/marketplace';

@Component({
  standalone: true,
  selector: 'app-wishlist-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './wishlist-page.html',
  styleUrl: './wishlist-page.scss',
})
export class WishlistPage implements OnInit {
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly cartService = inject(CustomerCartService);

  readonly text = MARKETPLACE_TEXT.wishlist;
  readonly commonText = MARKETPLACE_TEXT.common;
  readonly items = signal<WishlistItem[]>([]);
  readonly loading = signal(true);

  async ngOnInit(): Promise<void> {
    await this.load();
  }

  async remove(productId: number): Promise<void> {
    await firstValueFrom(this.marketplaceService.removeFromWishlist(productId));
    await this.load();
  }

  addToCart(item: WishlistItem): void {
    this.cartService.add({
      productId: item.product.id,
      quantity: 1,
      name: item.product.name,
      sellerName: item.product.sellerName,
      imageUrl: item.product.imageUrl,
      price: item.product.price,
      finalPrice: item.product.finalPrice,
    });
  }

  private async load(): Promise<void> {
    try {
      this.items.set(await firstValueFrom(this.marketplaceService.getWishlist()));
    } finally {
      this.loading.set(false);
    }
  }
}