import { Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { requireAuthentication } from './guard.utils';

let auth: Pick<AuthService, 'isLoggedIn' | 'logout'>;
let tokenService: Pick<TokenService, 'decodePayload'>;
let router: Pick<Router, 'createUrlTree'>;
let loginTree: UrlTree;

beforeEach(() => {
  loginTree = {} as UrlTree;
  auth = { isLoggedIn: vi.fn(), logout: vi.fn() };
  tokenService = { decodePayload: vi.fn() };
  router = { createUrlTree: vi.fn().mockReturnValue(loginTree) };
});

it('redirects unauthenticated users without decoding a token', () => {
  vi.mocked(auth.isLoggedIn).mockReturnValue(false);

  const result = requireAuthentication(
    auth as AuthService,
    tokenService as TokenService,
    router as Router,
  );

  expect(result).toBe(loginTree);
  expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  expect(tokenService.decodePayload).not.toHaveBeenCalled();
});

it('allows authenticated users when the token has no expiration claim', () => {
  vi.mocked(auth.isLoggedIn).mockReturnValue(true);
  vi.mocked(tokenService.decodePayload).mockReturnValue({});

  expect(
    requireAuthentication(auth as AuthService, tokenService as TokenService, router as Router),
  ).toBe(true);
});

it('allows an authenticated user with a future expiration', () => {
  vi.mocked(auth.isLoggedIn).mockReturnValue(true);
  vi.mocked(tokenService.decodePayload).mockReturnValue({
    exp: Math.floor(Date.now() / 1000) + 60,
  });

  expect(
    requireAuthentication(auth as AuthService, tokenService as TokenService, router as Router),
  ).toBe(true);
});

it('logs out and redirects an authenticated user with an expired token', () => {
  vi.mocked(auth.isLoggedIn).mockReturnValue(true);
  vi.mocked(tokenService.decodePayload).mockReturnValue({
    exp: Math.floor(Date.now() / 1000) - 1,
  });

  const result = requireAuthentication(
    auth as AuthService,
    tokenService as TokenService,
    router as Router,
  );

  expect(auth.logout).toHaveBeenCalledOnce();
  expect(result).toBe(loginTree);
});
