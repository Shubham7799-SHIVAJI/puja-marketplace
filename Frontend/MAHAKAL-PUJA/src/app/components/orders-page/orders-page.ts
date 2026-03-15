import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MARKETPLACE_TEXT } from '../../shared/constants/marketplace.constants';
import { CustomerOrder } from '../../shared/models/marketplace';
import { MarketplaceService } from '../../shared/services/marketplace';

@Component({
  standalone: true,
  selector: 'app-orders-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './orders-page.html',
  styleUrl: './orders-page.scss',
})
export class OrdersPage implements OnInit {
  private readonly marketplaceService = inject(MarketplaceService);

  readonly text = MARKETPLACE_TEXT.orders;
  readonly commonText = MARKETPLACE_TEXT.common;
  readonly orders = signal<CustomerOrder[]>([]);
  readonly loading = signal(true);

  async ngOnInit(): Promise<void> {
    try {
      this.orders.set(await firstValueFrom(this.marketplaceService.getOrders()));
    } finally {
      this.loading.set(false);
    }
  }
}