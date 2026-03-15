import { FirebaseOptions } from 'firebase/app';

type RuntimeFirebaseConfig = Partial<FirebaseOptions>;

declare global {
  interface Window {
    __DAILY_PUJA_FIREBASE__?: RuntimeFirebaseConfig;
  }
}

const runtimeConfig: RuntimeFirebaseConfig = typeof window !== 'undefined' && window.__DAILY_PUJA_FIREBASE__
  ? window.__DAILY_PUJA_FIREBASE__
  : {};

export const firebasePhoneAuthConfig: FirebaseOptions = {
  apiKey: runtimeConfig.apiKey ?? '',
  authDomain: runtimeConfig.authDomain ?? '',
  projectId: runtimeConfig.projectId ?? '',
  storageBucket: runtimeConfig.storageBucket ?? '',
  messagingSenderId: runtimeConfig.messagingSenderId ?? '',
  appId: runtimeConfig.appId ?? '',
  measurementId: runtimeConfig.measurementId ?? '',
};

export function isFirebasePhoneAuthConfigured(): boolean {
  return Object.values(firebasePhoneAuthConfig).every(
    (value) => typeof value === 'string' && value.trim().length > 0,
  );
}