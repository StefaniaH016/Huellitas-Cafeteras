import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  private readonly tokenKey = 'hc_token';

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  removeToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  decodePayload(): Record<string, unknown> | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }

      let b64 = parts[1];
      // Fix base64 padding
      b64 = b64.replace(/-/g, '+').replace(/_/g, '/');
      b64 = b64.padEnd(b64.length + (4 - (b64.length % 4)) % 4, '=');

      const payload = JSON.parse(atob(b64));
      return payload;
    } catch {
      return null;
    }
  }
}
