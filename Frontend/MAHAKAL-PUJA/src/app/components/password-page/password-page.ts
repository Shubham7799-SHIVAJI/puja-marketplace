import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../shared/services/auth';
import { OtpSecurityContextService } from '../../shared/services/otp-security-context';
import { SecureQueryStateService } from '../../shared/services/secure-query-state';
import { AUTH_FLOW_TEXT } from '../../shared/constants/Authentication-flow.constants';

@Component({
  standalone: true,
  selector: 'app-password-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './password-page.html',
  styleUrl: './password-page.scss',
})
export class PasswordPage implements OnInit {
  passwordForm!: FormGroup;
  submitted = false;
  passwordVisible = false;
  confirmPasswordVisible = false;
  isSubmitting = false;
  backendError = '';
  fieldBackendErrors: Record<string, string> = {};
  readonly text = AUTH_FLOW_TEXT.setPassword;
  private readonly passwordStrengthPattern = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private otpSecurityContextService: OtpSecurityContextService,
    private secureQueryStateService: SecureQueryStateService,
  ) {}

  ngOnInit() {
    this.passwordForm = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.pattern(this.passwordStrengthPattern)]],
        confirmPassword: ['', [Validators.required, Validators.pattern(this.passwordStrengthPattern)]],
      },
      { validators: this.passwordsMatchValidator() },
    );

    void this.prefillEmailFromQuery();
  }

  get f() {
    return this.passwordForm.controls;
  }

  togglePasswordVisibility() {
    this.passwordVisible = !this.passwordVisible;
  }

  toggleConfirmPasswordVisibility() {
    this.confirmPasswordVisible = !this.confirmPasswordVisible;
  }

  async onSubmit() {
    this.submitted = true;
    this.backendError = '';
    this.fieldBackendErrors = {};

    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    const email = this.passwordForm.value.email as string;
    const password = this.passwordForm.value.password as string;
    const confirmPassword = this.passwordForm.value.confirmPassword as string;

    const context = this.otpSecurityContextService.getContext();
    if (!this.otpSecurityContextService.isContextValid() || !context || context.email !== email) {
      this.backendError = 'Your verification session expired. Please verify OTP again.';
      this.otpSecurityContextService.clearContext();
      this.isSubmitting = false;
      await this.router.navigate(['/signin']);
      return;
    }

    if (!context.resetToken) {
      this.backendError = 'Password reset session is missing. Please verify OTP again.';
      this.otpSecurityContextService.clearContext();
      this.isSubmitting = false;
      await this.router.navigate(['/signin']);
      return;
    }

    try {
      await firstValueFrom(this.authService.setPassword(email, password, confirmPassword, context.resetToken));
      this.otpSecurityContextService.clearContext();
      await this.router.navigate(['/home']);
    } catch (error) {
      this.fieldBackendErrors = this.authService.getFieldErrors(error);
      this.backendError = this.authService.getFriendlyErrorMessage(error, this.text.requestFailedMessage);
    } finally {
      this.isSubmitting = false;
    }
  }

  private passwordsMatchValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const password = group.get('password')?.value ?? '';
      const confirmPassword = group.get('confirmPassword')?.value ?? '';

      if (!password || !confirmPassword) {
        return null;
      }

      return password === confirmPassword ? null : { passwordMismatch: true };
    };
  }

  private async prefillEmailFromQuery() {
    if (!this.otpSecurityContextService.isContextValid()) {
      this.otpSecurityContextService.clearContext();
      await this.router.navigate(['/signin']);
      return;
    }

    const encryptedEmail = this.route.snapshot.queryParamMap.get('email') ?? '';
    if (!encryptedEmail) {
      await this.router.navigate(['/signin']);
      return;
    }

    const email = await this.secureQueryStateService.decryptFromUrl(encryptedEmail);
    const context = this.otpSecurityContextService.getContext();
    if (!email || !context || context.email !== email) {
      this.otpSecurityContextService.clearContext();
      await this.router.navigate(['/signin']);
      return;
    }

    this.passwordForm.patchValue({ email });
  }
}
