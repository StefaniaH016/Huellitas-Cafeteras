import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

interface LoginResponse {
  token: string;
  rol?: string;
  role?: string;
}

interface RegistroResponse {
  id?: string;
  correo: string;
  nombre: string;
  mensaje?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private httpClient = inject(HttpClient);
  private tokenService = inject(TokenService);
  private router = inject(Router);
  private readonly rolKey = 'hc_rol';

  login(email: string, password: string): Observable<string> {
    const body = { correo: email, contrasena: password };
    return this.httpClient.post<LoginResponse>(`${environment.apiUrl}/auth/login`, body).pipe(
      tap((response) => {
        this.tokenService.setToken(response.token);
        // Store rol from response, fallback to token payload
        const rol =
          response.rol ?? response.role ?? this.tokenService.decodePayload()?.['rol'] ?? null;
        if (rol) {
          localStorage.setItem(this.rolKey, rol as string);
        }
      }),
      map((response) => {
        return (response.rol ??
          response.role ??
          this.tokenService.decodePayload()?.['rol'] ??
          null) as string;
      }),
    );
  }

  logout(): void {
    this.tokenService.removeToken();
    localStorage.removeItem(this.rolKey);
    this.router.navigate(['/login']);
  }

  registro(correo: string, contrasena: string, nombre: string): Observable<RegistroResponse> {
    const body = { correo, contrasena, nombre };
    return this.httpClient.post<RegistroResponse>(`${environment.apiUrl}/auth/registro`, body);
  }

  isLoggedIn(): boolean {
    return !!this.tokenService.getToken();
  }

  getRol(): string | null {
    // First try to get from stored rol (from login response)
    const storedRol = localStorage.getItem(this.rolKey);
    if (storedRol) {
      return storedRol;
    }
    // Fallback to token payload (if backend later adds rol as claim)
    const payload = this.tokenService.decodePayload();
    return (payload?.['rol'] ?? null) as string | null;
  }

  /**
   * Navigate to role-specific dashboard after successful login.
   * Centralizes redirect logic for use across components and guards.
   */
  navigateAfterLogin(rol: string): void {
    switch (rol) {
      case 'FUNDACION':
        this.router.navigate(['/fundacion']);
        break;
      case 'VETERINARIA':
        this.router.navigate(['/veterinaria']);
        break;
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      default:
        this.router.navigate(['/']);
    }
  }
}
