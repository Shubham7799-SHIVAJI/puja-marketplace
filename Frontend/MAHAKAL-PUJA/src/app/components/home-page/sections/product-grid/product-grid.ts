import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HomeProductService, HomeProduct } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-product-grid',
  imports: [CommonModule],
  templateUrl: './product-grid.html',
  styleUrl: './product-grid.scss',
})
export class ProductGrid implements OnInit {
  private readonly service = inject(HomeProductService);
  private readonly router = inject(Router);
  readonly products = signal<HomeProduct[]>([]);

  ngOnInit(): void {
    this.service.getProducts().subscribe((p) => this.products.set(p));
  }

  getStars(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) => (i < Math.floor(rating) ? 'full' : i < rating ? 'half' : 'empty'));
  }

  toggleWishlist(event: Event, product: HomeProduct): void {
    event.stopPropagation();
    this.products.update((list) =>
      list.map((p) => (p.id === product.id ? { ...p, wishlisted: !p.wishlisted } : p)),
    );
  }

  addToCart(event: Event, product: HomeProduct): void {
    event.stopPropagation();
    // Cart integration will be wired to CustomerCartService
  }

  goToProduct(id: number): void {
    this.router.navigate(['/product', id]);
  }
}
