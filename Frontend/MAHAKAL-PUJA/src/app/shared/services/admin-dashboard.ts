import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AdminWorkspaceData } from '../models/admin-dashboard';

@Injectable({
  providedIn: 'root',
})
export class AdminDashboardService {
  private readonly api = 'http://localhost:8080/api/admin/dashboard/workspace';

  constructor(private readonly http: HttpClient) {}

  getWorkspaceData(): Observable<AdminWorkspaceData> {
    return this.http.get<AdminWorkspaceData>(this.api);
  }
}
