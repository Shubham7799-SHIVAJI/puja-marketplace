import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

export interface StandardApiErrorResponse {
  code?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
}

@Injectable({
  providedIn: 'root',
})
export class ApiErrorService {
  parse(error: unknown): StandardApiErrorResponse | null {
    if (error instanceof HttpErrorResponse) {
      if (error.error && typeof error.error === 'object') {
        return error.error as StandardApiErrorResponse;
      }

      return {
        message: error.message,
      };
    }

    if (typeof error === 'object' && error !== null) {
      return error as StandardApiErrorResponse;
    }

    return null;
  }

  getFieldErrors(error: unknown): Record<string, string> {
    return this.parse(error)?.fieldErrors ?? {};
  }

  getMessage(error: unknown, fallbackMessage: string): string {
    return this.parse(error)?.message ?? fallbackMessage;
  }
}
