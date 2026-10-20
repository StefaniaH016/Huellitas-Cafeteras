import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { roleGuard } from './role.guard';

let auth: Pick<AuthService, 'getRol' | 'isLoggedIn' | 'logout'>;
let tokenService: Pick<TokenService, 'decodePayload'>;
let router: Pick<Router, 'createUrlTree'>;

beforeEach(() => {
  auth = {
    getRol: vi.fn(),
    isLoggedIn: vi.fn().mockReturnValue(true),
    logout: vi.fn(),
  };
  tokenService = { decodePayload: vi.fn().mockReturnValue(null) };
  router = { createUrlTree: vi.fn().mockReturnValue({} as UrlTree) };
  TestBed.configureTestingModule({
    providers: [
      { provide: AuthService, useValue: auth },
      { provide: TokenService, useValue: tokenService },
      { provide: Router, useValue: router },
    ],
  });
});

it('allows a user whose role is listed on the route', () => {
  vi.mocked(auth.getRol).mockReturnValue('FUNDACION_REFUGIO');
  const route = { data: { roles: ['FUNDACION_REFUGIO'] } } as unknown as ActivatedRouteSnapshot;

  const result = TestBed.runInInjectionContext(() => roleGuard(route, {} as never));

  expect(result).toBe(true);
});

it('redirects a user whose role is not allowed', () => {
  vi.mocked(auth.getRol).mockReturnValue('CIUDADANO_VOLUNTARIO');
  const route = { data: { roles: ['ADMINISTRADOR'] } } as unknown as ActivatedRouteSnapshot;
  const deniedTree = router.createUrlTree(['/acceso-denegado']);

  const result = TestBed.runInInjectionContext(() => roleGuard(route, {} as never));

  expect(result).toBe(deniedTree);
});

it('returns to login before evaluating roles when unauthenticated', () => {
  vi.mocked(auth.isLoggedIn).mockReturnValue(false);
  const route = { data: { roles: ['ADMINISTRADOR'] } } as unknown as ActivatedRouteSnapshot;
  const loginTree = router.createUrlTree(['/login']);

  const result = TestBed.runInInjectionContext(() => roleGuard(route, {} as never));

  expect(result).toBe(loginTree);
  expect(auth.getRol).not.toHaveBeenCalled();
});
