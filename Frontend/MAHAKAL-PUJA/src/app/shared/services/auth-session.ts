import { Injectable } from '@angular/core';

export interface AuthSession {
  token: string;
  refreshToken: string;
  email: string;
  role: string;
  expiresInMinutes: number;
  refreshExpiresInDays: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthSessionService {
  private readonly storageKey = 'mahakal-auth-session';

  save(session: AuthSession): void {
    localStorage.setItem(this.storageKey, JSON.stringify(session));
  }

  getSession(): AuthSession | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }

  getToken(): string | null {
    return this.getSession()?.token ?? null;
  }

  clear(): void {
    localStorage.removeItem(this.storageKey);
  }
}
