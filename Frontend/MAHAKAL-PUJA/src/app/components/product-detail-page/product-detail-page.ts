import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MARKETPLACE_TEXT } from '../../shared/constants/marketplace.constants';
import { MarketplaceProductDetail, ProductVariantItem } from '../../shared/models/marketplace';
import { AuthSessionService } from '../../shared/services/auth-session';
import { CustomerCartService } from '../../shared/services/customer-cart';
import { MarketplaceService } from '../../shared/services/marketplace';
import { ProductCompareService } from '../../shared/services/product-compare';
import { HomeProductService, HomeProduct } from '../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-product-detail-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-detail-page.html',
  styleUrl: './product-detail-page.scss',
})
export class ProductDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly marketplaceService = inject(MarketplaceService);
  private readonly cartService = inject(CustomerCartService);
  private readonly compareService = inject(ProductCompareService);
  private readonly authSessionService = inject(AuthSessionService);
  private readonly homeProductService = inject(HomeProductService);

  readonly text = MARKETPLACE_TEXT.product;
  readonly commonText = MARKETPLACE_TEXT.common;
  readonly detail = signal<MarketplaceProductDetail | null>(null);
  readonly homeProduct = signal<HomeProduct | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly activeImageIndex = signal(0);

  selectedVariantId: number | null = null;
  quantity = 1;

  async ngOnInit(): Promise<void> {
    const productId = Number(this.route.snapshot.paramMap.get('productId'));
    if (!productId) {
      this.error.set('Product not found.');
      this.loading.set(false);
      return;
    }

    try {
      this.detail.set(await firstValueFrom(this.marketplaceService.getProductDetail(productId)));
    } catch {
      // Fallback to home JSON data
      try {
        const all = await firstValueFrom(this.homeProductService.getProducts());
        const found = all.find((p) => p.id === productId) ?? null;
        if (found) {
          this.homeProduct.set(found);
        } else {
          this.error.set('Product not found.');
        }
      } catch {
        this.error.set(MARKETPLACE_TEXT.common.loadFailed);
      }
    } finally {
      this.loading.set(false);
    }
  }

  get selectedVariant(): ProductVariantItem | null {
    return this.detail()?.variants.find((v) => v.id === this.selectedVariantId) ?? null;
  }

  getStars(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) =>
      i < Math.floor(rating) ? 'full' : i < rating ? 'half' : 'empty',
    );
  }

  async toggleWishlist(): Promise<void> {
    const detail = this.detail();
    if (!detail) return;
    if (!this.authSessionService.getSession()) {
      await this.router.navigate(['/signin']);
      return;
    }
    if (detail.product.wishlisted) {
      await firstValueFrom(this.marketplaceService.removeFromWishlist(detail.product.id));
      this.detail.set({ ...detail, product: { ...detail.product, wishlisted: false } });
    } else {
      await firstValueFrom(this.marketplaceService.addToWishlist(detail.product.id));
      this.detail.set({ ...detail, product: { ...detail.product, wishlisted: true } });
    }
  }

  addToCart(): void {
    const d = this.detail();
    if (d) {
      this.cartService.add({
        productId: d.product.id,
        variantId: this.selectedVariantId,
        quantity: this.quantity,
        name: d.product.name,
        sellerName: d.product.sellerName,
        imageUrl: d.product.imageUrl,
        price: d.product.price,
        finalPrice: d.product.finalPrice + (this.selectedVariant?.additionalPrice ?? 0),
        variantLabel: this.selectedVariant
          ? `${this.selectedVariant.variantName}: ${this.selectedVariant.variantValue}`
          : null,
      });
      return;
    }
    const hp = this.homeProduct();
    if (hp) {
      this.cartService.add({
        productId: hp.id,
        variantId: null,
        quantity: this.quantity,
        name: hp.name,
        sellerName: hp.sellerName,
        imageUrl: null,
        price: hp.originalPrice,
        finalPrice: hp.price,
        variantLabel: null,
      });
    }
  }

  toggleCompare(): void {
    const detail = this.detail();
    if (detail) this.compareService.toggle(detail.product.id);
  }

  isCompared(): boolean {
    const detail = this.detail();
    return detail ? this.compareService.contains(detail.product.id) : false;
  }
}
