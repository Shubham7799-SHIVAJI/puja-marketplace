import { HttpClient, HttpEvent, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiErrorService } from './api-error';

export interface ShopRegistrationPayload {
  registrationId: string | null;
  currentStep: number;
  ownerFullName: string;
  email: string;
  phoneNumber: string;
  password: string;
  confirmPassword: string;
  emailOtp: string;
  phoneOtp: string;
  profilePhoto: string;
  shopName: string;
  shopUniqueId: string;
  shopCategory: string;
  shopDescription: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  landmark: string;
  latitude: number | null;
  longitude: number | null;
  shopPhoneNumber: string;
  shopEmail: string;
  whatsappNumber: string;
  ownerAadharNumber: string;
  ownerPanNumber: string;
  ownerAadharPhoto: string;
  ownerPanPhoto: string;
  ownerSelfieWithId: string;
  gstNumber: string;
  gstCertificateUpload: string;
  businessRegistrationNumber: string;
  accountHolderName: string;
  bankName: string;
  accountNumber: string;
  confirmAccountNumber: string;
  ifscCode: string;
  upiId: string;
  cancelledChequePhoto: string;
  acceptTermsAndConditions: boolean;
  acceptPrivacyPolicy: boolean;
  acceptCommissionPolicy: boolean;
}

export interface ShopRegistrationResponse {
  registrationId: string;
  shopUniqueId: string;
  status: string;
  currentStep: number;
  ownerFullName: string | null;
  email: string | null;
  phoneNumber: string | null;
  emailOtp: string | null;
  emailOtpVerified: boolean;
  phoneOtp: string | null;
  phoneOtpVerified: boolean;
  profilePhoto: string | null;
  shopName: string | null;
  shopCategory: string | null;
  shopDescription: string | null;
  addressLine1: string | null;
  addressLine2: string | null;
  city: string | null;
  state: string | null;
  pincode: string | null;
  country: string | null;
  landmark: string | null;
  latitude: number | null;
  longitude: number | null;
  shopPhoneNumber: string | null;
  shopEmail: string | null;
  whatsappNumber: string | null;
  ownerAadharNumber: string | null;
  ownerPanNumber: string | null;
  ownerAadharPhoto: string | null;
  ownerPanPhoto: string | null;
  ownerSelfieWithId: string | null;
  gstNumber: string | null;
  gstCertificateUpload: string | null;
  businessRegistrationNumber: string | null;
  accountHolderName: string | null;
  bankName: string | null;
  accountNumber: string | null;
  ifscCode: string | null;
  upiId: string | null;
  cancelledChequePhoto: string | null;
  acceptTermsAndConditions: boolean;
  acceptPrivacyPolicy: boolean;
  acceptCommissionPolicy: boolean;
  lastSavedAt: string | null;
  submittedAt: string | null;
}

export interface ShopFileUploadResponse {
  registrationId: string;
  shopUniqueId: string;
  fieldName: string;
  fileName: string;
  filePath: string;
  contentType: string;
  size: number;
}

export interface ShopOtpSendRequest {
  registrationId: string | null;
  channel: 'EMAIL' | 'PHONE';
  contact: string;
  ownerFullName?: string;
}

export interface ShopOtpVerifyRequest {
  registrationId: string | null;
  channel: 'EMAIL' | 'PHONE';
  contact: string;
  otp: string;
}

export interface ShopOtpResponse {
  registrationId: string;
  channel: 'EMAIL' | 'PHONE';
  contact: string;
  verified: boolean;
  message: string;
  previewOtp: string | null;
  registrationSessionToken: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class ShopRegistrationService {
  private readonly api = 'http://localhost:8080/shop-registration';
  private readonly sessionTokenStorageKey = 'shop-registration-session-tokens';

  constructor(
    private readonly http: HttpClient,
    private readonly apiErrorService: ApiErrorService,
  ) {}

  saveDraft(payload: ShopRegistrationPayload): Observable<ShopRegistrationResponse> {
    return this.http.post<ShopRegistrationResponse>(`${this.api}/draft`, payload);
  }

  getDraft(registrationId: string): Observable<ShopRegistrationResponse> {
    return this.http.get<ShopRegistrationResponse>(`${this.api}/draft/${registrationId}`);
  }

  submit(payload: ShopRegistrationPayload): Observable<ShopRegistrationResponse> {
    return this.http.post<ShopRegistrationResponse>(`${this.api}/submit`, payload);
  }

  sendOtp(payload: ShopOtpSendRequest): Observable<ShopOtpResponse> {
    return this.http.post<ShopOtpResponse>(`${this.api}/otp/send`, payload);
  }

  verifyOtp(payload: ShopOtpVerifyRequest): Observable<ShopOtpResponse> {
    return new Observable<ShopOtpResponse>((observer) => {
      this.http.post<ShopOtpResponse>(`${this.api}/otp/verify`, payload).subscribe({
        next: (response) => {
          if (response.registrationId && response.registrationSessionToken) {
            this.setRegistrationSessionToken(response.registrationId, response.registrationSessionToken);
          }
          observer.next(response);
          observer.complete();
        },
        error: (error) => observer.error(error),
      });
    });
  }

  uploadFile(file: File, fieldName: string, registrationId?: string): Observable<HttpEvent<ShopFileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fieldName', fieldName);

    if (registrationId) {
      formData.append('registrationId', registrationId);
    }

    const sessionToken = registrationId ? this.getRegistrationSessionToken(registrationId) : null;
    const headers = sessionToken
      ? new HttpHeaders({
          'X-Registration-Session': sessionToken,
        })
      : undefined;

    return this.http.post<ShopFileUploadResponse>(`${this.api}/upload`, formData, {
      reportProgress: true,
      observe: 'events',
      headers,
    });
  }

  private setRegistrationSessionToken(registrationId: string, token: string): void {
    const tokens = this.getRegistrationSessionTokenMap();
    tokens[registrationId] = token;
    sessionStorage.setItem(this.sessionTokenStorageKey, JSON.stringify(tokens));
  }

  private getRegistrationSessionToken(registrationId: string): string | null {
    return this.getRegistrationSessionTokenMap()[registrationId] ?? null;
  }

  private getRegistrationSessionTokenMap(): Record<string, string> {
    const raw = sessionStorage.getItem(this.sessionTokenStorageKey);
    if (!raw) {
      return {};
    }

    try {
      const parsed = JSON.parse(raw) as Record<string, string>;
      return parsed ?? {};
    } catch {
      return {};
    }
  }

  getFriendlyErrorMessage(error: unknown, fallbackMessage: string): string {
    const parsed = this.apiErrorService.parse(error);
    return parsed?.message ?? fallbackMessage;
  }

  getFieldErrors(error: unknown): Record<string, string> {
    const parsed = this.apiErrorService.parse(error);
    return parsed?.fieldErrors ?? {};
  }
}