import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HomeProductService, FestivalCollection } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-festival-collection',
  imports: [CommonModule],
  templateUrl: './festival-collection.html',
  styleUrl: './festival-collection.scss',
})
export class FestivalCollectionComponent implements OnInit {
  private readonly service = inject(HomeProductService);
  readonly collections = signal<FestivalCollection[]>([]);
  readonly activeTab = signal(0);

  ngOnInit(): void {
    this.service.getFestivalCollections().subscribe((c) => this.collections.set(c));
  }

  setTab(index: number): void {
    this.activeTab.set(index);
  }
}
