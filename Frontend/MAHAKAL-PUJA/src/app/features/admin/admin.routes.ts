import { Routes } from '@angular/router';

import { adminGuard, adminMatchGuard } from '../../shared/guards/admin.guard';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    canMatch: [adminMatchGuard],
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'dashboard' },
      },
      {
        path: 'users',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'users' },
      },
      {
        path: 'sellers',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'sellers' },
      },
      {
        path: 'products',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'products' },
      },
      {
        path: 'orders',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'orders' },
      },
      {
        path: 'payments-refunds',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'finance' },
      },
      {
        path: 'reviews-ratings',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'reviews' },
      },
      {
        path: 'support-tickets',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'support' },
      },
      {
        path: 'analytics-reports',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'analytics' },
      },
      {
        path: 'system-settings',
        loadComponent: () => import('../../components/admin-workspace/admin-workspace').then((m) => m.AdminWorkspace),
        data: { section: 'system' },
      },
    ],
  },
];
