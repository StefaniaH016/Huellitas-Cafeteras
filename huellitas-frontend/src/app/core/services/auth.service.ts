import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private httpClient = inject(HttpClient);
  private tokenService = inject(TokenService);
  private router = inject(Router);

  login(email: string, password: string): Observable<string> {
    const body = { email, password };
    return this.httpClient
      .post<{ token: string }>(`${environment.apiUrl}/api/auth/login`, body)
      .pipe(
        tap((response) => {
          this.tokenService.setToken(response.token);
        }),
        map(() => {
          return (
            (this.tokenService.decodePayload()?.['rol'] ??
              this.tokenService.decodePayload()?.['role'] ??
              null) as string
          );
        })
      );
  }

  logout(): void {
    this.tokenService.removeToken();
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.tokenService.getToken();
  }

  getRol(): string | null {
    const payload = this.tokenService.decodePayload();
    return (
      (payload?.['rol'] ?? payload?.['role'] ?? null) as string | null
    );
  }
}
