import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Check authentication first
  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const roles: string[] = route.data['roles'] ?? [];
  const userRole = auth.getRol();

  // Then check role
  if (userRole && roles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/acceso-denegado']);
};
