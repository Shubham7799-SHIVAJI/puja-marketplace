import { Injectable } from '@angular/core';

export interface AdminChallengeSession {
  contact: string;
  challengeToken: string;
  createdAt: number;
}

@Injectable({
  providedIn: 'root',
})
export class AdminAuthFlowService {
  private readonly storageKey = 'mahakal-admin-mfa-session';
  private readonly maxAgeMs = 10 * 60 * 1000;

  save(session: AdminChallengeSession): void {
    sessionStorage.setItem(this.storageKey, JSON.stringify(session));
  }

  get(): AdminChallengeSession | null {
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as AdminChallengeSession;
      if (!parsed.contact || !parsed.challengeToken || !parsed.createdAt) {
        this.clear();
        return null;
      }

      if (Date.now() - parsed.createdAt > this.maxAgeMs) {
        this.clear();
        return null;
      }

      return parsed;
    } catch {
      this.clear();
      return null;
    }
  }

  clear(): void {
    sessionStorage.removeItem(this.storageKey);
  }
}
