import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface HeroSlide {
  id: number;
  title: string;
  subtitle: string;
  badge: string;
  price: number;
  originalPrice: number;
  discount: number;
  bgGradient: string;
  accentColor: string;
  icon: string;
}

export interface PujaCategory {
  id: number;
  name: string;
  icon: string;
  slug: string;
  count: number;
}

export interface FeaturedKit {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice: number;
  discount: number;
  rating: number;
  reviews: number;
  icon: string;
  color: string;
  items: string[];
}

export interface NearbyProduct {
  id: number;
  name: string;
  shopName: string;
  shopLocation: string;
  price: number;
  originalPrice: number;
  discount: number;
  rating: number;
  reviews: number;
  icon: string;
  deliveryTime: string;
  isNearby: boolean;
}

export interface HomeProduct {
  id: number;
  name: string;
  sellerName: string;
  category: string;
  price: number;
  originalPrice: number;
  discount: number;
  rating: number;
  reviews: number;
  icon: string;
  inStock: boolean;
  wishlisted: boolean;
}

export interface DailyEssential {
  id: number;
  name: string;
  description: string;
  price: number;
  icon: string;
}

export interface FestivalProduct {
  id: number;
  name: string;
  price: number;
  discount: number;
  icon: string;
}

export interface FestivalCollection {
  id: number;
  festival: string;
  date: string;
  products: FestivalProduct[];
}

export interface CustomerReview {
  id: number;
  name: string;
  location: string;
  rating: number;
  text: string;
  date: string;
  avatar: string;
  verified: boolean;
}

export interface HomeData {
  heroSlides: HeroSlide[];
  categories: PujaCategory[];
  featuredKits: FeaturedKit[];
  nearbyProducts: NearbyProduct[];
  products: HomeProduct[];
  dailyEssentials: DailyEssential[];
  festivalProducts: FestivalCollection[];
  reviews: CustomerReview[];
}

@Injectable({ providedIn: 'root' })
export class HomeProductService {
  private readonly http = inject(HttpClient);
  private readonly dataUrl = 'assets/data/home-data.json';

  getHomeData(): Observable<HomeData> {
    return this.http.get<HomeData>(this.dataUrl).pipe(
      catchError(() => of({} as HomeData)),
    );
  }

  getHeroSlides(): Observable<HeroSlide[]> {
    return this.getHomeData().pipe(map((d) => d.heroSlides ?? []));
  }

  getCategories(): Observable<PujaCategory[]> {
    return this.getHomeData().pipe(map((d) => d.categories ?? []));
  }

  getFeaturedKits(): Observable<FeaturedKit[]> {
    return this.getHomeData().pipe(map((d) => d.featuredKits ?? []));
  }

  getNearbyProducts(): Observable<NearbyProduct[]> {
    return this.getHomeData().pipe(map((d) => d.nearbyProducts ?? []));
  }

  getProducts(): Observable<HomeProduct[]> {
    return this.getHomeData().pipe(map((d) => d.products ?? []));
  }

  getDailyEssentials(): Observable<DailyEssential[]> {
    return this.getHomeData().pipe(map((d) => d.dailyEssentials ?? []));
  }

  getFestivalCollections(): Observable<FestivalCollection[]> {
    return this.getHomeData().pipe(map((d) => d.festivalProducts ?? []));
  }

  getReviews(): Observable<CustomerReview[]> {
    return this.getHomeData().pipe(map((d) => d.reviews ?? []));
  }
}
