import { INDIAN_BANKS } from './banks.constants';

export type UploadFieldName =
  | 'profilePhoto'
  | 'ownerAadharPhoto'
  | 'ownerPanPhoto'
  | 'ownerSelfieWithId'
  | 'gstCertificateUpload'
  | 'cancelledChequePhoto';

export type UploadRule = {
  accept: string;
  allowedTypes: string[];
  errorMessage: string;
};

export const SHOP_REGISTRATION_DRAFT_STORAGE_KEY = 'shop-registration-draft-id';

export const SHOP_REGISTRATION_STEPS = [
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
] as const;

export const SHOP_CATEGORIES = [
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
] as const;

export const INDIAN_STATES = [
  'Andhra Pradesh',
  'Arunachal Pradesh',
  'Assam',
  'Bihar',
  'Chhattisgarh',
  'Goa',
  'Gujarat',
  'Haryana',
  'Himachal Pradesh',
  'Jharkhand',
  'Karnataka',
  'Kerala',
  'Madhya Pradesh',
  'Maharashtra',
  'Manipur',
  'Meghalaya',
  'Mizoram',
  'Nagaland',
  'Odisha',
  'Punjab',
  'Rajasthan',
  'Sikkim',
  'Tamil Nadu',
  'Telangana',
  'Tripura',
  'Uttar Pradesh',
  'Uttarakhand',
  'West Bengal',
  'Delhi',
  'Jammu and Kashmir',
  'Ladakh',
  'Puducherry',
] as const;

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

export const SHOP_REGISTRATION_UPLOAD_RULES: Record<UploadFieldName, UploadRule> = {
  profilePhoto: IMAGE_UPLOAD_RULE,
  ownerAadharPhoto: IMAGE_UPLOAD_RULE,
  ownerPanPhoto: IMAGE_UPLOAD_RULE,
  ownerSelfieWithId: IMAGE_UPLOAD_RULE,
  gstCertificateUpload: DOCUMENT_UPLOAD_RULE,
  cancelledChequePhoto: IMAGE_UPLOAD_RULE,
};

export const SHOP_REGISTRATION_TEXT = {
  hero: {
    eyebrow: 'Enterprise Marketplace Onboarding',
    title: 'Register every shop with a structured, draft-safe verification flow.',
    copy: 'Capture business details, KYC documents, banking information, and policy consent through a multi-step journey designed for marketplace operations teams.',
    completionLabel: 'Completion',
    saveStates: {
      saving: 'Saving...',
      saved: 'Saved',
      error: 'Save failed',
      idle: 'Idle',
    },
    shopIdLabel: 'Shop ID',
    shopIdFallback: 'Generated after first save',
  },
  progress: {
    title: 'Shop registration flow',
    description: 'Completed steps remain clickable so operators can review earlier information without losing context.',
    returnLink: 'Return to dashboard',
  },
  actions: {
    saveDraft: 'Save Draft',
    saveContinue: 'Save & Continue',
    back: 'Back',
    submit: 'Submit Application',
    submitting: 'Submitting...',
  },
  accountDetails: {
    cardTitle: 'Owner profile',
    cardDescription: 'Use the legal owner name and verified business contact information.',
    sending: 'Sending...',
    sendEmailOtp: 'Send Email OTP',
    sendPhoneOtp: 'Send Phone OTP',
    verifying: 'Verifying...',
    verified: 'Verified',
    verifyEmailOtp: 'Verify Email OTP',
    verifyPhoneOtp: 'Verify Phone OTP',
    emailVerified: 'Email verified.',
    phoneVerified: 'Phone verified.',
    verificationRuleTitle: 'Verification rule',
    verificationRuleDescription: 'For now, either email OTP or phone OTP verification is enough to continue.',
    previewPrefix: 'Test OTP:',
    fields: {
      ownerFullName: {
        label: 'Owner Full Name',
        tooltip: 'Use the exact legal or tax-registered owner name.',
      },
      email: {
        label: 'Email Address',
        tooltip: 'Primary owner email for operational communication.',
      },
      phoneNumber: {
        label: 'Phone Number',
        tooltip: '10 digit Indian mobile number used for SMS OTP verification.',
      },
      password: {
        label: 'Password',
        tooltip: 'Minimum 8 characters for marketplace account access.',
      },
      confirmPassword: {
        label: 'Confirm Password',
        tooltip: 'Repeat the same password exactly.',
      },
      emailOtp: {
        label: 'Email OTP',
        tooltip: 'Enter the 6 digit email OTP and verify it.',
      },
      phoneOtp: {
        label: 'Phone OTP',
        tooltip: 'Enter the 6 digit OTP sent to the phone number and verify it.',
      },
      profilePhoto: {
        label: 'Profile Photo',
        helperText: 'Upload a clear profile photo in JPG, PNG, or WEBP format. Maximum 5 MB.',
      },
    },
  },
  shopInformation: {
    cardTitle: 'Storefront identity',
    cardDescription: 'Define how the shop appears across marketplace catalogs and administrative systems.',
    fields: {
      shopName: {
        label: 'Shop Name',
        tooltip: 'Use the public-facing storefront name.',
      },
      shopUniqueId: {
        label: 'Shop Unique ID',
        tooltip: 'Generated by the system after the first draft save.',
      },
      shopCategory: {
        label: 'Shop Category',
        tooltip: 'Choose the best-fit operating category for discovery and reporting.',
        placeholder: 'Select a category',
      },
      shopDescription: {
        label: 'Shop Description',
        tooltip: 'Describe inventory, operating model, and unique value proposition.',
      },
    },
  },
  address: {
    cardTitle: 'Operational address',
    cardDescription: 'Capture the physical business location so fulfillment and field operations have precise coordinates.',
    fields: {
      addressLine1: {
        label: 'Address Line 1',
        tooltip: 'Street, building number, or primary business address line.',
      },
      addressLine2: {
        label: 'Address Line 2',
        tooltip: 'Optional landmark zone, floor, or suite detail.',
      },
      city: {
        label: 'City',
        tooltip: 'City where the shop operates.',
      },
      state: {
        label: 'State',
        tooltip: 'Administrative region used for tax, delivery, and support mapping.',
        placeholder: 'Select a state',
      },
      pincode: {
        label: 'Pincode',
        tooltip: '6 digit Indian pincode for routing and service coverage.',
      },
      country: {
        label: 'Country',
        tooltip: 'Defaults to India for this onboarding program.',
      },
      landmark: {
        label: 'Landmark',
        tooltip: 'Helpful nearby landmark for navigation and support visits.',
      },
      latitude: {
        label: 'Latitude',
        tooltip: 'Optional geolocation coordinate for map pinning.',
      },
      longitude: {
        label: 'Longitude',
        tooltip: 'Optional geolocation coordinate for map pinning.',
      },
    },
  },
  contactDetails: {
    cardTitle: 'Customer contact channels',
    cardDescription: 'These details will be used in marketplace operations, escalations, and customer communication.',
    fields: {
      shopPhoneNumber: {
        label: 'Shop Phone Number',
        tooltip: 'Primary phone number visible to marketplace support teams.',
      },
      shopEmail: {
        label: 'Shop Email',
        tooltip: 'Customer-facing email for order or support related communication.',
      },
      whatsappNumber: {
        label: 'WhatsApp Number',
        tooltip: 'Operations and customer communication number for WhatsApp updates.',
      },
    },
  },
  kycVerification: {
    cardTitle: 'KYC and statutory verification',
    cardDescription: 'Upload mandatory identity proof and capture compliance references for onboarding review.',
    fields: {
      ownerAadharNumber: {
        label: 'Owner Aadhar Number',
        tooltip: 'Enter the 12 digit Aadhar number without spaces.',
      },
      ownerPanNumber: {
        label: 'Owner PAN Number',
        tooltip: 'Use the PAN in uppercase format.',
      },
      ownerAadharPhoto: {
        label: 'Owner Aadhar Photo',
        helperText: 'Upload the Aadhar photo in JPG, PNG, or WEBP format. Maximum 5 MB.',
      },
      ownerPanPhoto: {
        label: 'Owner PAN Photo',
        helperText: 'Upload the PAN photo in JPG, PNG, or WEBP format. Maximum 5 MB.',
      },
      ownerSelfieWithId: {
        label: 'Owner Selfie With ID',
        helperText: 'Upload the selfie with ID in JPG, PNG, or WEBP format. Maximum 5 MB.',
      },
      gstNumber: {
        label: 'GST Number',
        tooltip: 'Optional, but if provided the GST certificate becomes mandatory.',
      },
      gstCertificateUpload: {
        label: 'GST Certificate Upload',
        helperText: 'Required only when GST number is entered. Upload PDF or DOCX. Maximum 5 MB.',
      },
      businessRegistrationNumber: {
        label: 'Business Registration Number',
        tooltip: 'Trade license, MSME, or other business registration reference.',
      },
    },
  },
  bankDetails: {
    cardTitle: 'Settlement and payouts',
    cardDescription: 'Use the operational payout account that should receive marketplace settlements.',
    selectPlaceholder: 'Select a bank',
    fields: {
      accountHolderName: {
        label: 'Account Holder Name',
        tooltip: 'Must match the bank records for faster verification.',
      },
      bankName: {
        label: 'Bank Name',
        tooltip: 'Choose the payout bank used for settlements.',
      },
      accountNumber: {
        label: 'Account Number',
        tooltip: 'Use only numeric bank account digits.',
      },
      confirmAccountNumber: {
        label: 'Confirm Account Number',
        tooltip: 'Repeat the payout account number to avoid settlement issues.',
      },
      ifscCode: {
        label: 'IFSC Code',
        tooltip: '11 character IFSC used for bank verification.',
      },
      upiId: {
        label: 'UPI ID',
        tooltip: 'Optional UPI handle for faster settlements or reimbursements.',
      },
      cancelledChequePhoto: {
        label: 'Cancelled Cheque Photo',
        helperText: 'Upload the cancelled cheque photo in JPG, PNG, or WEBP format. Maximum 5 MB.',
      },
    },
  },
  policies: {
    cardTitle: 'Application review',
    cardDescription: 'Confirm the business is ready for operational onboarding and marketplace policy compliance.',
    review: {
      owner: 'Owner',
      shop: 'Shop',
      location: 'Location',
      status: 'Status',
      ownerFallback: 'Not captured yet',
      shopFallback: 'Not captured yet',
      cityFallback: 'City missing',
      stateFallback: 'State missing',
      statusDraftSaved: 'Draft saved',
      statusSaving: 'Saving',
      statusPending: 'Pending',
    },
    checkboxes: {
      terms: {
        title: 'Accept Terms and Conditions',
        description: 'Confirms the shop agrees to marketplace operating rules and onboarding commitments.',
      },
      privacy: {
        title: 'Accept Privacy Policy',
        description: 'Confirms understanding of data processing, KYC storage, and operational communication handling.',
      },
      commission: {
        title: 'Accept Commission Policy',
        description: 'Confirms agreement to the platform’s revenue share and payout settlement policy.',
      },
    },
  },
  messages: {
    otpVerificationRequired: 'Verify either email OTP or phone OTP before continuing.',
    passwordsDoNotMatch: 'Passwords do not match.',
    accountNumbersDoNotMatch: 'Account numbers do not match.',
    gstCertificateRequired: 'GST certificate is required when GST number is provided.',
    fileSizeExceeded: 'File size must be 5 MB or smaller.',
    draftUpdated: 'Draft updated',
    applicationSubmitted: 'Application submitted',
    savedJustNow: 'Saved just now',
    savedAtPrefix: 'Saved',
    sendOtpFailed: 'Unable to send OTP right now.',
    verifyOtpFailed: 'Unable to verify OTP right now.',
    submitFailed: 'Unable to submit the application right now.',
    uploadFailed: 'Unable to upload the file right now.',
    saveDraftFailed: 'Unable to save the shop registration draft right now.',
    invalidEmail: 'Enter a valid email address.',
    genericReview: 'Please review {label}.',
  },
  defaults: {
    country: 'India',
  },
  banks: [...INDIAN_BANKS],
} as const;
