import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VerificacionPendiente {
  usuarioId: number;
  nombreEntidad: string;
  rolSolicitado: string;
  estado: string;
  nit?: string | null;
  municipio?: string | null;
  documentoSoporteNombre?: string | null;
  documentoSoporteDisponible: boolean;
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

  solicitarRevision(
    documentoSoporte: File,
    organizacion?: { nombreEntidad: string; nit: string; municipioOperacion: string },
  ): Observable<VerificacionResponse> {
    const formData = new FormData();
    formData.append('documentoSoporte', documentoSoporte, documentoSoporte.name);
    if (organizacion) {
      formData.append('nombreEntidad', organizacion.nombreEntidad);
      formData.append('nit', organizacion.nit);
      formData.append('municipioOperacion', organizacion.municipioOperacion);
    }
    return this.httpClient.post<VerificacionResponse>(
      `${this.apiUrl}/verificaciones/solicitar-revision`,
      formData,
    );
  }

  obtenerDocumentoSoporte(usuarioId: number): Observable<Blob> {
    return this.httpClient.get(`${this.apiUrl}/verificaciones/${usuarioId}/documento-soporte`, {
      responseType: 'blob',
    });
  }
}
