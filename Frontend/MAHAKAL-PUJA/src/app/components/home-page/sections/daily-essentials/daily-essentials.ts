import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HomeProductService, DailyEssential } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-daily-essentials',
  imports: [CommonModule],
  templateUrl: './daily-essentials.html',
  styleUrl: './daily-essentials.scss',
})
export class DailyEssentials implements OnInit {
  private readonly service = inject(HomeProductService);
  readonly essentials = signal<DailyEssential[]>([]);

  ngOnInit(): void {
    this.service.getDailyEssentials().subscribe((e) => this.essentials.set(e));
  }
}
