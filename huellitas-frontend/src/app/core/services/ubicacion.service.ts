import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Departamento {
  id: number;
  nombre: string;
}

export interface Municipio {
  id: number;
  nombre: string;
  departamentoId: number;
}

export interface Barrio {
  id: number;
  nombre: string;
  municipioId: number;
}

@Injectable({
  providedIn: 'root',
})
export class UbicacionService {
  private httpClient = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/ubicaciones`;

  getDepartamentos(): Observable<Departamento[]> {
    return this.httpClient.get<Departamento[]>(`${this.apiUrl}/departamentos`);
  }

  getMunicipios(departamentoId: number): Observable<Municipio[]> {
    return this.httpClient.get<Municipio[]>(
      `${this.apiUrl}/departamentos/${departamentoId}/municipios`,
    );
  }

  getBarrios(municipioId: number): Observable<Barrio[]> {
    return this.httpClient.get<Barrio[]>(`${this.apiUrl}/municipios/${municipioId}/barrios`);
  }
}
