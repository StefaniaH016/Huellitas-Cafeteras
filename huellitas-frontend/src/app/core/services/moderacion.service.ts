import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface CasoReportado {
  id: number;
  descripcion: string;
  estado: string;
}

interface ResolucionDto {
  aprobado: boolean;
  razon?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ModeracionService {
  private httpClient = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getCasosReportados(): Observable<{ casos: CasoReportado[] }> {
    return this.httpClient.get<{ casos: CasoReportado[] }>(
      `${this.apiUrl}/moderacion/casos-reportados`,
    );
  }

  resolverModeración(casoId: number, dto: ResolucionDto): Observable<void> {
    return this.httpClient.post<void>(`${this.apiUrl}/moderacion/${casoId}/resolver`, dto);
  }
}
