import { Injectable } from '@angular/core';
import { FirebaseApp, getApp, getApps, initializeApp } from 'firebase/app';
import {
  Auth,
  ConfirmationResult,
  RecaptchaVerifier,
  UserCredential,
  getAuth,
  signInWithPhoneNumber,
} from 'firebase/auth';

import { firebasePhoneAuthConfig, isFirebasePhoneAuthConfigured } from '../config/firebase.config';

export interface FirebasePhoneVerificationResult {
  phoneNumber: string | null;
  providerUid: string;
  idToken: string;
}

@Injectable({
  providedIn: 'root',
})
export class FirebasePhoneAuthService {
  private readonly app: FirebaseApp;
  private readonly auth: Auth;

  private confirmationResult: ConfirmationResult | null = null;
  private recaptchaVerifier: RecaptchaVerifier | null = null;
  private recaptchaContainerId: string | null = null;

  constructor() {
    this.app = getApps().length ? getApp() : initializeApp(firebasePhoneAuthConfig);
    this.auth = getAuth(this.app);
    this.auth.languageCode = 'en';
  }

  async sendOtp(indianPhoneNumber: string, recaptchaContainerId: string): Promise<void> {
    this.ensureConfigured();
    this.resetSession();

    const verifier = await this.createRecaptchaVerifier(recaptchaContainerId);

    try {
      this.confirmationResult = await signInWithPhoneNumber(
        this.auth,
        this.toIndianE164(indianPhoneNumber),
        verifier,
      );
    } catch (error) {
      this.clearRecaptcha();
      throw this.toFriendlyError(error);
    }
  }

  async verifyOtp(otp: string): Promise<FirebasePhoneVerificationResult> {
    if (!this.confirmationResult) {
      throw new Error('Request a phone OTP first.');
    }

    try {
      const credential: UserCredential = await this.confirmationResult.confirm(otp.trim());
      return {
        phoneNumber: credential.user.phoneNumber,
        providerUid: credential.user.uid,
        idToken: await credential.user.getIdToken(),
      };
    } catch (error) {
      throw this.toFriendlyError(error);
    }
  }

  resetSession(): void {
    this.confirmationResult = null;
    this.clearRecaptcha();
  }

  private async createRecaptchaVerifier(containerId: string): Promise<RecaptchaVerifier> {
    this.clearRecaptcha();
    this.recaptchaContainerId = containerId;

    const container = document.getElementById(containerId);
    if (!container) {
      throw new Error('Phone verification container is missing from the page.');
    }

    container.innerHTML = '';
    this.recaptchaVerifier = new RecaptchaVerifier(this.auth, containerId, {
      size: 'invisible',
      'expired-callback': () => {
        this.clearRecaptcha();
      },
    });

    await this.recaptchaVerifier.render();
    return this.recaptchaVerifier;
  }

  private clearRecaptcha(): void {
    if (this.recaptchaVerifier) {
      this.recaptchaVerifier.clear();
      this.recaptchaVerifier = null;
    }

    if (this.recaptchaContainerId) {
      const container = document.getElementById(this.recaptchaContainerId);
      if (container) {
        container.innerHTML = '';
      }
    }
  }

  private ensureConfigured(): void {
    if (!isFirebasePhoneAuthConfigured()) {
      throw new Error('Firebase phone auth is not configured. Update firebase.config.ts with your Firebase project keys.');
    }
  }

  private toIndianE164(phoneNumber: string): string {
    const digits = phoneNumber.replace(/\D/g, '');

    if (!/^[6-9][0-9]{9}$/.test(digits)) {
      throw new Error('Enter a valid 10 digit Indian mobile number.');
    }

    return `+91${digits}`;
  }

  private toFriendlyError(error: unknown): Error {
    const code = typeof error === 'object' && error !== null && 'code' in error
      ? String((error as { code?: string }).code)
      : '';
    const message = typeof error === 'object' && error !== null && 'message' in error
      ? String((error as { message?: string }).message)
      : '';

    console.error('Firebase phone auth error', { code, message, error });

    switch (code) {
      case 'auth/operation-not-allowed':
        return new Error('Phone sign-in is not enabled in Firebase Authentication for this project.');
      case 'auth/unauthorized-domain':
        return new Error('This domain is not authorized in Firebase. Add localhost in Firebase Authentication authorized domains.');
      case 'auth/invalid-app-credential':
      case 'auth/missing-app-credential':
        return new Error('Firebase could not validate the app verifier. Refresh the page and try again. If it continues, check Firebase phone auth and authorized domains.');
      case 'auth/app-not-authorized':
        return new Error('This Firebase app is not authorized for Authentication. Check the Firebase web app configuration.');
      case 'auth/argument-error':
        return new Error('Firebase phone verification was called with invalid parameters.');
      case 'auth/invalid-phone-number':
        return new Error('Enter a valid phone number for Firebase verification.');
      case 'auth/too-many-requests':
        return new Error('Too many OTP requests were sent for this number. Try again later.');
      case 'auth/invalid-verification-code':
        return new Error('The phone OTP is incorrect.');
      case 'auth/code-expired':
        return new Error('The phone OTP has expired. Request a new one.');
      case 'auth/captcha-check-failed':
        return new Error('reCAPTCHA verification failed. Please try again.');
      default:
        if (code || message) {
          return new Error(`Firebase phone verification failed${code ? ` (${code})` : ''}${message ? `: ${message}` : '.'}`);
        }

        return new Error('Firebase phone verification could not be completed right now.');
    }
  }
}