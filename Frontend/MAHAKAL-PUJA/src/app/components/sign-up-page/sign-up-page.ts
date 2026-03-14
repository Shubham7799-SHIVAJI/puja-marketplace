import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../shared/services/auth';
import { CommonModule } from '@angular/common';
import { SecureQueryStateService } from '../../shared/services/secure-query-state';
import { AUTH_FLOW_TEXT } from '../../shared/constants/Authentication-flow.constants';
import { firstValueFrom } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-sign-up-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CommonModule,
  ],
  templateUrl: './sign-up-page.html',
  styleUrl: './sign-up-page.scss',
})
export class SignUpPage implements OnInit {
  loginForm!: FormGroup;
  submitted = false;
  isSubmitting = false;
  backendError = '';
  fieldBackendErrors: Record<string, string> = {};
  readonly text = AUTH_FLOW_TEXT.signUp;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private secureQueryStateService: SecureQueryStateService,
  ) {}

  ngOnInit() {
    this.loginForm = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(/\S+/)]],
      contact: ['', [Validators.required, Validators.email]],
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  async onSubmit() {
    this.submitted = true;
    this.backendError = '';
    this.fieldBackendErrors = {};

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.loginForm.value;
    const payload = {
      name: formValue.name,
      contact: formValue.contact,
    };

    try {
      await firstValueFrom(this.authService.login(payload));

      const [encryptedContact, encryptedFlowToken] = await Promise.all([
        this.secureQueryStateService.encryptForUrl(payload.contact),
        this.secureQueryStateService.encryptForUrl('login-flow'),
      ]);

      await this.router.navigate(['/otp'], {
        queryParams: {
          email: encryptedContact,
          password: encryptedFlowToken,
          name: payload.name,
        },
      });
    } catch (error) {
      this.fieldBackendErrors = this.authService.getFieldErrors(error);
      this.backendError = this.authService.getFriendlyErrorMessage(
        error,
        this.text.requestFailedMessage,
      );
    } finally {
      this.isSubmitting = false;
    }
  }
}
