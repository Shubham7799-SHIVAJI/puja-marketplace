import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiErrorService } from './api-error';

export interface LoginRequest {
  name: string;
  contact: string;
}

export interface LoginResponse {
  message: string;
  otpSent: string;
}

export interface ResendOtpRequest {
  contact: string;
}

export interface ResendOtpResponse {
  message: string;
}

export interface VerifyOtpRequest {
  contact: string;
  otp: string;
}

export interface VerifyOtpResponse {
  message: string;
  resetToken: string;
}

export interface SetPasswordRequest {
  contact: string;
  password: string;
  confirmPassword: string;
  resetToken: string;
}

export interface SetPasswordResponse {
  message: string;
}

export interface SigninRequest {
  contact: string;
  password: string;
}

export interface SigninResponse {
  token: string;
  refreshToken: string;
  email: string;
  role: string;
  expiresInMinutes: number;
  refreshExpiresInDays: number;
  message: string;
}

export interface AdminLoginChallengeResponse {
  mfaRequired: boolean;
  challengeToken: string;
  expiresInMinutes: number;
  message: string;
}

export interface AuthErrorResponse {
  timestamp?: string;
  status?: number;
  code?: string;
  error?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly api = 'http://localhost:8080/auth';
  private readonly codeMessageMap: Record<string, string> = {
    INVALID_OTP: 'The OTP you entered is incorrect. Please try again.',
    OTP_EXPIRED: 'Your OTP has expired. Please request a new OTP.',
    OTP_NOT_FOUND: 'No OTP was found for this email. Please request a new OTP.',
    USER_NOT_FOUND: 'No user found for this email. Please sign up again.',
    INVALID_CREDENTIALS: 'Email or password is incorrect. Please try again.',
    PASSWORD_NOT_SET: 'Password is not set for this email. Please set password first.',
    EMAIL_NOT_VERIFIED: 'Email is not verified. Please complete OTP verification first.',
    ADMIN_2FA_REQUIRED: 'Admin account requires OTP verification before access.',
  };

  constructor(
    private http: HttpClient,
    private apiErrorService: ApiErrorService,
  ) {}

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.api}/login`, data);
  }

  resendOtp(data: ResendOtpRequest): Observable<ResendOtpResponse> {
    return this.http.post<ResendOtpResponse>(`${this.api}/resend-otp`, data);
  }

  verifyOtp(data: VerifyOtpRequest): Observable<VerifyOtpResponse> {
    return this.http.post<VerifyOtpResponse>(`${this.api}/verify-otp`, data);
  }

  setPassword(contact: string, password: string, confirmPassword: string, resetToken: string): Observable<SetPasswordResponse> {
    const payload: SetPasswordRequest = {
      contact,
      password,
      confirmPassword,
      resetToken,
    };

    return this.http.post<SetPasswordResponse>(`${this.api}/set-password`, payload);
  }

  signin(contact: string, password: string): Observable<SigninResponse> {
    const payload: SigninRequest = {
      contact,
      password,
    };

    return this.http.post<SigninResponse>(`${this.api}/signin`, payload);
  }

  startAdminLoginChallenge(contact: string, password: string): Observable<AdminLoginChallengeResponse> {
    return this.http.post<AdminLoginChallengeResponse>(`${this.api}/admin/challenge`, {
      contact,
      password,
    });
  }

  verifyAdminOtp(challengeToken: string, otp: string): Observable<SigninResponse> {
    return this.http.post<SigninResponse>(`${this.api}/admin/verify-otp`, {
      challengeToken,
      otp,
    });
  }

  getErrorCode(error: unknown): string | undefined {
    return this.apiErrorService.parse(error)?.code;
  }

  getFriendlyErrorMessage(error: unknown, fallbackMessage: string): string {
    const parsed = this.apiErrorService.parse(error);

    if (!parsed) {
      return fallbackMessage;
    }

    if (parsed.code === 'VALIDATION_ERROR') {
      const fieldErrors = parsed.fieldErrors ?? {};
      const validationMessages = Object.values(fieldErrors).filter(Boolean);
      if (validationMessages.length > 0) {
        return validationMessages.join(' ');
      }
    }

    if (parsed.code && this.codeMessageMap[parsed.code]) {
      return this.codeMessageMap[parsed.code];
    }

    if (parsed.message) {
      return parsed.message;
    }

    return fallbackMessage;
  }

  getFieldErrors(error: unknown): Record<string, string> {
    const parsed = this.apiErrorService.parse(error);
    if (!parsed?.fieldErrors) {
      return {};
    }

    return parsed.fieldErrors;
  }
}