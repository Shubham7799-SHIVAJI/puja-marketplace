import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { Header } from './shared/components/header/header';
import { AuthHeader } from './shared/components/auth-header/auth-header';

@Component({
  selector: 'app-root',
  imports: [Header, AuthHeader, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly title = signal('MAHAKAL-PUJA');
  protected readonly usesSellerLayout = signal(this.isSellerLayout(this.router.url));
  protected readonly usesAuthLayout = signal(this.isAuthLayout(this.router.url));

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event) => {
        this.usesSellerLayout.set(this.isSellerLayout(event.urlAfterRedirects));
        this.usesAuthLayout.set(this.isAuthLayout(event.urlAfterRedirects));
      });
  }

  private isSellerLayout(url: string): boolean {
    return (
      url.includes('/shop-dashboard') ||
      url.includes('/seller-dashboard') ||
      url.includes('/admin-dashboard')
    );
  }

  /** Auth pages: signin, signup, otp, set-password, shop-registration */
  private isAuthLayout(url: string): boolean {
    return (
      url === '/' ||
      url.startsWith('/sign-up') ||
      url.startsWith('/signin') ||
      url.startsWith('/sign-in') ||
      url.startsWith('/otp') ||
      url.startsWith('/set-password') ||
      url.startsWith('/password-page') ||
      url.startsWith('/shop-registration')
    );
  }
}
