import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';

import { AnalyticsChart } from '../../shared/components/analytics-chart/analytics-chart';
import { DataTable } from '../../shared/components/data-table/data-table';
import { MetricCard } from '../../shared/components/metric-card/metric-card';
import { NotificationsPanel } from '../../shared/components/notifications-panel/notifications-panel';
import { SellerFormField } from '../../shared/components/seller-form-field/seller-form-field';
import {
  AlertItem,
  SellerNavItem,
  SellerSectionKey,
  SellerWorkspaceData,
} from '../../shared/models/seller-dashboard';
import { SellerDashboardService } from '../../shared/services/seller-dashboard';
import { ShopRegistrationResponse, ShopRegistrationService } from '../../shared/services/shop-registration';

@Component({
  selector: 'app-seller-workspace',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MetricCard,
    DataTable,
    AnalyticsChart,
    NotificationsPanel,
    SellerFormField,
  ],
  templateUrl: './seller-workspace.html',
  styleUrl: './seller-workspace.scss',
})
export class SellerWorkspace implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly sellerDashboardService = inject(SellerDashboardService);
  private readonly shopRegistrationService = inject(ShopRegistrationService);

  loading = true;
  error = '';
  activeSection: SellerSectionKey = 'dashboard';
  registration: ShopRegistrationResponse | null = null;
  workspace: SellerWorkspaceData | null = null;

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const registrationId = params.get('registrationId');
      this.loading = true;
      this.error = '';

      const registrationRequest = registrationId
        ? this.shopRegistrationService.getDraft(registrationId)
        : of(null);

      forkJoin({
        registration: registrationRequest,
      })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: ({ registration }) => {
            this.registration = registration;

            this.sellerDashboardService
              .getWorkspaceData(registration)
              .pipe(takeUntilDestroyed(this.destroyRef))
              .subscribe({
                next: (workspace) => {
                  this.workspace = workspace;
                  this.loading = false;
                },
                error: () => {
                  this.error = 'Unable to assemble the seller workspace right now.';
                  this.loading = false;
                },
              });
          },
          error: (error) => {
            this.error = this.shopRegistrationService.getFriendlyErrorMessage(
              error,
              'Unable to load seller details right now.',
            );
            this.loading = false;
          },
        });
    });
  }

  selectSection(section: SellerSectionKey): void {
    this.activeSection = section;
  }

  trackByNav(_: number, item: SellerNavItem): SellerSectionKey {
    return item.key;
  }

  trackByAlert(_: number, item: AlertItem): string {
    return `${item.label}-${item.value}`;
  }
}