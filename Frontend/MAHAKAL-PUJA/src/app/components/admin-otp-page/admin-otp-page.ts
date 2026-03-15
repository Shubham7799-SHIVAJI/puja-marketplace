import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { AuthService } from '../../shared/services/auth';
import { AuthSessionService } from '../../shared/services/auth-session';
import { AdminAuthFlowService } from '../../shared/services/admin-auth-flow';

@Component({
  selector: 'app-admin-otp-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-otp-page.html',
  styleUrl: './admin-otp-page.scss',
})
export class AdminOtpPage implements OnInit {
  readonly form: ReturnType<FormBuilder['group']>;

  loading = false;
  backendError = '';
  contact = '';

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly authSessionService: AuthSessionService,
    private readonly adminAuthFlowService: AdminAuthFlowService,
  ) {
    this.form = this.formBuilder.group({
      otp: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
    });
  }

  ngOnInit(): void {
    const challenge = this.adminAuthFlowService.get();
    if (!challenge) {
      this.router.navigate(['/signin']);
      return;
    }
    this.contact = challenge.contact;
  }

  async submit(): Promise<void> {
    this.backendError = '';
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const challenge = this.adminAuthFlowService.get();
    if (!challenge) {
      this.backendError = 'Admin challenge expired. Please sign in again.';
      await this.router.navigate(['/signin']);
      return;
    }

    this.loading = true;
    try {
      const response = await firstValueFrom(
        this.authService.verifyAdminOtp(challenge.challengeToken, this.form.controls['otp'].value ?? ''),
      );

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

      this.adminAuthFlowService.clear();
      await this.router.navigate(['/admin/dashboard']);
    } catch (error) {
      this.backendError = this.authService.getFriendlyErrorMessage(error, 'Admin OTP verification failed.');
    } finally {
      this.loading = false;
    }
  }
}
