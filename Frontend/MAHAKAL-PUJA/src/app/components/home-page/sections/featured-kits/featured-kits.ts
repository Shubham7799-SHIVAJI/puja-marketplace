import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HomeProductService, FeaturedKit } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-featured-kits',
  imports: [CommonModule],
  templateUrl: './featured-kits.html',
  styleUrl: './featured-kits.scss',
})
export class FeaturedKits implements OnInit {
  private readonly service = inject(HomeProductService);
  readonly kits = signal<FeaturedKit[]>([]);

  ngOnInit(): void {
    this.service.getFeaturedKits().subscribe((kits) => this.kits.set(kits));
  }

  getStars(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) => (i < Math.floor(rating) ? 'full' : i < rating ? 'half' : 'empty'));
  }
}
