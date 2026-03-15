import { Routes } from '@angular/router';
import { SignUpPage } from './components/sign-up-page/sign-up-page';
import { OtpPage } from './components/otp-page/otp-page';
import { SignInPage } from './components/sign-in-page/sign-in-page';
import { PasswordPage } from './components/password-page/password-page';
import { HomePage } from './components/home-page/home-page';
import { ShopRegistration } from './components/shop-registration/shop-registration';
import { setPasswordGuard } from './shared/guards/set-password.guard';

export const routes: Routes = [
  { path: '', component: SignUpPage },
  { path: 'sign-up', component: SignUpPage },
  { path: 'signin', component: SignInPage },
  { path: 'sign-in', redirectTo: 'signin', pathMatch: 'full' },
  { path: 'otp', component: OtpPage },
  { path: 'set-password', component: PasswordPage, canActivate: [setPasswordGuard] },
  { path: 'password-page', redirectTo: 'set-password', pathMatch: 'full' },
  { path: 'shop-registration', component: ShopRegistration },
  {
    path: 'shop-dashboard/:registrationId',
    loadComponent: () => import('./components/seller-workspace/seller-workspace').then((m) => m.SellerWorkspace),
  },
  {
    path: 'seller-dashboard/:registrationId',
    loadComponent: () => import('./components/seller-workspace/seller-workspace').then((m) => m.SellerWorkspace),
  },
  {
    path: 'seller-dashboard',
    loadComponent: () => import('./components/seller-workspace/seller-workspace').then((m) => m.SellerWorkspace),
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  { path: 'admin-dashboard', redirectTo: 'admin/dashboard', pathMatch: 'full' },
  {
    path: 'admin-otp',
    loadComponent: () => import('./components/admin-otp-page/admin-otp-page').then((m) => m.AdminOtpPage),
  },
  {
    path: 'product/:productId',
    loadComponent: () => import('./components/product-detail-page/product-detail-page').then((m) => m.ProductDetailPage),
  },
  {
    path: 'wishlist',
    loadComponent: () => import('./components/wishlist-page/wishlist-page').then((m) => m.WishlistPage),
  },
  {
    path: 'checkout',
    loadComponent: () => import('./components/checkout-page/checkout-page').then((m) => m.CheckoutPage),
  },
  {
    path: 'orders',
    loadComponent: () => import('./components/orders-page/orders-page').then((m) => m.OrdersPage),
  },
  { path: 'home', component: HomePage },
  { path: '**', redirectTo: 'sign-up' },
];
