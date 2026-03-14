import { Injectable } from '@angular/core';

export interface OtpSecurityContext {
  flow: string;
  email: string;
  verifiedAt: number;
}

@Injectable({
  providedIn: 'root',
})
export class OtpSecurityContextService {
  private readonly storageKey = 'otp_security_context';
  private readonly maxAgeMs = 10 * 60 * 1000;

  setVerifiedContext(flow: string, email: string) {
    const payload: OtpSecurityContext = {
      flow,
      email,
      verifiedAt: Date.now(),
    };

    sessionStorage.setItem(this.storageKey, JSON.stringify(payload));
  }

  getContext(): OtpSecurityContext | null {
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as OtpSecurityContext;
      if (!parsed.flow || !parsed.email || !parsed.verifiedAt) {
        return null;
      }

      return parsed;
    } catch {
      return null;
    }
  }

  isContextValid(): boolean {
    const context = this.getContext();
    if (!context) {
      return false;
    }

    return Date.now() - context.verifiedAt <= this.maxAgeMs;
  }

  clearContext() {
    sessionStorage.removeItem(this.storageKey);
  }
}
