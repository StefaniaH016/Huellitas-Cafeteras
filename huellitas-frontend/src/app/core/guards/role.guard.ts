import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { requireAuthentication } from '../utils/guard.utils';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  const authenticationResult = requireAuthentication(auth, tokenService, router);
  if (authenticationResult !== true) {
    return authenticationResult;
  }

  const roles: string[] = route.data['roles'] ?? [];
  const userRole = auth.getRol();

  // Then check role
  if (userRole && roles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/acceso-denegado']);
};
