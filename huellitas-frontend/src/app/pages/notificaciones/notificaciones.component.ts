import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Notificacion, NotificacionService } from '../../core/services/notificacion.service';
import { AuthService } from '../../core/services/auth.service';
import { haceCuanto } from '../../core/utils/caso-display.utils';
import { pageContent } from '../../core/utils/page.utils';
import { EstadoCarga } from '../../core/utils/request.utils';

/** Ícono de cada tipo de notificación del backend. */
const ICONOS: Record<string, string> = {
  EMERGENCIA: '🚨',
  CAMBIO_ESTADO: '🔄',
  CIERRE: '✅',
  RECORDATORIO: '⏰',
  CUSTODIA: '🏠',
  VERIFICACION: '✅',
  CUENTAS: '🏢',
  MODERACION: '⚑',
  INACTIVOS: '⏰',
};

/** Notificaciones: todas o solo las sin leer; abrir una la marca como leída y lleva al caso. */
@Component({
  selector: 'app-notificaciones',
  templateUrl: './notificaciones.component.html',
  standalone: true,
  imports: [CommonModule, RouterLink],
})
export class NotificacionesComponent implements OnInit {
  private notificacionService = inject(NotificacionService);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly carga = new EstadoCarga();
  readonly soloSinLeer = signal(false);
  readonly notificaciones = signal<Notificacion[]>([]);
  readonly sinLeer = this.notificacionService.sinLeer;
  readonly haceCuanto = haceCuanto;

  ngOnInit(): void {
    this.cargar();
  }

  filtrar(soloSinLeer: boolean): void {
    this.soloSinLeer.set(soloSinLeer);
    this.cargar();
  }

  cargar(): void {
    this.carga.ejecutar(this.notificacionService.listar(this.soloSinLeer()), (page) =>
      this.notificaciones.set(pageContent(page)),
    );
    this.notificacionService.actualizarContador();
  }

  icono(tipo: string): string {
    return ICONOS[tipo] ?? '🔔';
  }

  abrir(notificacion: Notificacion): void {
    const ir = (): void => {
      if (notificacion.casoId) {
        void this.router.navigate(['/caso-detalle', notificacion.casoId]);
      }
    };
    if (notificacion.leida) {
      ir();
      return;
    }
    this.notificacionService.marcarLeida(notificacion.id).subscribe({
      next: () => {
        this.notificaciones.update((lista) =>
          lista.map((n) => (n.id === notificacion.id ? { ...n, leida: true } : n)),
        );
        ir();
      },
      error: (err: unknown) => console.error('Error marcando la notificación:', err),
    });
  }

  isAdmin(): boolean {
    return this.authService.tieneRol('ADMIN');
  }

  goBack(): void {
    void this.router.navigate(['/']);
  }
}
