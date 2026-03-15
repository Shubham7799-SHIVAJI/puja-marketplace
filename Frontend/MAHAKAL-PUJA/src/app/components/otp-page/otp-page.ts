import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../shared/services/auth';
import { OtpSecurityContextService } from '../../shared/services/otp-security-context';
import { SecureQueryStateService } from '../../shared/services/secure-query-state';
import { AUTH_FLOW_TEXT } from '../../shared/constants/Authentication-flow.constants';

@Component({
  standalone: true,
  selector: 'app-otp-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './otp-page.html',
  styleUrl: './otp-page.scss',
})
export class OtpPage implements OnInit, AfterViewInit, OnDestroy {
  @ViewChildren('otpInput') otpInputs!: QueryList<ElementRef<HTMLInputElement>>;

  otpForm: FormGroup;
  readonly otpIndexes = Array.from({ length: 6 }, (_, index) => index);
  contactValue = '';
  displayName = '';
  contactTypeLabel = 'contact';
  maskedContact = '';
  submitted = false;
  readonly text = AUTH_FLOW_TEXT.otp;
  remainingSeconds = 120;
  canResendOtp = false;
  resendStatusMessage = '';
  backendError = '';
  verifySuccessMessage = '';
  verifyInProgress = false;
  resendInProgress = false;
  private timerId: number | null = null;
  private flowToken = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private otpSecurityContextService: OtpSecurityContextService,
    private secureQueryStateService: SecureQueryStateService,
  ) {
    this.otpForm = this.fb.group({
      otp0: this.createOtpControl(),
      otp1: this.createOtpControl(),
      otp2: this.createOtpControl(),
      otp3: this.createOtpControl(),
      otp4: this.createOtpControl(),
      otp5: this.createOtpControl(),
    });
  }

  ngOnInit() {
    void this.initializeOtpPage();
  }

  ngOnDestroy() {
    this.clearTimer();
  }

  get formattedTimer() {
    const minutes = Math.floor(this.remainingSeconds / 60)
      .toString()
      .padStart(2, '0');
    const seconds = (this.remainingSeconds % 60).toString().padStart(2, '0');
    return `${minutes}:${seconds}`;
  }

  ngAfterViewInit() {
    queueMicrotask(() => this.focusInput(0));
  }

  get otpControls() {
    return this.otpIndexes.map((index) => this.otpForm.get(`otp${index}`) as FormControl);
  }

  onDigitInput(event: Event, index: number) {
    const input = event.target as HTMLInputElement;
    const sanitizedValue = input.value.replace(/\D/g, '').slice(-1);

    this.otpControls[index].setValue(sanitizedValue);
    input.value = sanitizedValue;

    if (sanitizedValue && index < this.otpIndexes.length - 1) {
      this.focusInput(index + 1);
    }
  }

  onKeyDown(event: KeyboardEvent, index: number) {
    const input = event.target as HTMLInputElement;

    if (event.key === 'Backspace' && !input.value && index > 0) {
      this.focusInput(index - 1);
      return;
    }

    if (event.key === 'ArrowLeft' && index > 0) {
      event.preventDefault();
      this.focusInput(index - 1);
      return;
    }

    if (event.key === 'ArrowRight' && index < this.otpIndexes.length - 1) {
      event.preventDefault();
      this.focusInput(index + 1);
    }
  }

  onPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pastedValue = event.clipboardData?.getData('text') ?? '';
    const digits = pastedValue.replace(/\D/g, '').slice(0, this.otpIndexes.length).split('');

    this.otpIndexes.forEach((index) => {
      this.otpControls[index].setValue(digits[index] ?? '');
    });

    const focusIndex = Math.min(digits.length, this.otpIndexes.length - 1);
    this.focusInput(focusIndex);
  }

  async onSubmit() {
    this.submitted = true;
    this.backendError = '';
    this.verifySuccessMessage = '';

    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      const firstEmptyIndex = this.otpControls.findIndex((control) => !control.value);
      this.focusInput(firstEmptyIndex === -1 ? 0 : firstEmptyIndex);
      return;
    }

    this.verifyInProgress = true;
    const otp = this.otpControls.map((control) => control.value).join('');

    try {
      const response = await firstValueFrom(
        this.authService.verifyOtp({
          contact: this.contactValue,
          otp,
        }),
      );

      if (!response.resetToken) {
        throw new Error('Reset token missing from verification response.');
      }

      this.otpSecurityContextService.setVerifiedContext(this.flowToken, this.contactValue, response.resetToken);
      const encryptedEmail = await this.secureQueryStateService.encryptForUrl(this.contactValue);
      await this.router.navigate(['/set-password'], {
        queryParams: {
          email: encryptedEmail,
        },
      });
    } catch (error) {
      this.backendError = this.authService.getFriendlyErrorMessage(error, this.text.verifyFailedMessage);
    } finally {
      this.verifyInProgress = false;
    }
  }

  async onResendOtp() {
    if (!this.canResendOtp || this.resendInProgress) {
      return;
    }

    this.resendInProgress = true;
    this.backendError = '';
    this.verifySuccessMessage = '';

    try {
      await firstValueFrom(
        this.authService.resendOtp({
          contact: this.contactValue,
        }),
      );

      this.otpForm.reset();
      this.submitted = false;
      this.resendStatusMessage = `${this.text.resendSuccessPrefix} ${this.maskedContact}.`;
      this.startCountdown();
      this.focusInput(0);
    } catch (error) {
      this.backendError = this.authService.getFriendlyErrorMessage(error, this.text.resendFailedMessage);
    } finally {
      this.resendInProgress = false;
    }
  }

  trackByIndex(index: number) {
    return index;
  }

  private createOtpControl() {
    return this.fb.control('', [Validators.required, Validators.pattern(/^\d$/)]);
  }

  private async initializeOtpPage() {
    const encryptedContact = this.route.snapshot.queryParamMap.get('email') ?? '';
    const encryptedPassword = this.route.snapshot.queryParamMap.get('password') ?? '';

    if (!encryptedContact || !encryptedPassword) {
      await this.router.navigate(['/signin']);
      return;
    }

    const [contact, password] = await Promise.all([
      this.secureQueryStateService.decryptFromUrl(encryptedContact),
      this.secureQueryStateService.decryptFromUrl(encryptedPassword),
    ]);

    if (!contact || !password) {
      await this.router.navigate(['/signin']);
      return;
    }

    this.contactValue = contact;
    this.displayName = this.route.snapshot.queryParamMap.get('name') ?? '';
  this.flowToken = password;
    const isEmail = this.isEmail(this.contactValue);
    this.contactTypeLabel = isEmail ? 'email address' : 'phone number';
    this.maskedContact = this.maskContact(this.contactValue, isEmail);
    this.startCountdown();
  }

  private startCountdown() {
    this.clearTimer();
    this.remainingSeconds = 120;
    this.canResendOtp = false;

    this.timerId = window.setInterval(() => {
      if (this.remainingSeconds > 0) {
        this.remainingSeconds -= 1;
      }

      if (this.remainingSeconds === 0) {
        this.canResendOtp = true;
        this.clearTimer();
      }
    }, 1000);
  }

  private clearTimer() {
    if (this.timerId !== null) {
      window.clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private focusInput(index: number) {
    const input = this.otpInputs?.get(index)?.nativeElement;
    input?.focus();
    input?.select();
  }

  private isEmail(value: string) {
    return /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(value);
  }

  private maskContact(value: string, isEmail: boolean) {
    if (!value) {
      return isEmail ? 'your email address' : 'your phone number';
    }

    if (isEmail) {
      const [localPart, domain] = value.split('@');
      if (!localPart || !domain) {
        return value;
      }

      const visibleLocalPart = `${localPart.slice(0, 2)}${'*'.repeat(Math.max(localPart.length - 2, 2))}`;
      return `${visibleLocalPart}@${domain}`;
    }

    const lastFourDigits = value.slice(-4);
    return `${'*'.repeat(Math.max(value.length - 4, 4))}${lastFourDigits}`;
  }

}
