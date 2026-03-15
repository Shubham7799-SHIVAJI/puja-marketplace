import { FirebaseOptions } from 'firebase/app';

export const firebasePhoneAuthConfig: FirebaseOptions = {
  apiKey: 'AIzaSyDcVB4MiUWU6wsqNzRuY9pTR9GJfZHnkRk',
  authDomain: 'daily-mahakal-puja.firebaseapp.com',
  projectId: 'daily-mahakal-puja',
  storageBucket: 'daily-mahakal-puja.firebasestorage.app',
  messagingSenderId: '400664692534',
  appId: '1:400664692534:web:c176b626ffff27439e5597',
  measurementId: 'G-WBSP7BN92D',
};

const REQUIRED_PLACEHOLDER_PREFIX = 'YOUR_FIREBASE_';

export function isFirebasePhoneAuthConfigured(): boolean {
  return Object.values(firebasePhoneAuthConfig).every(
    (value) => typeof value === 'string' && !value.startsWith(REQUIRED_PLACEHOLDER_PREFIX),
  );
}