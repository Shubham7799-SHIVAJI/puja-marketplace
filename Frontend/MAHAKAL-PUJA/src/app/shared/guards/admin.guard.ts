import { CanActivateFn, CanMatchFn, Router, UrlSegment } from '@angular/router';
import { inject } from '@angular/core';

import { AuthSessionService } from '../services/auth-session';

function isAdminRole(role: string | null | undefined): boolean {
  const normalized = (role ?? '').trim().toUpperCase();
  return normalized === 'ADMIN' || normalized === 'SUPER_ADMIN';
}

function evaluateAccess(targetPath: string) {
  const router = inject(Router);
  const authSessionService = inject(AuthSessionService);
  const session = authSessionService.getSession();

  if (!session?.token) {
    return router.createUrlTree(['/signin'], {
      queryParams: { returnUrl: targetPath },
    });
  }

  if (!isAdminRole(session.role)) {
    return router.createUrlTree(['/home']);
  }

  return true;
}

export const adminGuard: CanActivateFn = (route, state) => evaluateAccess(state.url);

export const adminMatchGuard: CanMatchFn = (_route, segments: UrlSegment[]) => {
  const path = '/' + segments.map((segment) => segment.path).join('/');
  return evaluateAccess(path || '/admin');
};
