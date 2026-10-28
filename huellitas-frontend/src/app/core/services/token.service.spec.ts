import { TokenService } from './token.service';

let service: TokenService;

beforeEach(() => {
  localStorage.clear();
  service = new TokenService();
});

afterEach(() => {
  vi.restoreAllMocks();
});

it('stores, returns, and removes the token', () => {
  service.setToken('jwt-token');

  expect(service.getToken()).toBe('jwt-token');
  expect(service.hasToken()).toBe(true);

  service.removeToken();

  expect(service.getToken()).toBeNull();
  expect(service.hasToken()).toBe(false);
});

it('clears the token through the alias', () => {
  service.setToken('jwt-token');

  service.clearToken();

  expect(localStorage.getItem('auth_token')).toBeNull();
});

it('decodes a valid JWT payload', () => {
  const payload = btoa(JSON.stringify({ exp: 123, nombre: 'Luna' }));
  service.setToken(`header.${payload}.signature`);

  expect(service.decodePayload()).toEqual({ exp: 123, nombre: 'Luna' });
});

it('returns null when no token exists', () => {
  expect(service.decodePayload()).toBeNull();
});

it('returns null and warns for an invalid token format', () => {
  const warning = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
  service.setToken('not-a-jwt');

  expect(service.decodePayload()).toBeNull();
  expect(warning).toHaveBeenCalled();
});

it('returns null when the payload cannot be decoded', () => {
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  service.setToken('header.a.signature');

  expect(service.decodePayload()).toBeNull();
  expect(error).toHaveBeenCalled();
});
