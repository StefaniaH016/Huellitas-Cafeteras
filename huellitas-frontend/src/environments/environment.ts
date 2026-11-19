import { resolveApiUrl } from './api-url';

export const environment = {
  production: false,
  apiUrl: resolveApiUrl(typeof window === 'undefined' ? '' : window.location.hostname),
};
