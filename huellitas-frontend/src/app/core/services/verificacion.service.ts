import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VerificacionPendiente {
  usuarioId: number;
  nombreEntidad: string;
  rolSolicitado: string;
  estado: string;
}

export type DecisionVerificacion = 'APROBAR' | 'RECHAZAR';

export interface VerificacionResponse {
  usuarioId: number;
  estado: string;
  verificado: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class VerificacionService {
  private httpClient = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getPendientes(): Observable<VerificacionPendiente[]> {
    return this.httpClient.get<VerificacionPendiente[]>(`${this.apiUrl}/verificaciones/pendientes`);
  }

  resolverVerificacion(
    usuarioId: number,
    dto: { decision: DecisionVerificacion; observacion?: string },
  ): Observable<VerificacionResponse> {
    return this.httpClient.patch<VerificacionResponse>(
      `${this.apiUrl}/verificaciones/${usuarioId}`,
      dto,
    );
  }
}
