import { Routes } from '@angular/router';
import { SignUpPage } from './components/sign-up-page/sign-up-page';
import { OtpPage } from './components/otp-page/otp-page';
import { SignInPage } from './components/sign-in-page/sign-in-page';
import { PasswordPage } from './components/password-page/password-page';
import { HomePage } from './components/home-page/home-page';
import { ShopDashboard } from './components/shop-dashboard/shop-dashboard';
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
  { path: 'shop-dashboard/:registrationId', component: ShopDashboard },
  { path: 'home', component: HomePage },
  { path: '**', redirectTo: 'sign-up' },
];
