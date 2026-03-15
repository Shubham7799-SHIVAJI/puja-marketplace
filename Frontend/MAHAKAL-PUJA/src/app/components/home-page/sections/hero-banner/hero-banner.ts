import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { HomeProductService, HeroSlide } from '../../../../shared/services/home-product';

@Component({
  standalone: true,
  selector: 'app-hero-banner',
  imports: [CommonModule],
  templateUrl: './hero-banner.html',
  styleUrl: './hero-banner.scss',
})
export class HeroBanner implements OnInit, OnDestroy {
  private readonly service = inject(HomeProductService);

  readonly slides = signal<HeroSlide[]>([]);
  readonly activeIndex = signal(0);
  private timer: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.service.getHeroSlides().subscribe((slides) => {
      this.slides.set(slides);
      if (slides.length > 1) {
        this.timer = setInterval(() => {
          this.activeIndex.update((i) => (i + 1) % slides.length);
        }, 4500);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }

  goTo(index: number): void {
    this.activeIndex.set(index);
  }

  prev(): void {
    this.activeIndex.update((i) => (i - 1 + this.slides().length) % this.slides().length);
  }

  next(): void {
    this.activeIndex.update((i) => (i + 1) % this.slides().length);
  }
}
