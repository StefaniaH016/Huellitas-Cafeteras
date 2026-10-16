import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  // Check token expiration
  const payload = tokenService.decodePayload();
  if (payload && typeof payload['exp'] === 'number') {
    const now = Math.floor(Date.now() / 1000);
    if (payload['exp'] < now) {
      // Token expired
      auth.logout();
      return router.createUrlTree(['/login']);
    }
  }

  return true;
};
