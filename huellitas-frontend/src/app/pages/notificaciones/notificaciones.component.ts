import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Notificacion, NotificacionService } from '../../core/services/notificacion.service';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { haceCuanto } from '../../core/utils/caso-display.utils';
import { pageContent } from '../../core/utils/page.utils';
import { EstadoCarga, getServerErrorMessage } from '../../core/utils/request.utils';

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
  private usuarioService = inject(UsuarioService);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly carga = new EstadoCarga();
  readonly perfilCarga = new EstadoCarga();
  readonly guardandoPreferencia = signal(false);
  readonly soloSinLeer = signal(false);
  readonly notificaciones = signal<Notificacion[]>([]);
  readonly perfil = signal<UsuarioPerfil | null>(null);
  readonly disponibleRescate = signal(false);
  readonly errorPreferencia = signal('');
  readonly mensajePreferencia = signal('');
  readonly sinLeer = this.notificacionService.sinLeer;
  readonly haceCuanto = haceCuanto;

  ngOnInit(): void {
    this.cargar();
    if (!this.isAdmin()) {
      this.perfilCarga.ejecutar(
        this.usuarioService.getMiPerfil(),
        (perfil) => {
          this.perfil.set(perfil);
          this.disponibleRescate.set(perfil.disponibleRescate);
        },
        'No fue posible cargar tu preferencia de rescate.',
      );
    }
  }

  cambiarDisponibilidadRescate(event: Event): void {
    const disponible = (event.target as HTMLInputElement).checked;
    const perfil = this.perfil();
    if (!perfil) {
      return;
    }

    this.errorPreferencia.set('');
    this.mensajePreferencia.set('');
    const disponibleAnterior = this.disponibleRescate();
    this.disponibleRescate.set(disponible);
    this.guardandoPreferencia.set(true);
    this.usuarioService
      .actualizarPerfil({
        nombre: perfil.nombre,
        telefono: perfil.telefono,
        barrioId: perfil.barrioId ?? null,
        disponibleRescate: disponible,
      })
      .subscribe({
        next: (perfilActualizado) => {
          this.perfil.set(perfilActualizado);
          this.disponibleRescate.set(perfilActualizado.disponibleRescate);
          this.mensajePreferencia.set('Preferencia actualizada.');
          this.guardandoPreferencia.set(false);
        },
        error: (error: unknown) => {
          console.error('Error al actualizar la preferencia de rescate:', error);
          this.disponibleRescate.set(disponibleAnterior);
          this.errorPreferencia.set(
            getServerErrorMessage(error) ?? 'No fue posible actualizar tu preferencia de rescate.',
          );
          this.guardandoPreferencia.set(false);
        },
      });
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
