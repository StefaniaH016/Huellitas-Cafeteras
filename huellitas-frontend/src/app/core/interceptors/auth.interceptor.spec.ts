import { HttpRequest, HttpResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TokenService } from '../services/token.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let tokenService: Pick<TokenService, 'getToken'>;

  beforeEach(() => {
    tokenService = { getToken: vi.fn().mockReturnValue(null) };
    TestBed.configureTestingModule({
      providers: [{ provide: TokenService, useValue: tokenService }],
    });
  });

  it('adds a bearer token when one is present', () => {
    vi.mocked(tokenService.getToken).mockReturnValue('jwt-token');
    const request = new HttpRequest('GET', '/api/casos');
    const next = vi.fn((req: HttpRequest<unknown>) => of(new HttpResponse({ body: req } as never)));

    const result = TestBed.runInInjectionContext(() => authInterceptor(request, next));

    expect(next).toHaveBeenCalledOnce();
    expect(next.mock.calls[0][0].headers.get('Authorization')).toBe('Bearer jwt-token');
    expect(result).toBeDefined();
  });

  it('forwards the original request when no token exists', () => {
    const request = new HttpRequest('GET', '/api/casos');
    const next = vi.fn((req: HttpRequest<unknown>) => of(new HttpResponse({ body: req } as never)));

    TestBed.runInInjectionContext(() => authInterceptor(request, next));

    expect(next).toHaveBeenCalledWith(request);
  });
});
