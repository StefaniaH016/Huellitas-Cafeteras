import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let auth: Pick<AuthService, 'isLoggedIn' | 'logout'>;
  let tokenService: Pick<TokenService, 'decodePayload'>;
  let router: Pick<Router, 'createUrlTree'>;

  beforeEach(() => {
    auth = { isLoggedIn: vi.fn(), logout: vi.fn() };
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

  it('returns true when the user is authenticated', () => {
    vi.mocked(auth.isLoggedIn).mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('returns the login UrlTree when the user is unauthenticated', () => {
    vi.mocked(auth.isLoggedIn).mockReturnValue(false);
    const loginTree = router.createUrlTree(['/login']);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBe(loginTree);
  });
});
