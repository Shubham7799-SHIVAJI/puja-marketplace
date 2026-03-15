import { HttpClient, HttpErrorResponse, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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
}

export interface ApiErrorResponse {
  code?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
}

@Injectable({
  providedIn: 'root',
})
export class ShopRegistrationService {
  private readonly api = 'http://localhost:8080/shop-registration';

  constructor(private readonly http: HttpClient) {}

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
    return this.http.post<ShopOtpResponse>(`${this.api}/otp/verify`, payload);
  }

  uploadFile(file: File, fieldName: string, registrationId?: string): Observable<HttpEvent<ShopFileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fieldName', fieldName);

    if (registrationId) {
      formData.append('registrationId', registrationId);
    }

    return this.http.post<ShopFileUploadResponse>(`${this.api}/upload`, formData, {
      reportProgress: true,
      observe: 'events',
    });
  }

  getFriendlyErrorMessage(error: unknown, fallbackMessage: string): string {
    const parsed = this.parseError(error);
    return parsed?.message ?? fallbackMessage;
  }

  getFieldErrors(error: unknown): Record<string, string> {
    const parsed = this.parseError(error);
    return parsed?.fieldErrors ?? {};
  }

  private parseError(error: unknown): ApiErrorResponse | null {
    if (error instanceof HttpErrorResponse) {
      if (error.error && typeof error.error === 'object') {
        return error.error as ApiErrorResponse;
      }

      return { message: error.message };
    }

    if (typeof error === 'object' && error !== null) {
      return error as ApiErrorResponse;
    }

    return null;
  }
}