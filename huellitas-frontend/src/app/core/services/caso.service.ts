import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BitacoraService } from './bitacora.service';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

@Injectable({
  providedIn: 'root',
})
export class CasoService {
  private httpClient = inject(HttpClient);
  private bitacoraService = inject(BitacoraService);
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

  actualizarReporte(id: number, dto: ApiResponse): Observable<ApiResponse> {
    return this.httpClient.put<ApiResponse>(`${this.apiUrl}/${id}`, dto);
  }

  eliminarCaso(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/${id}`);
  }

  eliminarFoto(casoId: number, fotoId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/${casoId}/fotos/${fotoId}`);
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
    return this.bitacoraService.getBitacora(casoId);
  }

  abrirBitacora(casoId: number, dto?: ApiResponse): Observable<ApiResponse> {
    return this.bitacoraService.abrirBitacora(casoId, dto);
  }

  agregarEvolucion(casoId: number, dto: ApiResponse): Observable<ApiResponse> {
    return this.bitacoraService.agregarEvolucion(casoId, dto);
  }

  registrarAlta(casoId: number, dto: ApiResponse): Observable<ApiResponse> {
    return this.bitacoraService.registrarAlta(casoId, dto);
  }
}
