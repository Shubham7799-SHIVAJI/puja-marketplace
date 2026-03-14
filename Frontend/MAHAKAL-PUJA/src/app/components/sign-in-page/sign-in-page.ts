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
      await firstValueFrom(this.authService.signin(contact, password));
      await this.router.navigate(['/home']);
    } catch (error) {
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
