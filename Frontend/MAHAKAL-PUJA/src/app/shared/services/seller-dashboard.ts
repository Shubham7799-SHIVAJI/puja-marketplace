import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { SellerWorkspaceData } from '../models/seller-dashboard';
import { ShopRegistrationResponse } from './shop-registration';

@Injectable({
  providedIn: 'root',
})
export class SellerDashboardService {
  private readonly api = 'http://localhost:8080/seller-dashboard/workspace';

  constructor(private readonly http: HttpClient) {}

  getWorkspaceData(shop?: ShopRegistrationResponse | null): Observable<SellerWorkspaceData> {
    if (shop?.registrationId) {
      return this.http.get<SellerWorkspaceData>(`${this.api}/${shop.registrationId}`);
    }

    return this.http.get<SellerWorkspaceData>(this.api);
  }
}
