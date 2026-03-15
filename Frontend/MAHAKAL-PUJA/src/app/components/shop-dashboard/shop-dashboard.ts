import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ShopRegistrationResponse, ShopRegistrationService } from '../../shared/services/shop-registration';

@Component({
  standalone: true,
  selector: 'app-shop-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './shop-dashboard.html',
  styleUrl: './shop-dashboard.scss',
})
export class ShopDashboard implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly shopRegistrationService = inject(ShopRegistrationService);
  private readonly destroyRef = inject(DestroyRef);

  dashboard: ShopRegistrationResponse | null = null;
  loading = true;
  error = '';

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const registrationId = params.get('registrationId');

      if (!registrationId) {
        this.loading = false;
        this.error = 'Registration details are missing for this dashboard.';
        return;
      }

      this.loading = true;
      this.error = '';

      this.shopRegistrationService
        .getDraft(registrationId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (response) => {
            this.dashboard = response;
            this.loading = false;
          },
          error: (error) => {
            this.error = this.shopRegistrationService.getFriendlyErrorMessage(
              error,
              'Unable to load your shop dashboard right now.',
            );
            this.loading = false;
          },
        });
    });
  }
}