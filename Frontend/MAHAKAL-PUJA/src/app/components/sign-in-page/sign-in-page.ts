import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AUTH_FLOW_TEXT } from '../../shared/constants/Authentication-flow.constants';
import { AuthService } from '../../shared/services/auth';
import { firstValueFrom } from 'rxjs';
import { SecureQueryStateService } from '../../shared/services/secure-query-state';
import { AuthSessionService } from '../../shared/services/auth-session';
import { AdminAuthFlowService } from '../../shared/services/admin-auth-flow';

@Component({
  standalone: true,
  selector: 'app-sign-in-page',
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './sign-in-page.html',
  styleUrl: './sign-in-page.scss',
})
export class SignInPage implements OnInit {
  signInForm!: FormGroup;
  submitted = false;
  passwordVisible = false;
  isSubmitting = false;
  backendError = '';
  fieldBackendErrors: Record<string, string> = {};
  readonly text = AUTH_FLOW_TEXT.signIn;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private secureQueryStateService: SecureQueryStateService,
    private authSessionService: AuthSessionService,
    private adminAuthFlowService: AdminAuthFlowService,
  ) {}

  ngOnInit() {
    this.signInForm = this.fb.group({
      contact: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern(/\S+/)]],
    });
  }

  get f() {
    return this.signInForm.controls;
  }

  togglePasswordVisibility() {
    this.passwordVisible = !this.passwordVisible;
  }

  async onSubmit() {
    this.submitted = true;
    this.backendError = '';
    this.fieldBackendErrors = {};

    if (this.signInForm.invalid) {
      this.signInForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const contact = this.signInForm.value.contact as string;
    const password = this.signInForm.value.password as string;

    try {
      const response = await firstValueFrom(this.authService.signin(contact, password));

      if (response.token) {
        this.authSessionService.save({
          token: response.token,
          refreshToken: response.refreshToken,
          email: response.email,
          role: response.role,
          expiresInMinutes: response.expiresInMinutes,
          refreshExpiresInDays: response.refreshExpiresInDays,
        });
      }

      if (response.role === 'ADMIN' || response.role === 'SUPER_ADMIN') {
        await this.router.navigate(['/admin/dashboard']);
      } else if (response.role === 'SELLER') {
        await this.router.navigate(['/seller-dashboard']);
      } else {
        await this.router.navigate(['/home']);
      }
    } catch (error) {
      const code = this.authService.getErrorCode(error);
      if (code === 'ADMIN_2FA_REQUIRED') {
        try {
          const challengeResponse = await firstValueFrom(
            this.authService.startAdminLoginChallenge(contact, password),
          );

          this.adminAuthFlowService.save({
            contact,
            challengeToken: challengeResponse.challengeToken,
            createdAt: Date.now(),
          });

          await this.router.navigate(['/admin-otp']);
          return;
        } catch (challengeError) {
          this.backendError = this.authService.getFriendlyErrorMessage(
            challengeError,
            'Admin challenge could not be started. Please try again.',
          );
          return;
        }
      }

      this.fieldBackendErrors = this.authService.getFieldErrors(error);
      this.backendError = this.authService.getFriendlyErrorMessage(error, this.text.requestFailedMessage);
    } finally {
      this.isSubmitting = false;
    }
  }

  async onForgotPassword() {
    if (this.isSubmitting) {
      return;
    }

    this.backendError = '';
    this.fieldBackendErrors = {};
    const contactControl = this.signInForm.get('contact');
    contactControl?.markAsTouched();

    if (!contactControl || contactControl.invalid) {
      this.backendError = this.text.forgotPasswordPromptMessage;
      return;
    }

    this.isSubmitting = true;
    const email = contactControl.value as string;

    try {
      await firstValueFrom(
        this.authService.resendOtp({
          contact: email,
        }),
      );

      const [encryptedEmail, encryptedFlowToken] = await Promise.all([
        this.secureQueryStateService.encryptForUrl(email),
        this.secureQueryStateService.encryptForUrl('forgot-password-flow'),
      ]);

      await this.router.navigate(['/otp'], {
        queryParams: {
          email: encryptedEmail,
          password: encryptedFlowToken,
        },
      });
    } catch (error) {
      this.fieldBackendErrors = this.authService.getFieldErrors(error);
      this.backendError = this.authService.getFriendlyErrorMessage(
        error,
        this.text.forgotPasswordFailedMessage,
      );
    } finally {
      this.isSubmitting = false;
    }
  }

}
