import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const roles: string[] = route.data['roles'] ?? [];
  const userRole = auth.getRol();

  if (userRole && roles.includes(userRole)) {
    return true;
  }

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  return router.createUrlTree(['/acceso-denegado']);
};
