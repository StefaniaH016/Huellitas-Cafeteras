import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  // Check authentication first
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

  const roles: string[] = route.data['roles'] ?? [];
  const userRole = auth.getRol();

  // Then check role
  if (userRole && roles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/acceso-denegado']);
};
