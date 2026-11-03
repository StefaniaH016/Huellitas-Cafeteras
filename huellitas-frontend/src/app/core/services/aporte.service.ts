import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AporteResponseDTO, CrearAporteDTO, OcultarAporteDTO } from '../../../core/models/dtos';

/** Alias para compatibilidad. Usar AporteResponseDTO desde dtos.ts */
export type Aporte = AporteResponseDTO;

/** Alias para compatibilidad. Usar CrearAporteDTO desde dtos.ts */
export type CrearAporte = CrearAporteDTO;

@Injectable({
  providedIn: 'root',
})
export class AporteService {
  private httpClient = inject(HttpClient);

  private url(casoId: number): string {
    return `${environment.apiUrl}/casos/${casoId}/aportes`;
  }

  listar(casoId: number): Observable<Aporte[]> {
    return this.httpClient.get<Aporte[]>(this.url(casoId));
  }

  crear(casoId: number, dto: CrearAporte): Observable<Aporte> {
    return this.httpClient.post<Aporte>(this.url(casoId), dto);
  }

  ocultarAporte(id: number, motivo: string): Observable<Aporte> {
    const dto: OcultarAporteDTO = { motivo };
    return this.httpClient.patch<Aporte>(
      `${environment.apiUrl}/moderacion/aportes/${id}/ocultar`,
      dto,
    );
  }
}
