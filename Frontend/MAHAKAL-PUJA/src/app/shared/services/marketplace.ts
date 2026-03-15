import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CategoryOption,
  CheckoutPayload,
  CheckoutResponse,
  CustomerAddress,
  CustomerAddressPayload,
  CustomerNotification,
  CustomerOrder,
  CustomerReviewPayload,
  MarketplaceHighlights,
  MarketplaceProductCard,
  MarketplaceProductDetail,
  PageResponse,
  ReviewItem,
  WishlistItem,
} from '../models/marketplace';

@Injectable({
  providedIn: 'root',
})
export class MarketplaceService {
  private readonly api = 'http://localhost:8080/marketplace';

  constructor(private readonly http: HttpClient) {}

  getCatalog(filters: {
    query?: string;
    category?: string;
    brand?: string;
    minPrice?: number;
    maxPrice?: number;
    minRating?: number;
    inStockOnly?: boolean;
    sortBy?: string;
    page?: number;
    size?: number;
  }): Observable<PageResponse<MarketplaceProductCard>> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<PageResponse<MarketplaceProductCard>>(`${this.api}/catalog`, { params });
  }

  getSuggestions(query: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/catalog/suggestions`, {
      params: new HttpParams().set('query', query),
    });
  }

  getCategories(): Observable<CategoryOption[]> {
    return this.http.get<CategoryOption[]>(`${this.api}/categories`);
  }

  getHighlights(): Observable<MarketplaceHighlights> {
    return this.http.get<MarketplaceHighlights>(`${this.api}/highlights`);
  }

  getProductDetail(productId: number): Observable<MarketplaceProductDetail> {
    return this.http.get<MarketplaceProductDetail>(`${this.api}/products/${productId}`);
  }

  compareProducts(ids: number[]): Observable<MarketplaceProductCard[]> {
    return this.http.get<MarketplaceProductCard[]>(`${this.api}/compare`, {
      params: new HttpParams().set('ids', ids.join(',')),
    });
  }

  getWishlist(): Observable<WishlistItem[]> {
    return this.http.get<WishlistItem[]>(`${this.api}/wishlist`);
  }

  addToWishlist(productId: number): Observable<WishlistItem> {
    return this.http.post<WishlistItem>(`${this.api}/wishlist/${productId}`, {});
  }

  removeFromWishlist(productId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/wishlist/${productId}`);
  }

  getAddresses(): Observable<CustomerAddress[]> {
    return this.http.get<CustomerAddress[]>(`${this.api}/addresses`);
  }

  createAddress(payload: CustomerAddressPayload): Observable<CustomerAddress> {
    return this.http.post<CustomerAddress>(`${this.api}/addresses`, payload);
  }

  updateAddress(addressId: number, payload: CustomerAddressPayload): Observable<CustomerAddress> {
    return this.http.put<CustomerAddress>(`${this.api}/addresses/${addressId}`, payload);
  }

  deleteAddress(addressId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/addresses/${addressId}`);
  }

  checkout(payload: CheckoutPayload): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.api}/checkout`, payload);
  }

  getOrders(): Observable<CustomerOrder[]> {
    return this.http.get<CustomerOrder[]>(`${this.api}/orders`);
  }

  getOrder(orderId: number): Observable<CustomerOrder> {
    return this.http.get<CustomerOrder>(`${this.api}/orders/${orderId}`);
  }

  createReview(payload: CustomerReviewPayload): Observable<ReviewItem> {
    return this.http.post<ReviewItem>(`${this.api}/reviews`, payload);
  }

  getNotifications(): Observable<CustomerNotification[]> {
    return this.http.get<CustomerNotification[]>(`${this.api}/notifications`);
  }
}