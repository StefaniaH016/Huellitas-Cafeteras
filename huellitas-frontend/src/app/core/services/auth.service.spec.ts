import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { TokenService } from './token.service';

let service: AuthService;
let http: HttpTestingController;
let router: { navigate: ReturnType<typeof vi.fn> };
let tokenService: {
  decodePayload: ReturnType<typeof vi.fn>;
  getToken: ReturnType<typeof vi.fn>;
  removeToken: ReturnType<typeof vi.fn>;
  setToken: ReturnType<typeof vi.fn>;
};

beforeEach(() => {
  router = { navigate: vi.fn().mockResolvedValue(true) };
  tokenService = {
    decodePayload: vi.fn().mockReturnValue(null),
    getToken: vi.fn().mockReturnValue(null),
    removeToken: vi.fn(),
    setToken: vi.fn(),
  };

  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: Router, useValue: router },
      { provide: TokenService, useValue: tokenService },
    ],
  });
  service = TestBed.inject(AuthService);
  http = TestBed.inject(HttpTestingController);
  localStorage.clear();
});

afterEach(() => {
  http.verify();
  localStorage.clear();
  vi.restoreAllMocks();
});

it('logs in, stores the token and role, and emits the role', () => {
  let result: string | undefined;
  service.login('user@example.test', 'secret').subscribe((role) => (result = role));

  const request = http.expectOne(`${environment.apiUrl}/auth/login`);
  expect(request.request.body).toEqual({ correo: 'user@example.test', contrasena: 'secret' });
  request.flush({ token: 'jwt', rol: 'ADMINISTRADOR' });

  expect(tokenService.setToken).toHaveBeenCalledWith('jwt');
  expect(localStorage.getItem('hc_rol')).toBe('ADMINISTRADOR');
  expect(result).toBe('ADMINISTRADOR');
});

it('uses the role alias and falls back to the token claim', () => {
  tokenService.decodePayload.mockReturnValue({ rol: 'VETERINARIA' });
  let roleAlias: string | undefined;
  service.login('user@example.test', 'secret').subscribe((role) => (roleAlias = role));
  http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: 'jwt', role: 'FUNDACION' });
  expect(roleAlias).toBe('FUNDACION');

  let tokenRole: string | undefined;
  service.login('user@example.test', 'secret').subscribe((role) => (tokenRole = role));
  http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: 'jwt' });
  expect(tokenRole).toBe('VETERINARIA');
  expect(localStorage.getItem('hc_rol')).toBe('VETERINARIA');
});

it('registers a user with the expected request body', () => {
  const expected = { id: '1', correo: 'user@example.test', nombre: 'Luna' };
  let result: unknown;
  service.registro('user@example.test', 'secret', 'Luna').subscribe((response) => {
    result = response;
  });

  const request = http.expectOne(`${environment.apiUrl}/auth/registro`);
  expect(request.request.body).toEqual({
    correo: 'user@example.test',
    contrasena: 'secret',
    nombre: 'Luna',
  });
  request.flush(expected);
  expect(result).toEqual(expected);
});

it('clears credentials and redirects on logout', () => {
  localStorage.setItem('hc_rol', 'ADMINISTRADOR');

  service.logout();

  expect(tokenService.removeToken).toHaveBeenCalledOnce();
  expect(localStorage.getItem('hc_rol')).toBeNull();
  expect(router.navigate).toHaveBeenCalledWith(['/login']);
});

it('reports whether a token exists', () => {
  tokenService.getToken.mockReturnValue('jwt');
  expect(service.isLoggedIn()).toBe(true);

  tokenService.getToken.mockReturnValue(null);
  expect(service.isLoggedIn()).toBe(false);
});

it('normalizes a stored role and falls back to the token role', () => {
  localStorage.setItem('hc_rol', 'fundacion');
  expect(service.getRol()).toBe('FUNDACION_REFUGIO');

  localStorage.removeItem('hc_rol');
  tokenService.decodePayload.mockReturnValue({ rol: 'CIUDADANO' });
  expect(service.getRol()).toBe('CIUDADANO_VOLUNTARIO');
});

it('warns and returns null for an unrecognized stored role', () => {
  const warning = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
  localStorage.setItem('hc_rol', 'UNRECOGNIZED');

  expect(service.getRol()).toBeNull();
  expect(warning).toHaveBeenCalled();
});

it('navigates to home after login and reads the name from the token', () => {
  tokenService.decodePayload.mockReturnValue({ nombre: 'Luna' });

  service.navigateAfterLogin();

  expect(router.navigate).toHaveBeenCalledWith(['/home']);
  expect(service.getNombre()).toBe('Luna');
});
