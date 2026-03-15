import { Component, inject, signal, HostListener } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerCartService } from '../../services/customer-cart';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private readonly router = inject(Router);
  readonly cartService = inject(CustomerCartService);

  readonly searchQuery = signal('');
  readonly mobileMenuOpen = signal(false);
  readonly scrolled = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled.set(window.scrollY > 60);
  }

  search(): void {
    const q = this.searchQuery().trim();
    if (q) this.router.navigate(['/home'], { queryParams: { q } });
  }

  toggleMenu(): void {
    this.mobileMenuOpen.update((v) => !v);
  }
}
