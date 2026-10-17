import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface VerificacionPendiente {
  id: number;
  usuarioId: number;
  estado: string;
}

interface ResolucionVerificacionDto {
  aprobado: boolean;
  motivo?: string;
}

@Injectable({
  providedIn: 'root',
})
export class VerificacionService {
  private httpClient = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getPendientes(): Observable<{ verificaciones: VerificacionPendiente[] }> {
    return this.httpClient.get<{ verificaciones: VerificacionPendiente[] }>(
      `${this.apiUrl}/verificaciones/pendientes`,
    );
  }

  resolverVerificacion(usuarioId: number, dto: ResolucionVerificacionDto): Observable<void> {
    return this.httpClient.post<void>(`${this.apiUrl}/verificaciones/${usuarioId}/resolver`, dto);
  }
}
