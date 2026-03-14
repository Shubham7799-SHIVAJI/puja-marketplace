import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SecureQueryStateService {
  private readonly sessionKeyName = 'otp_secure_query_secret';

  async encryptForUrl(plainText: string): Promise<string> {
    if (!plainText) {
      return '';
    }

    const secret = this.getOrCreateSecret();
    const cryptoKey = await this.importKey(secret);
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encodedText = new TextEncoder().encode(plainText);

    const cipherBuffer = await crypto.subtle.encrypt(
      {
        name: 'AES-GCM',
        iv: this.toArrayBuffer(iv),
      },
      cryptoKey,
      this.toArrayBuffer(encodedText),
    );

    const cipherBytes = new Uint8Array(cipherBuffer);
    return `${this.toBase64Url(iv)}.${this.toBase64Url(cipherBytes)}`;
  }

  async decryptFromUrl(encryptedPayload: string): Promise<string | null> {
    if (!encryptedPayload) {
      return '';
    }

    const secret = sessionStorage.getItem(this.sessionKeyName);
    if (!secret) {
      return null;
    }

    const [ivPart, cipherPart] = encryptedPayload.split('.');
    if (!ivPart || !cipherPart) {
      return null;
    }

    try {
      const cryptoKey = await this.importKey(secret);
      const iv = this.fromBase64Url(ivPart);
      const cipherBytes = this.fromBase64Url(cipherPart);

      const plainBuffer = await crypto.subtle.decrypt(
        {
          name: 'AES-GCM',
          iv: this.toArrayBuffer(iv),
        },
        cryptoKey,
        this.toArrayBuffer(cipherBytes),
      );

      return new TextDecoder().decode(plainBuffer);
    } catch {
      return null;
    }
  }

  private getOrCreateSecret(): string {
    const existingSecret = sessionStorage.getItem(this.sessionKeyName);
    if (existingSecret) {
      try {
        const existingBytes = this.fromBase64Url(existingSecret);
        if (this.isValidAesKeyLength(existingBytes)) {
          return existingSecret;
        }
      } catch {
        // Invalid legacy key format: regenerate below.
      }
    }

    const randomBytes = crypto.getRandomValues(new Uint8Array(32));
    const generatedSecret = this.toBase64Url(randomBytes);
    sessionStorage.setItem(this.sessionKeyName, generatedSecret);
    return generatedSecret;
  }

  private async importKey(secret: string): Promise<CryptoKey> {
    const secretBytes = this.fromBase64Url(secret);
    if (!this.isValidAesKeyLength(secretBytes)) {
      throw new Error('Invalid AES key size');
    }

    return crypto.subtle.importKey(
      'raw',
      this.toArrayBuffer(secretBytes),
      {
        name: 'AES-GCM',
      },
      false,
      ['encrypt', 'decrypt'],
    );
  }

  private toBase64Url(bytes: Uint8Array): string {
    let binary = '';
    bytes.forEach((byte) => {
      binary += String.fromCharCode(byte);
    });

    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }

  private fromBase64Url(value: string): Uint8Array {
    const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
    const padding = (4 - (normalized.length % 4)) % 4;
    const base64 = `${normalized}${'='.repeat(padding)}`;
    const binary = atob(base64);

    const bytes = new Uint8Array(binary.length);
    for (let index = 0; index < binary.length; index += 1) {
      bytes[index] = binary.charCodeAt(index);
    }

    return bytes;
  }

  private toArrayBuffer(bytes: Uint8Array): ArrayBuffer {
    return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) as ArrayBuffer;
  }

  private isValidAesKeyLength(bytes: Uint8Array): boolean {
    return bytes.byteLength === 16 || bytes.byteLength === 32;
  }
}