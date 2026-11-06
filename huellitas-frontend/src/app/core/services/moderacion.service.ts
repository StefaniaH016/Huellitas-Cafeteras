import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CasoReportado {
  casoId: number;
  motivo: string;
  estadoRevision: string;
  fechaCreacion: string;
}

export type DecisionModeracion = 'MANTENER' | 'OCULTAR';

export interface DecisionModeracionDTO {
  decision: DecisionModeracion;
  motivo: string;
}

export interface ReportarCasoDTO {
  motivo: string;
}

export interface ModeracionResponse {
  casoId: number;
  decision: string;
  fechaRevision: string;
}

@Injectable({
  providedIn: 'root',
})
export class ModeracionService {
  private httpClient = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getCasosReportados(): Observable<CasoReportado[]> {
    return this.httpClient.get<CasoReportado[]>(`${this.apiUrl}/moderacion/casos`);
  }

  reportarCaso(casoId: number, dto: ReportarCasoDTO): Observable<ModeracionResponse> {
    return this.httpClient.post<ModeracionResponse>(
      `${this.apiUrl}/moderacion/casos/${casoId}/reportar`,
      dto,
    );
  }

  resolverModeracion(casoId: number, dto: DecisionModeracionDTO): Observable<ModeracionResponse> {
    return this.httpClient.patch<ModeracionResponse>(
      `${this.apiUrl}/moderacion/casos/${casoId}`,
      dto,
    );
  }
}
