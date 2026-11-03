import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';
import { normalizeRole } from '../utils/role.utils';

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
    void this.router.navigate(['/login']);
  }

  registro(request: {
    nombre: string;
    correo: string;
    contrasena: string;
    telefono: string;
    barrioId?: number;
    rol: string;
    nombreEntidad?: string;
    nit?: string;
    documentoLegal?: string;
    municipioOperacion?: string;
    documentoSoporte?: string;
    aceptoTratamientoDatos: boolean;
  }): Observable<RegistroResponse> {
    const body = {
      correo: request.correo,
      contrasena: request.contrasena,
      nombre: request.nombre,
      rol: request.rol,
      aceptoTratamientoDatos: request.aceptoTratamientoDatos,
      ...(request.telefono && { telefono: request.telefono }),
      ...(request.barrioId && { barrioId: request.barrioId }),
      ...(request.nombreEntidad && { nombreEntidad: request.nombreEntidad }),
      ...(request.nit && { nit: request.nit }),
      ...(request.documentoLegal && { documentoLegal: request.documentoLegal }),
      ...(request.municipioOperacion && { municipioOperacion: request.municipioOperacion }),
      ...(request.documentoSoporte && { documentoSoporte: request.documentoSoporte }),
    };
    return this.httpClient.post<RegistroResponse>(`${environment.apiUrl}/auth/registro`, body);
  }

  isLoggedIn(): boolean {
    return !!this.tokenService.getToken();
  }

  getRol(): string | null {
    // First try to get from stored rol (from login response)
    let storedRol = localStorage.getItem(this.rolKey);
    if (!storedRol) {
      // Fallback to token payload (if backend later adds rol as claim)
      const payload = this.tokenService.decodePayload();
      storedRol = (payload?.['rol'] ?? null) as string | null;
    }

    // Normalize the role (handles both legacy short forms and current full enum names)
    const normalized = normalizeRole(storedRol);
    if (normalized) {
      return normalized;
    }

    // If normalization fails, return null and log warning
    if (storedRol) {
      console.warn(`[AuthService] Unable to normalize role: "${storedRol}"`);
    }
    return null;
  }

  /**
   * Navigate to /home for all roles after successful login.
   * All roles land on /home with role-specific dashboards.
   */
  navigateAfterLogin(): void {
    void this.router.navigate(['/home']);
  }

  getNombre(): string | null {
    const payload = this.tokenService.decodePayload();
    return (payload?.['nombre'] ?? null) as string | null;
  }

  tieneRol(rol: string): boolean {
    const rolActual = this.getRol();
    return rolActual === rol || rolActual === normalizeRole(rol);
  }
}
