import { Component } from '@angular/core';
import { HeroBanner } from './sections/hero-banner/hero-banner';
import { CategoryBar } from './sections/category-bar/category-bar';
import { FeaturedKits } from './sections/featured-kits/featured-kits';
import { NearbyShops } from './sections/nearby-shops/nearby-shops';
import { ProductGrid } from './sections/product-grid/product-grid';
import { DailyEssentials } from './sections/daily-essentials/daily-essentials';
import { FestivalCollectionComponent } from './sections/festival-collection/festival-collection';
import { CustomerReviews } from './sections/customer-reviews/customer-reviews';
import { HomeFooter } from './sections/home-footer/home-footer';

@Component({
  standalone: true,
  selector: 'app-home-page',
  imports: [
    HeroBanner,
    CategoryBar,
    FeaturedKits,
    NearbyShops,
    ProductGrid,
    DailyEssentials,
    FestivalCollectionComponent,
    CustomerReviews,
    HomeFooter,
  ],
  templateUrl: './home-page.html',
  styleUrl: './home-page.scss',
})
export class HomePage {}

