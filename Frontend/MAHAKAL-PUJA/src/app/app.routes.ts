import { Routes } from '@angular/router';
import { LoginPage } from './components/login-page/login-page';
import { SignInPage } from './components/sign-in-page/sign-in-page';

export const routes: Routes = [
  { path: '', component: LoginPage },
  { path: 'sign-in', component: SignInPage }
];
