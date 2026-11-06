import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EvolucionDetalleDTO,
  BitacoraDetalleDTO,
  BitacoraResponseDTO,
  EvolucionDTO,
  AltaMedicaDTO,
  AltaMedicaResponseDTO,
  RegistroMedicoResponseDTO,
  PacienteResponseDTO,
} from '../../../core/models/dtos';

/** Alias para compatibilidad. Usar EvolucionDetalleDTO desde dtos.ts */
export type Evolucion = EvolucionDetalleDTO;

/** Alias para compatibilidad. Usar BitacoraDetalleDTO desde dtos.ts */
export type Bitacora = BitacoraDetalleDTO;

/** Alias para compatibilidad. Usar BitacoraResponseDTO desde dtos.ts */
export type BitacoraAbierta = BitacoraResponseDTO;

/** Alias para compatibilidad. Usar EvolucionDTO desde dtos.ts */
export type NuevaEvolucion = EvolucionDTO;

/** Alias para compatibilidad. Usar AltaMedicaDTO desde dtos.ts */
export type AltaMedica = AltaMedicaDTO;

/** Alias para compatibilidad. Usar AltaMedicaResponseDTO desde dtos.ts */
export type AltaMedicaRegistrada = AltaMedicaResponseDTO;

/** Alias para compatibilidad. Usar RegistroMedicoResponseDTO desde dtos.ts */
export type RegistroMedico = RegistroMedicoResponseDTO;

@Injectable({
  providedIn: 'root',
})
export class BitacoraService {
  private httpClient = inject(HttpClient);

  private url(casoId: number, ruta = ''): string {
    return `${environment.apiUrl}/casos/${casoId}/bitacora${ruta}`;
  }

  getBitacora(casoId: number): Observable<Bitacora> {
    return this.httpClient.get<Bitacora>(this.url(casoId));
  }

  abrirBitacora(casoId: number, observacionInicial?: string): Observable<BitacoraAbierta> {
    return this.httpClient.post<BitacoraAbierta>(this.url(casoId), { observacionInicial });
  }

  agregarEvolucion(casoId: number, dto: NuevaEvolucion): Observable<RegistroMedico> {
    return this.httpClient.post<RegistroMedico>(this.url(casoId, '/evoluciones'), dto);
  }

  registrarAlta(casoId: number, dto: AltaMedica): Observable<AltaMedicaRegistrada> {
    return this.httpClient.patch<AltaMedicaRegistrada>(this.url(casoId, '/alta'), dto);
  }

  listarMisPacientes(): Observable<PacienteResponseDTO[]> {
    return this.httpClient.get<PacienteResponseDTO[]>(
      `${environment.apiUrl}/bitacoras/mis-pacientes`,
    );
  }

  /** El PDF se pide con el token (el interceptor lo agrega), por eso se descarga como Blob. */
  descargarPdf(casoId: number): Observable<Blob> {
    return this.httpClient.get(this.url(casoId, '/pdf'), { responseType: 'blob' });
  }
}
