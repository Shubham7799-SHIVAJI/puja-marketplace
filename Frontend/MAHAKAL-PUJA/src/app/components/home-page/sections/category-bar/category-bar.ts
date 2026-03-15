import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HomeProductService, PujaCategory } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-category-bar',
  imports: [CommonModule],
  templateUrl: './category-bar.html',
  styleUrl: './category-bar.scss',
})
export class CategoryBar implements OnInit {
  private readonly service = inject(HomeProductService);

  readonly categories = signal<PujaCategory[]>([]);
  readonly activeSlug = signal('');

  ngOnInit(): void {
    this.service.getCategories().subscribe((cats) => this.categories.set(cats));
  }

  select(slug: string): void {
    this.activeSlug.set(this.activeSlug() === slug ? '' : slug);
  }
}
