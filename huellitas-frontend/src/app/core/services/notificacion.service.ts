import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, compactParams } from '../utils/page.utils';
import { NotificacionDTO } from '../../../core/models/dtos';

/** Alias para compatibilidad. Usar NotificacionDTO desde dtos.ts */
export type Notificacion = NotificacionDTO;

@Injectable({
  providedIn: 'root',
})
export class NotificacionService {
  private httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/notificaciones`;

  /** Notificaciones sin leer; el header muestra este contador. */
  readonly sinLeer = signal(0);

  /** soloSinLeer usa ?leidas=false, que el backend traduce a "solo no leídas". */
  listar(soloSinLeer = false, page = 0, size = 30): Observable<Page<Notificacion>> {
    return this.httpClient.get<Page<Notificacion>>(this.apiUrl, {
      params: compactParams({ leidas: soloSinLeer ? false : null, page, size }),
    });
  }

  /** Actualiza el contador usando el endpoint de conteo del usuario autenticado. */
  actualizarContador(): void {
    this.httpClient.get<number>(`${this.apiUrl}/no-leidas/count`).subscribe({
      next: (count) => this.sinLeer.set(count),
      error: (error: unknown) =>
        console.error('Error cargando el contador de notificaciones:', error),
    });
  }

  marcarLeida(id: number): Observable<Notificacion> {
    return this.httpClient
      .patch<Notificacion>(`${this.apiUrl}/${id}/leer`, {})
      .pipe(tap(() => this.sinLeer.update((n) => Math.max(0, n - 1))));
  }
}
