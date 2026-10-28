import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { requireAuthentication } from '../utils/guard.utils';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  return requireAuthentication(auth, tokenService, router);
};
