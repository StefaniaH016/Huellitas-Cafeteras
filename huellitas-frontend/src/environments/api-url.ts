const localApiUrl = 'http://localhost:8080/v1';
const productionApiUrl = 'https://huellitas-cafeteras-prod.azurewebsites.net/v1';

export function resolveApiUrl(hostname: string): string {
  const normalizedHostname = hostname.toLowerCase().replace(/^\[|\]$/g, '');
  const isLocal =
    normalizedHostname === 'localhost' ||
    normalizedHostname.endsWith('.localhost') ||
    normalizedHostname === '::1' ||
    /^127(?:\.\d{1,3}){3}$/.test(normalizedHostname);

  return isLocal ? localApiUrl : productionApiUrl;
}
