import { CommonModule } from '@angular/common';
import { HttpEventType } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, finalize, firstValueFrom } from 'rxjs';

import { FileUploadField } from '../../shared/components/file-upload-field/file-upload-field';
import {
  ShopFileUploadResponse,
  ShopOtpResponse,
  ShopRegistrationPayload,
  ShopRegistrationResponse,
  ShopRegistrationService,
} from '../../shared/services/shop-registration';

interface StepDefinition {
  label: string;
  caption: string;
  groupName: string;
  description: string;
}

interface UploadState {
  fileName: string;
  previewUrl: string | null;
  progress: number | null;
  isUploading: boolean;
  uploaded: boolean;
  error: string;
}

type UploadFieldName =
  | 'profilePhoto'
  | 'ownerAadharPhoto'
  | 'ownerPanPhoto'
  | 'ownerSelfieWithId'
  | 'gstCertificateUpload'
  | 'cancelledChequePhoto';

type UploadStateMap = {
  [key in UploadFieldName]: UploadState;
};

type UploadRule = {
  accept: string;
  allowedTypes: string[];
  errorMessage: string;
};

type OtpChannel = 'EMAIL' | 'PHONE';

interface OtpActionState {
  sending: boolean;
  verifying: boolean;
  verified: boolean;
  message: string;
  previewOtp: string;
  error: string;
}

const PHONE_PATTERN = /^[6-9][0-9]{9}$/;
const OTP_PATTERN = /^[0-9]{6}$/;
const AADHAR_PATTERN = /^[0-9]{12}$/;
const PAN_PATTERN = /^[A-Z]{5}[0-9]{4}[A-Z]$/;
const GST_PATTERN = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][A-Z0-9]{3}$/;
const IFSC_PATTERN = /^[A-Z]{4}0[A-Z0-9]{6}$/;
const UPI_PATTERN = /^[A-Za-z0-9.\-_]{2,256}@[A-Za-z]{2,64}$/;
const DRAFT_STORAGE_KEY = 'shop-registration-draft-id';
const IMAGE_UPLOAD_RULE: UploadRule = {
  accept: 'image/jpeg,image/png,image/webp',
  allowedTypes: ['image/jpeg', 'image/png', 'image/webp'],
  errorMessage: 'Only JPG, PNG, and WEBP images are allowed.',
};
const DOCUMENT_UPLOAD_RULE: UploadRule = {
  accept: '.pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  allowedTypes: ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
  errorMessage: 'Only PDF and DOCX files are allowed.',
};
const UPLOAD_RULES: Record<UploadFieldName, UploadRule> = {
  profilePhoto: IMAGE_UPLOAD_RULE,
  ownerAadharPhoto: IMAGE_UPLOAD_RULE,
  ownerPanPhoto: IMAGE_UPLOAD_RULE,
  ownerSelfieWithId: IMAGE_UPLOAD_RULE,
  gstCertificateUpload: DOCUMENT_UPLOAD_RULE,
  cancelledChequePhoto: IMAGE_UPLOAD_RULE,
};

function noWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = `${control.value ?? ''}`.trim();
    return value ? null : { whitespace: true };
  };
}

function matchingFieldsValidator(sourceField: string, targetField: string, errorKey: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const source = control.get(sourceField)?.value;
    const target = control.get(targetField)?.value;

    if (!source || !target) {
      return null;
    }

    return source === target ? null : { [errorKey]: true };
  };
}

function gstCertificateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const gstNumber = `${control.get('gstNumber')?.value ?? ''}`.trim();
    const gstCertificateUpload = `${control.get('gstCertificateUpload')?.value ?? ''}`.trim();

    if (!gstNumber) {
      return null;
    }

    return gstCertificateUpload ? null : { gstCertificateRequired: true };
  };
}

function eitherOtpVerifiedValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const emailOtpVerified = !!control.get('emailOtpVerified')?.value;
    const phoneOtpVerified = !!control.get('phoneOtpVerified')?.value;

    return emailOtpVerified || phoneOtpVerified ? null : { otpVerificationRequired: true };
  };
}

@Component({
  standalone: true,
  selector: 'app-shop-registration',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, FileUploadField],
  templateUrl: './shop-registration.html',
  styleUrl: './shop-registration.scss',
})
export class ShopRegistration implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly shopRegistrationService = inject(ShopRegistrationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly steps: StepDefinition[] = [
    {
      label: 'Account Details',
      caption: 'Step 1',
      groupName: 'accountDetails',
      description: 'Owner identity, login credentials, OTP references, and profile photo.',
    },
    {
      label: 'Shop Information',
      caption: 'Step 2',
      groupName: 'shopInformation',
      description: 'Primary branding, category, and business description.',
    },
    {
      label: 'Address',
      caption: 'Step 3',
      groupName: 'address',
      description: 'Precise location and geographical coordinates for discoverability.',
    },
    {
      label: 'Contact Details',
      caption: 'Step 4',
      groupName: 'contactDetails',
      description: 'Customer-facing email, phone, and WhatsApp channels.',
    },
    {
      label: 'KYC Verification',
      caption: 'Step 5',
      groupName: 'kycVerification',
      description: 'Identity proof, tax details, and statutory verification uploads.',
    },
    {
      label: 'Bank Details',
      caption: 'Step 6',
      groupName: 'bankDetails',
      description: 'Settlement information for payouts and reconciliation.',
    },
    {
      label: 'Policies & Consent',
      caption: 'Step 8',
      groupName: 'policies',
      description: 'Final declarations, policy acceptance, and application submission.',
    },
  ];

  readonly shopCategories = [
    'Puja Samagri',
    'Temple Decor',
    'Flowers & Garlands',
    'Ayurveda & Wellness',
    'Jewellery',
    'Handicrafts',
    'Groceries',
    'Books & Spiritual Media',
    'Food & Sweets',
    'Clothing & Textiles',
  ];

  readonly indianStates = [
    'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Goa', 'Gujarat',
    'Haryana', 'Himachal Pradesh', 'Jharkhand', 'Karnataka', 'Kerala', 'Madhya Pradesh',
    'Maharashtra', 'Manipur', 'Meghalaya', 'Mizoram', 'Nagaland', 'Odisha', 'Punjab',
    'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura', 'Uttar Pradesh',
    'Uttarakhand', 'West Bengal', 'Delhi', 'Jammu and Kashmir', 'Ladakh', 'Puducherry',
  ];

  readonly form = this.fb.group({
    accountDetails: this.fb.group(
      {
        ownerFullName: ['', [Validators.required, noWhitespaceValidator()]],
        email: ['', [Validators.required, Validators.email]],
        phoneNumber: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]],
        emailOtp: ['', [Validators.pattern(OTP_PATTERN)]],
        emailOtpVerified: [false],
        phoneOtp: ['', [Validators.pattern(OTP_PATTERN)]],
        phoneOtpVerified: [false],
        profilePhoto: ['', [Validators.required]],
      },
      { validators: [matchingFieldsValidator('password', 'confirmPassword', 'passwordMismatch'), eitherOtpVerifiedValidator()] },
    ),
    shopInformation: this.fb.group({
      shopName: ['', [Validators.required, noWhitespaceValidator()]],
      shopUniqueId: [{ value: '', disabled: true }],
      shopCategory: ['', [Validators.required]],
      shopDescription: ['', [Validators.required, Validators.minLength(20)]],
    }),
    address: this.fb.group({
      addressLine1: ['', [Validators.required, noWhitespaceValidator()]],
      addressLine2: [''],
      city: ['', [Validators.required, noWhitespaceValidator()]],
      state: ['', [Validators.required]],
      pincode: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
      country: ['India', [Validators.required]],
      landmark: [''],
      latitude: [null as number | null],
      longitude: [null as number | null],
    }),
    contactDetails: this.fb.group({
      shopPhoneNumber: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
      shopEmail: ['', [Validators.required, Validators.email]],
      whatsappNumber: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    }),
    kycVerification: this.fb.group(
      {
        ownerAadharNumber: ['', [Validators.required, Validators.pattern(AADHAR_PATTERN)]],
        ownerPanNumber: ['', [Validators.required, Validators.pattern(PAN_PATTERN)]],
        ownerAadharPhoto: ['', [Validators.required]],
        ownerPanPhoto: ['', [Validators.required]],
        ownerSelfieWithId: ['', [Validators.required]],
        gstNumber: ['', [Validators.pattern(GST_PATTERN)]],
        gstCertificateUpload: [''],
        businessRegistrationNumber: ['', [Validators.required, noWhitespaceValidator()]],
      },
      { validators: gstCertificateValidator() },
    ),
    bankDetails: this.fb.group(
      {
        accountHolderName: ['', [Validators.required, noWhitespaceValidator()]],
        bankName: ['', [Validators.required, noWhitespaceValidator()]],
        accountNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{9,18}$/)]],
        confirmAccountNumber: ['', [Validators.required]],
        ifscCode: ['', [Validators.required, Validators.pattern(IFSC_PATTERN)]],
        upiId: ['', [Validators.pattern(UPI_PATTERN)]],
        cancelledChequePhoto: ['', [Validators.required]],
      },
      { validators: matchingFieldsValidator('accountNumber', 'confirmAccountNumber', 'accountMismatch') },
    ),
    policies: this.fb.group({
      acceptTermsAndConditions: [false, [Validators.requiredTrue]],
      acceptPrivacyPolicy: [false, [Validators.requiredTrue]],
      acceptCommissionPolicy: [false, [Validators.requiredTrue]],
    }),
  });

  currentStepIndex = 0;
  registrationId: string | null = null;
  saveState: 'idle' | 'saving' | 'saved' | 'error' = 'idle';
  submitted = false;
  isSubmitting = false;
  backendError = '';
  lastSavedAtLabel = '';
  fieldBackendErrors: Record<string, string> = {};
  restoringDraft = false;

  uploadStates: UploadStateMap = {
    profilePhoto: this.createUploadState(),
    ownerAadharPhoto: this.createUploadState(),
    ownerPanPhoto: this.createUploadState(),
    ownerSelfieWithId: this.createUploadState(),
    gstCertificateUpload: this.createUploadState(),
    cancelledChequePhoto: this.createUploadState(),
  };

  otpStates: Record<OtpChannel, OtpActionState> = {
    EMAIL: this.createOtpState(),
    PHONE: this.createOtpState(),
  };

  ngOnInit(): void {
    const storedDraftId = localStorage.getItem(DRAFT_STORAGE_KEY);
    if (storedDraftId) {
      this.loadDraft(storedDraftId);
    }

    this.form.valueChanges
      .pipe(debounceTime(2000), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (this.restoringDraft || this.isSubmitting || !this.hasMeaningfulData()) {
          return;
        }

        void this.saveDraft(true);
      });

    this.control('accountDetails.email')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.resetOtpChannel('EMAIL'));

    this.control('accountDetails.phoneNumber')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.resetOtpChannel('PHONE'));
  }

  get currentStep(): StepDefinition {
    return this.steps[this.currentStepIndex];
  }

  get completionPercent(): number {
    return Math.round(((this.currentStepIndex + 1) / this.steps.length) * 100);
  }

  async goNext(): Promise<void> {
    this.submitted = true;
    this.backendError = '';
    this.fieldBackendErrors = {};
    const currentGroup = this.getStepGroup(this.currentStep.groupName);

    if (currentGroup.invalid) {
      currentGroup.markAllAsTouched();
      return;
    }

    await this.saveDraft(false);

    if (this.currentStepIndex < this.steps.length - 1) {
      this.currentStepIndex += 1;
      this.updateSaveTimestampLabel();
    }
  }

  goBack(): void {
    if (this.currentStepIndex > 0) {
      this.currentStepIndex -= 1;
    }
  }

  jumpToStep(index: number): void {
    if (index === this.currentStepIndex) {
      return;
    }

    if (index < this.currentStepIndex || this.isStepComplete(index)) {
      this.currentStepIndex = index;
    }
  }

  async saveDraftManually(): Promise<void> {
    await this.saveDraft(false);
  }

  async sendOtp(channel: OtpChannel): Promise<void> {
    const contactPath = channel === 'EMAIL' ? 'accountDetails.email' : 'accountDetails.phoneNumber';
    const contactControl = this.control(contactPath);
    contactControl?.markAsTouched();

    if (!contactControl || contactControl.invalid) {
      return;
    }

    await this.ensureDraftExists();
    const otpState = this.otpStates[channel];
    otpState.sending = true;
    otpState.error = '';
    otpState.message = '';
    otpState.previewOtp = '';

    try {
      const response = await firstValueFrom(this.shopRegistrationService.sendOtp({
        registrationId: this.registrationId,
        channel,
        contact: `${contactControl.value ?? ''}`,
        ownerFullName: `${this.control('accountDetails.ownerFullName')?.value ?? ''}`,
      }));

      this.registrationId = response.registrationId;
      localStorage.setItem(DRAFT_STORAGE_KEY, response.registrationId);
      otpState.message = response.message;
      otpState.previewOtp = response.previewOtp ?? '';
      otpState.verified = false;
      this.setOtpVerified(channel, false);
    } catch (error) {
      otpState.error = this.shopRegistrationService.getFriendlyErrorMessage(error, 'Unable to send OTP right now.');
    } finally {
      otpState.sending = false;
    }
  }

  async verifyOtp(channel: OtpChannel): Promise<void> {
    const contactPath = channel === 'EMAIL' ? 'accountDetails.email' : 'accountDetails.phoneNumber';
    const otpPath = channel === 'EMAIL' ? 'accountDetails.emailOtp' : 'accountDetails.phoneOtp';
    const contactControl = this.control(contactPath);
    const otpControl = this.control(otpPath);
    contactControl?.markAsTouched();
    otpControl?.markAsTouched();

    if (!contactControl || !otpControl || contactControl.invalid || otpControl.invalid) {
      return;
    }

    await this.ensureDraftExists();
    const otpState = this.otpStates[channel];
    otpState.verifying = true;
    otpState.error = '';

    try {
      const response = await firstValueFrom(this.shopRegistrationService.verifyOtp({
        registrationId: this.registrationId,
        channel,
        contact: `${contactControl.value ?? ''}`,
        otp: `${otpControl.value ?? ''}`,
      }));

      this.registrationId = response.registrationId;
      localStorage.setItem(DRAFT_STORAGE_KEY, response.registrationId);
      otpState.message = response.message;
      otpState.previewOtp = '';
      otpState.verified = true;
      this.setOtpVerified(channel, true);
    } catch (error) {
      otpState.error = this.shopRegistrationService.getFriendlyErrorMessage(error, 'Unable to verify OTP right now.');
      this.setOtpVerified(channel, false);
    } finally {
      otpState.verifying = false;
    }
  }

  otpVerificationMessage(): string {
    if (this.fieldBackendErrors['emailOtp']) {
      return this.fieldBackendErrors['emailOtp'];
    }

    if (this.fieldBackendErrors['phoneOtp']) {
      return this.fieldBackendErrors['phoneOtp'];
    }

    if (this.submitted && this.control('accountDetails')?.hasError('otpVerificationRequired')) {
      return 'Verify either email OTP or phone OTP before continuing.';
    }

    return '';
  }

  async submitApplication(): Promise<void> {
    this.submitted = true;
    this.backendError = '';
    this.fieldBackendErrors = {};

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const firstInvalidIndex = this.steps.findIndex((step) => this.getStepGroup(step.groupName).invalid);
      if (firstInvalidIndex >= 0) {
        this.currentStepIndex = firstInvalidIndex;
      }
      return;
    }

    this.isSubmitting = true;

    try {
      const response = await firstValueFrom(this.shopRegistrationService.submit(this.buildPayload()));
      this.applyServerState(response);
      this.saveState = 'saved';
      this.lastSavedAtLabel = 'Application submitted';
      localStorage.removeItem(DRAFT_STORAGE_KEY);
      this.form.markAsPristine();
      await this.router.navigate(['/shop-dashboard', response.registrationId]);
    } catch (error) {
      this.fieldBackendErrors = this.shopRegistrationService.getFieldErrors(error);
      this.backendError = this.shopRegistrationService.getFriendlyErrorMessage(
        error,
        'Unable to submit the application right now.',
      );
      const firstBackendField = Object.keys(this.fieldBackendErrors)[0];
      const firstInvalidIndex = this.findStepIndexByField(firstBackendField);
      if (firstInvalidIndex >= 0) {
        this.currentStepIndex = firstInvalidIndex;
      }
      this.saveState = 'error';
    } finally {
      this.isSubmitting = false;
    }
  }

  async handleFileSelected(fieldName: UploadFieldName, file: File): Promise<void> {
    const validationMessage = this.validateFile(fieldName, file);
    if (validationMessage) {
      this.setUploadError(fieldName, validationMessage);
      return;
    }

    await this.ensureDraftExists();
    const localPreview = file.type.startsWith('image/') ? URL.createObjectURL(file) : null;
    const uploadState = this.uploadStates[fieldName];
    uploadState.error = '';
    uploadState.fileName = file.name;
    uploadState.previewUrl = localPreview;
    uploadState.progress = 0;
    uploadState.isUploading = true;
    uploadState.uploaded = false;

    await new Promise<void>((resolve, reject) => {
      this.shopRegistrationService
        .uploadFile(file, fieldName, this.registrationId ?? undefined)
        .pipe(
          finalize(() => {
            uploadState.isUploading = false;
          }),
          takeUntilDestroyed(this.destroyRef),
        )
        .subscribe({
          next: (event) => {
            if (event.type === HttpEventType.UploadProgress) {
              uploadState.progress = event.total ? Math.round((event.loaded / event.total) * 100) : 0;
            }

            if (event.type === HttpEventType.Response && event.body) {
              this.applyUploadResponse(fieldName, event.body, file.name, localPreview);
            }
          },
          error: (error) => {
            uploadState.progress = null;
            uploadState.uploaded = false;
            uploadState.error = this.shopRegistrationService.getFriendlyErrorMessage(
              error,
              'Unable to upload the file right now.',
            );
            reject(error);
          },
          complete: () => resolve(),
        });
    });
  }

  clearUploadedFile(fieldName: UploadFieldName): void {
    const uploadState = this.uploadStates[fieldName];
    if (uploadState.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(uploadState.previewUrl);
    }

    this.uploadStates[fieldName] = this.createUploadState();
    this.getFileControl(fieldName).setValue('');
    this.getFileControl(fieldName).markAsTouched();
  }

  isStepComplete(index: number): boolean {
    return this.steps.slice(0, index + 1).every((step) => this.getStepGroup(step.groupName).valid);
  }

  control(path: string): AbstractControl | null {
    return this.form.get(path);
  }

  showError(path: string, backendField = path.split('.').pop() ?? path): boolean {
    const control = this.control(path);
    if (!control) {
      return false;
    }

    return !!this.fieldBackendErrors[backendField] || control.invalid && (control.touched || this.submitted);
  }

  errorMessage(path: string, label: string, backendField = path.split('.').pop() ?? path): string {
    if (this.fieldBackendErrors[backendField]) {
      return this.fieldBackendErrors[backendField];
    }

    if (path.startsWith('accountDetails.') && this.control('accountDetails')?.hasError('passwordMismatch')) {
      return 'Passwords do not match.';
    }

    if (path.startsWith('accountDetails.') && this.control('accountDetails')?.hasError('otpVerificationRequired')) {
      return 'Verify either email OTP or phone OTP before continuing.';
    }

    if (path.startsWith('bankDetails.') && this.control('bankDetails')?.hasError('accountMismatch')) {
      return 'Account numbers do not match.';
    }

    if (
      path.startsWith('kycVerification.')
      && backendField === 'gstCertificateUpload'
      && this.control('kycVerification')?.hasError('gstCertificateRequired')
    ) {
      return 'GST certificate is required when GST number is provided.';
    }

    const control = this.control(path);
    const errors = control?.errors;

    if (!errors) {
      return '';
    }

    if (errors['required'] || errors['requiredTrue']) {
      return `${label} is required.`;
    }

    if (errors['whitespace']) {
      return `${label} cannot be empty.`;
    }

    if (errors['email']) {
      return 'Enter a valid email address.';
    }

    if (errors['pattern']) {
      return `Enter a valid ${label.toLowerCase()}.`;
    }

    if (errors['minlength']) {
      return `${label} must be at least ${errors['minlength'].requiredLength} characters.`;
    }

    if (errors['passwordMismatch']) {
      return 'Passwords do not match.';
    }

    if (errors['accountMismatch']) {
      return 'Account numbers do not match.';
    }

    if (errors['gstCertificateRequired']) {
      return 'GST certificate is required when GST number is provided.';
    }

    return `Please review ${label.toLowerCase()}.`;
  }

  private async saveDraft(isAutoSave: boolean): Promise<void> {
    if (!this.hasMeaningfulData()) {
      return;
    }

    this.saveState = 'saving';

    try {
      const response = await firstValueFrom(this.shopRegistrationService.saveDraft(this.buildPayload()));
      this.applyServerState(response);
      this.saveState = 'saved';
      if (!isAutoSave) {
        this.form.markAsPristine();
      }
    } catch (error) {
      this.saveState = 'error';
      if (!isAutoSave) {
        this.backendError = this.shopRegistrationService.getFriendlyErrorMessage(
          error,
          'Unable to save the shop registration draft right now.',
        );
      }
    }
  }

  private async ensureDraftExists(): Promise<void> {
    if (this.registrationId) {
      return;
    }

    await this.saveDraft(false);
  }

  private loadDraft(registrationId: string): void {
    this.restoringDraft = true;
    this.shopRegistrationService
      .getDraft(registrationId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.patchFormFromResponse(response);
          this.applyServerState(response);
          this.form.markAsPristine();
        },
        error: () => {
          localStorage.removeItem(DRAFT_STORAGE_KEY);
        },
        complete: () => {
          this.restoringDraft = false;
        },
      });
  }

  private patchFormFromResponse(response: ShopRegistrationResponse): void {
    this.form.patchValue({
      accountDetails: {
        ownerFullName: response.ownerFullName ?? '',
        email: response.email ?? '',
        phoneNumber: response.phoneNumber ?? '',
        password: '',
        confirmPassword: '',
        emailOtp: response.emailOtp ?? '',
        emailOtpVerified: response.emailOtpVerified,
        phoneOtp: response.phoneOtp ?? '',
        phoneOtpVerified: response.phoneOtpVerified,
        profilePhoto: response.profilePhoto ?? '',
      },
      shopInformation: {
        shopName: response.shopName ?? '',
        shopUniqueId: response.shopUniqueId ?? '',
        shopCategory: response.shopCategory ?? '',
        shopDescription: response.shopDescription ?? '',
      },
      address: {
        addressLine1: response.addressLine1 ?? '',
        addressLine2: response.addressLine2 ?? '',
        city: response.city ?? '',
        state: response.state ?? '',
        pincode: response.pincode ?? '',
        country: response.country ?? 'India',
        landmark: response.landmark ?? '',
        latitude: response.latitude,
        longitude: response.longitude,
      },
      contactDetails: {
        shopPhoneNumber: response.shopPhoneNumber ?? '',
        shopEmail: response.shopEmail ?? '',
        whatsappNumber: response.whatsappNumber ?? '',
      },
      kycVerification: {
        ownerAadharNumber: response.ownerAadharNumber ?? '',
        ownerPanNumber: response.ownerPanNumber ?? '',
        ownerAadharPhoto: response.ownerAadharPhoto ?? '',
        ownerPanPhoto: response.ownerPanPhoto ?? '',
        ownerSelfieWithId: response.ownerSelfieWithId ?? '',
        gstNumber: response.gstNumber ?? '',
        gstCertificateUpload: response.gstCertificateUpload ?? '',
        businessRegistrationNumber: response.businessRegistrationNumber ?? '',
      },
      bankDetails: {
        accountHolderName: response.accountHolderName ?? '',
        bankName: response.bankName ?? '',
        accountNumber: response.accountNumber ?? '',
        confirmAccountNumber: response.accountNumber ?? '',
        ifscCode: response.ifscCode ?? '',
        upiId: response.upiId ?? '',
        cancelledChequePhoto: response.cancelledChequePhoto ?? '',
      },
      policies: {
        acceptTermsAndConditions: response.acceptTermsAndConditions,
        acceptPrivacyPolicy: response.acceptPrivacyPolicy,
        acceptCommissionPolicy: response.acceptCommissionPolicy,
      },
    });

    this.restoreFileState('profilePhoto', response.profilePhoto);
    this.restoreFileState('ownerAadharPhoto', response.ownerAadharPhoto);
    this.restoreFileState('ownerPanPhoto', response.ownerPanPhoto);
    this.restoreFileState('ownerSelfieWithId', response.ownerSelfieWithId);
    this.restoreFileState('gstCertificateUpload', response.gstCertificateUpload);
    this.restoreFileState('cancelledChequePhoto', response.cancelledChequePhoto);

    this.otpStates.EMAIL.verified = !!response.emailOtpVerified;
    this.otpStates.EMAIL.message = response.emailOtpVerified ? 'Email verified.' : '';
    this.otpStates.EMAIL.previewOtp = '';
    this.otpStates.EMAIL.error = '';
    this.otpStates.PHONE.verified = !!response.phoneOtpVerified;
    this.otpStates.PHONE.message = response.phoneOtpVerified ? 'Phone verified.' : '';
    this.otpStates.PHONE.previewOtp = '';
    this.otpStates.PHONE.error = '';
  }

  private restoreFileState(fieldName: UploadFieldName, value: string | null): void {
    if (!value) {
      this.uploadStates[fieldName] = this.createUploadState();
      return;
    }

    this.uploadStates[fieldName] = {
      fileName: value.split('/').pop() ?? value,
      previewUrl: null,
      progress: 100,
      isUploading: false,
      uploaded: true,
      error: '',
    };
  }

  private applyServerState(response: ShopRegistrationResponse): void {
    this.registrationId = response.registrationId;
    localStorage.setItem(DRAFT_STORAGE_KEY, response.registrationId);
    this.currentStepIndex = Math.min(Math.max((response.currentStep ?? 1) - 1, 0), this.steps.length - 1);
    this.getShopUniqueIdControl().setValue(response.shopUniqueId ?? '');
    this.setOtpVerified('EMAIL', !!response.emailOtpVerified);
    this.setOtpVerified('PHONE', !!response.phoneOtpVerified);
    this.lastSavedAtLabel = response.lastSavedAt
      ? `Saved ${new Date(response.lastSavedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
      : 'Saved just now';
  }

  private applyUploadResponse(
    fieldName: UploadFieldName,
    response: ShopFileUploadResponse,
    fallbackName: string,
    localPreview: string | null,
  ): void {
    this.registrationId = response.registrationId;
    localStorage.setItem(DRAFT_STORAGE_KEY, response.registrationId);
    this.getShopUniqueIdControl().setValue(response.shopUniqueId);

    const uploadState = this.uploadStates[fieldName];
    uploadState.fileName = response.fileName || fallbackName;
    uploadState.previewUrl = localPreview;
    uploadState.progress = 100;
    uploadState.isUploading = false;
    uploadState.uploaded = true;
    uploadState.error = '';

    this.getFileControl(fieldName).setValue(response.filePath);
    this.getFileControl(fieldName).markAsTouched();
  }

  private buildPayload(): ShopRegistrationPayload {
    const rawValue = this.form.getRawValue();
    return {
      registrationId: this.registrationId,
      currentStep: this.currentStepIndex + 1,
      ownerFullName: rawValue.accountDetails.ownerFullName ?? '',
      email: rawValue.accountDetails.email ?? '',
      phoneNumber: rawValue.accountDetails.phoneNumber ?? '',
      password: rawValue.accountDetails.password ?? '',
      confirmPassword: rawValue.accountDetails.confirmPassword ?? '',
      emailOtp: rawValue.accountDetails.emailOtp ?? '',
      phoneOtp: rawValue.accountDetails.phoneOtp ?? '',
      profilePhoto: rawValue.accountDetails.profilePhoto ?? '',
      shopName: rawValue.shopInformation.shopName ?? '',
      shopUniqueId: rawValue.shopInformation.shopUniqueId ?? '',
      shopCategory: rawValue.shopInformation.shopCategory ?? '',
      shopDescription: rawValue.shopInformation.shopDescription ?? '',
      addressLine1: rawValue.address.addressLine1 ?? '',
      addressLine2: rawValue.address.addressLine2 ?? '',
      city: rawValue.address.city ?? '',
      state: rawValue.address.state ?? '',
      pincode: rawValue.address.pincode ?? '',
      country: rawValue.address.country ?? 'India',
      landmark: rawValue.address.landmark ?? '',
      latitude: rawValue.address.latitude,
      longitude: rawValue.address.longitude,
      shopPhoneNumber: rawValue.contactDetails.shopPhoneNumber ?? '',
      shopEmail: rawValue.contactDetails.shopEmail ?? '',
      whatsappNumber: rawValue.contactDetails.whatsappNumber ?? '',
      ownerAadharNumber: (rawValue.kycVerification.ownerAadharNumber ?? '').replace(/\s+/g, ''),
      ownerPanNumber: (rawValue.kycVerification.ownerPanNumber ?? '').toUpperCase(),
      ownerAadharPhoto: rawValue.kycVerification.ownerAadharPhoto ?? '',
      ownerPanPhoto: rawValue.kycVerification.ownerPanPhoto ?? '',
      ownerSelfieWithId: rawValue.kycVerification.ownerSelfieWithId ?? '',
      gstNumber: (rawValue.kycVerification.gstNumber ?? '').toUpperCase(),
      gstCertificateUpload: rawValue.kycVerification.gstCertificateUpload ?? '',
      businessRegistrationNumber: rawValue.kycVerification.businessRegistrationNumber ?? '',
      accountHolderName: rawValue.bankDetails.accountHolderName ?? '',
      bankName: rawValue.bankDetails.bankName ?? '',
      accountNumber: rawValue.bankDetails.accountNumber ?? '',
      confirmAccountNumber: rawValue.bankDetails.confirmAccountNumber ?? '',
      ifscCode: (rawValue.bankDetails.ifscCode ?? '').toUpperCase(),
      upiId: rawValue.bankDetails.upiId ?? '',
      cancelledChequePhoto: rawValue.bankDetails.cancelledChequePhoto ?? '',
      acceptTermsAndConditions: !!rawValue.policies.acceptTermsAndConditions,
      acceptPrivacyPolicy: !!rawValue.policies.acceptPrivacyPolicy,
      acceptCommissionPolicy: !!rawValue.policies.acceptCommissionPolicy,
    };
  }

  private hasMeaningfulData(): boolean {
    const payload = this.buildPayload();
    return Object.entries(payload).some(([key, value]) => {
      if (key === 'registrationId' || key === 'currentStep' || key === 'country') {
        return !!value && value !== 'India';
      }

      if (typeof value === 'boolean') {
        return value;
      }

      if (typeof value === 'number') {
        return value !== null;
      }

      return `${value ?? ''}`.trim().length > 0;
    });
  }

  uploadAccept(fieldName: UploadFieldName): string {
    return UPLOAD_RULES[fieldName].accept;
  }

  private validateFile(fieldName: UploadFieldName, file: File): string {
    const rule = UPLOAD_RULES[fieldName];
    if (!rule.allowedTypes.includes(file.type)) {
      return rule.errorMessage;
    }

    if (file.size > 5 * 1024 * 1024) {
      return 'File size must be 5 MB or smaller.';
    }

    return '';
  }

  private setUploadError(fieldName: UploadFieldName, message: string): void {
    this.uploadStates[fieldName] = {
      ...this.uploadStates[fieldName],
      error: message,
      isUploading: false,
      uploaded: false,
      progress: null,
    };
  }

  private setOtpVerified(channel: OtpChannel, verified: boolean): void {
    const path = channel === 'EMAIL' ? 'accountDetails.emailOtpVerified' : 'accountDetails.phoneOtpVerified';
    this.control(path)?.setValue(verified, { emitEvent: false });
    this.otpStates[channel].verified = verified;
  }

  private resetOtpChannel(channel: OtpChannel): void {
    if (this.restoringDraft) {
      return;
    }

    this.setOtpVerified(channel, false);
    this.otpStates[channel] = this.createOtpState();
  }

  private getStepGroup(groupName: string): FormGroup {
    return this.form.get(groupName) as FormGroup;
  }

  private getShopUniqueIdControl(): AbstractControl {
    return this.form.get('shopInformation.shopUniqueId') as AbstractControl;
  }

  private getFileControl(fieldName: UploadFieldName): AbstractControl {
    const pathMap: Record<UploadFieldName, string> = {
      profilePhoto: 'accountDetails.profilePhoto',
      ownerAadharPhoto: 'kycVerification.ownerAadharPhoto',
      ownerPanPhoto: 'kycVerification.ownerPanPhoto',
      ownerSelfieWithId: 'kycVerification.ownerSelfieWithId',
      gstCertificateUpload: 'kycVerification.gstCertificateUpload',
      cancelledChequePhoto: 'bankDetails.cancelledChequePhoto',
    };

    return this.form.get(pathMap[fieldName]) as AbstractControl;
  }

  private updateSaveTimestampLabel(): void {
    if (!this.lastSavedAtLabel) {
      this.lastSavedAtLabel = 'Draft updated';
    }
  }

  private findStepIndexByField(fieldName: string): number {
    const stepMap: Record<string, number> = {
      ownerFullName: 0,
      email: 0,
      phoneNumber: 0,
      password: 0,
      confirmPassword: 0,
      emailOtp: 0,
      phoneOtp: 0,
      profilePhoto: 0,
      shopName: 1,
      shopCategory: 1,
      shopDescription: 1,
      addressLine1: 2,
      addressLine2: 2,
      city: 2,
      state: 2,
      pincode: 2,
      country: 2,
      landmark: 2,
      latitude: 2,
      longitude: 2,
      shopPhoneNumber: 3,
      shopEmail: 3,
      whatsappNumber: 3,
      ownerAadharNumber: 4,
      ownerPanNumber: 4,
      ownerAadharPhoto: 4,
      ownerPanPhoto: 4,
      ownerSelfieWithId: 4,
      gstNumber: 4,
      gstCertificateUpload: 4,
      businessRegistrationNumber: 4,
      accountHolderName: 5,
      bankName: 5,
      accountNumber: 5,
      confirmAccountNumber: 5,
      ifscCode: 5,
      upiId: 5,
      cancelledChequePhoto: 5,
      acceptTermsAndConditions: 6,
      acceptPrivacyPolicy: 6,
      acceptCommissionPolicy: 6,
    };

    return stepMap[fieldName] ?? -1;
  }

  private createOtpState(): OtpActionState {
    return {
      sending: false,
      verifying: false,
      verified: false,
      message: '',
      previewOtp: '',
      error: '',
    };
  }

  private createUploadState(): UploadState {
    return {
      fileName: '',
      previewUrl: null,
      progress: null,
      isUploading: false,
      uploaded: false,
      error: '',
    };
  }

}
