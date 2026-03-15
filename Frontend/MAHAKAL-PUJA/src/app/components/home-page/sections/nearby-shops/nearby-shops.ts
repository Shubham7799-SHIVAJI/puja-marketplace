import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HomeProductService, NearbyProduct } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-nearby-shops',
  imports: [CommonModule],
  templateUrl: './nearby-shops.html',
  styleUrl: './nearby-shops.scss',
})
export class NearbyShops implements OnInit {
  private readonly service = inject(HomeProductService);
  readonly products = signal<NearbyProduct[]>([]);

  ngOnInit(): void {
    this.service.getNearbyProducts().subscribe((p) => this.products.set(p));
  }

  getStars(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) => (i < Math.floor(rating) ? 'full' : 'empty'));
  }
}
