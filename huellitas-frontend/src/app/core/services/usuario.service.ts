import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  getMiPerfil(): Observable<ApiResponse> {
    return this.httpClient.get<ApiResponse>(`${this.apiUrl}/me`);
  }

  actualizarPerfil(dto: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.put<ApiResponse>(`${this.apiUrl}/me`, dto);
  }
}
