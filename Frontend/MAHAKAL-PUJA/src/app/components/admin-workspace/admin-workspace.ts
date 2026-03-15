import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AnalyticsChart } from '../../shared/components/analytics-chart/analytics-chart';
import { DataTable } from '../../shared/components/data-table/data-table';
import { MetricCard } from '../../shared/components/metric-card/metric-card';
import { NotificationsPanel } from '../../shared/components/notifications-panel/notifications-panel';
import { ADMIN_DASHBOARD_TEXT } from '../../shared/constants/admin-dashboard.constants';
import { AdminSectionKey, AdminWorkspaceData } from '../../shared/models/admin-dashboard';
import { AlertItem } from '../../shared/models/seller-dashboard';
import { AdminDashboardService } from '../../shared/services/admin-dashboard';
import { AuthSessionService } from '../../shared/services/auth-session';

@Component({
  selector: 'app-admin-workspace',
  standalone: true,
  imports: [CommonModule, RouterLink, MetricCard, DataTable, AnalyticsChart, NotificationsPanel],
  templateUrl: './admin-workspace.html',
  styleUrl: './admin-workspace.scss',
})
export class AdminWorkspace implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly adminDashboardService = inject(AdminDashboardService);
  private readonly authSessionService = inject(AuthSessionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly text = ADMIN_DASHBOARD_TEXT;

  loading = true;
  error = '';
  readonly activeSection = signal<AdminSectionKey>('dashboard');
  readonly workspace = signal<AdminWorkspaceData | null>(null);
  readonly session = this.authSessionService.getSession();

  readonly currentSectionText = computed(() => this.text.sections[this.activeSection()]);

  ngOnInit(): void {
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      const section = data['section'] as AdminSectionKey | undefined;
      if (section) {
        this.activeSection.set(section);
      }
    });

    this.adminDashboardService
      .getWorkspaceData()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (workspace) => {
          this.workspace.set(workspace);
          this.loading = false;
        },
        error: () => {
          this.error = this.text.emptyError;
          this.loading = false;
        },
      });
  }

  selectSection(section: AdminSectionKey): void {
    this.activeSection.set(section);
    this.router.navigate(['/admin', this.sectionToRoute(section)]);
  }

  trackByNav(_: number, item: { key: AdminSectionKey }): AdminSectionKey {
    return item.key;
  }

  trackByAlert(_: number, item: AlertItem): string {
    return `${item.label}-${item.value}`;
  }

  signOut(): void {
    this.authSessionService.clear();
    window.location.href = '/signin';
  }

  private sectionToRoute(section: AdminSectionKey): string {
    switch (section) {
      case 'finance':
        return 'payments-refunds';
      case 'reviews':
        return 'reviews-ratings';
      case 'support':
        return 'support-tickets';
      case 'analytics':
        return 'analytics-reports';
      case 'system':
        return 'system-settings';
      default:
        return section;
    }
  }
}
