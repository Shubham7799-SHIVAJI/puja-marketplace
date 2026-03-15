import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HomeProductService, CustomerReview } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-customer-reviews',
  imports: [CommonModule],
  templateUrl: './customer-reviews.html',
  styleUrl: './customer-reviews.scss',
})
export class CustomerReviews implements OnInit {
  private readonly service = inject(HomeProductService);
  readonly reviews = signal<CustomerReview[]>([]);

  ngOnInit(): void {
    this.service.getReviews().subscribe((r) => this.reviews.set(r));
  }

  getStars(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) => (i < rating ? 'full' : 'empty'));
  }
}
