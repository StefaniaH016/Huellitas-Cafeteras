import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

@Injectable({
  providedIn: 'root',
})
export class CasoService {
  private httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/casos`;

  getFeed(page = 0, size = 10): Observable<ApiResponse> {
    return this.httpClient.get<ApiResponse>(`${this.apiUrl}`, {
      params: { page, size },
    });
  }

  listarCasos(filtros?: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.get<ApiResponse>(`${this.apiUrl}`, { params: filtros });
  }

  getMisReportes(page = 0): Observable<ApiResponse> {
    return this.httpClient.get<ApiResponse>(`${this.apiUrl}/mis-reportes`, {
      params: { page },
    });
  }

  getDetalle(id: number): Observable<ApiResponse> {
    return this.httpClient.get<ApiResponse>(`${this.apiUrl}/${id}`);
  }

  crearReporte(dto: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.post<ApiResponse>(`${this.apiUrl}`, dto);
  }

  subirImagen(archivo: File, casoId?: string | number): Observable<ApiResponse> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    const params: Record<string, string> = {};
    if (casoId !== undefined && casoId !== null) {
      params['casoId'] = casoId.toString();
    }
    return this.httpClient.post<ApiResponse>(`${environment.apiUrl}/imagenes`, formData, {
      params,
    });
  }

  actualizarEstado(id: number, dto: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.patch<ApiResponse>(`${this.apiUrl}/${id}/estado`, dto);
  }

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
}
