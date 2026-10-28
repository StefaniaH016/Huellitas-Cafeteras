import { resolveApiUrl } from './api-url';

describe('resolveApiUrl', () => {
  it.each(['localhost', 'app.localhost', '127.0.0.1', '::1', '[::1]'])(
    'uses the local API for %s',
    (hostname) => {
      expect(resolveApiUrl(hostname)).toBe('http://localhost:8080/v1');
    },
  );

  it.each(['huellitas-cafeteras-prod.azurewebsites.net', 'huellitas.example.com'])(
    'uses the production API for %s',
    (hostname) => {
      expect(resolveApiUrl(hostname)).toBe(
        'https://huellitas-cafeteras-prod.azurewebsites.net/v1',
      );
    },
  );
});
