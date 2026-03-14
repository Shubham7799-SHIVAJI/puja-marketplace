import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { OtpSecurityContextService } from '../services/otp-security-context';

export const setPasswordGuard: CanActivateFn = () => {
  const router = inject(Router);
  const otpSecurityContextService = inject(OtpSecurityContextService);

  if (otpSecurityContextService.isContextValid()) {
    return true;
  }

  otpSecurityContextService.clearContext();
  return router.createUrlTree(['/signin']);
};
