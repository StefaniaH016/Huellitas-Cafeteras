import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

@Injectable({
  providedIn: 'root',
})
export class BitacoraService {
  private httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/casos`;

  getBitacora(casoId: number): Observable<ApiResponse> {
    return this.httpClient.get<ApiResponse>(`${this.apiUrl}/${casoId}/bitacora`);
  }

  abrirBitacora(casoId: number, dto?: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.post<ApiResponse>(`${this.apiUrl}/${casoId}/bitacora`, dto || {});
  }

  agregarEvolucion(casoId: number, dto: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.post<ApiResponse>(`${this.apiUrl}/${casoId}/bitacora/evoluciones`, dto);
  }

  registrarAlta(casoId: number, dto: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.patch<ApiResponse>(`${this.apiUrl}/${casoId}/bitacora/alta`, dto);
  }

  getPdfUrl(casoId: number): string {
    return `${this.apiUrl}/${casoId}/bitacora/pdf`;
  }
}
