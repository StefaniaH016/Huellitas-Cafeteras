import { Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

export function requireAuthentication(
  auth: AuthService,
  tokenService: TokenService,
  router: Router,
): true | UrlTree {
  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const payload = tokenService.decodePayload();
  if (payload && typeof payload['exp'] === 'number') {
    const now = Math.floor(Date.now() / 1000);
    if (payload['exp'] < now) {
      auth.logout();
      return router.createUrlTree(['/login']);
    }
  }

  return true;
}
