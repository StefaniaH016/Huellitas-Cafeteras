import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UsuarioPerfil {
  id: number;
  nombre: string;
  correo: string;
  telefono?: string;
  rol: string;
  verificado: boolean;
  departamentoId?: number;
  municipioId?: number;
  barrioId?: number;
  organizacionId?: number;
  organizacionNombre?: string | null;
  estadoVerificacionOrganizacion?: string | null;
  aceptaCustodia?: boolean | null;
  capacidadAlbergue?: number | null;
  disponibleRescate: boolean;
}

export interface ActualizarPerfilRequest {
  nombre: string;
  telefono?: string;
  barrioId: number | null;
  disponibleRescate: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  getMiPerfil(): Observable<UsuarioPerfil> {
    return this.httpClient.get<UsuarioPerfil>(`${this.apiUrl}/me`);
  }

  actualizarPerfil(dto: ActualizarPerfilRequest): Observable<UsuarioPerfil> {
    return this.httpClient.put<UsuarioPerfil>(`${this.apiUrl}/me`, dto);
  }
}
