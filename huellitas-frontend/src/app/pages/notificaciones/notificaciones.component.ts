import { Component, OnInit, computed, inject, signal } from '@angular/core';
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
  EVOLUCION_MEDICA: '🩺',
  ALTA_MEDICA: '🎉',
  VERIFICACION: '✅',
  CUENTAS: '🏢',
  MODERACION: '⚑',
  INACTIVOS: '⏰',
};

type FiltroNotificaciones =
  | 'todas'
  | 'sin-leer'
  | 'mis-casos'
  | 'mi-zona'
  | 'recordatorios'
  | 'cuentas'
  | 'moderacion'
  | 'sin-actividad';

/** Notificaciones: filtros locales sobre los avisos cargados; abrir una la marca como leída. */
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
  readonly skeletonNotifications = [1, 2, 3, 4];
  readonly filtroActivo = signal<FiltroNotificaciones>('todas');
  readonly notificaciones = signal<Notificacion[]>([]);
  readonly notificacionesFiltradas = computed(() =>
    this.aplicarFiltro(this.notificaciones(), this.filtroActivo()),
  );
  readonly contadores = computed(() => {
    const lista = this.notificaciones();
    return {
      todas: lista.length,
      misCasos: this.aplicarFiltro(lista, 'mis-casos').length,
      miZona: this.aplicarFiltro(lista, 'mi-zona').length,
      recordatorios: this.aplicarFiltro(lista, 'recordatorios').length,
      cuentas: this.aplicarFiltro(lista, 'cuentas').length,
      moderacion: this.aplicarFiltro(lista, 'moderacion').length,
      sinActividad: this.aplicarFiltro(lista, 'sin-actividad').length,
    };
  });
  readonly nombreFiltroActivo = computed(() => {
    const nombres: Record<FiltroNotificaciones, string> = {
      todas: 'notificaciones',
      'sin-leer': 'notificaciones sin leer',
      'mis-casos': 'notificaciones de mis casos',
      'mi-zona': 'emergencias de mi zona',
      recordatorios: 'recordatorios',
      cuentas: 'notificaciones de cuentas',
      moderacion: 'notificaciones de moderación',
      'sin-actividad': 'avisos de casos sin actividad',
    };
    return nombres[this.filtroActivo()];
  });
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

  filtrar(filtro: FiltroNotificaciones | boolean): void {
    this.filtroActivo.set(typeof filtro === 'boolean' ? (filtro ? 'sin-leer' : 'todas') : filtro);
  }

  cargar(): void {
    this.carga.ejecutar(this.notificacionService.listar(false), (page) =>
      this.notificaciones.set(pageContent(page)),
    );
    this.notificacionService.actualizarContador();
  }

  private aplicarFiltro(lista: Notificacion[], filtro: FiltroNotificaciones): Notificacion[] {
    switch (filtro) {
      case 'sin-leer':
        return lista.filter((notificacion) => !notificacion.leida);
      case 'mis-casos':
        return lista.filter(
          (notificacion) =>
            notificacion.casoId !== null &&
            notificacion.casoId !== undefined &&
            !this.esAlertaDeZona(notificacion),
        );
      case 'mi-zona':
        return lista.filter((notificacion) => this.esAlertaDeZona(notificacion));
      case 'recordatorios':
        return lista.filter((notificacion) => notificacion.tipo === 'RECORDATORIO');
      case 'cuentas':
        return lista.filter((notificacion) =>
          ['VERIFICACION', 'CUENTAS'].includes(notificacion.tipo),
        );
      case 'moderacion':
        return lista.filter((notificacion) => {
          const mensaje = this.normalizarTexto(notificacion.mensaje);
          return notificacion.tipo === 'MODERACION' || mensaje.includes('moderacion');
        });
      case 'sin-actividad':
        return lista.filter((notificacion) => {
          const mensaje = this.normalizarTexto(notificacion.mensaje);
          return (
            notificacion.tipo === 'INACTIVOS' ||
            mensaje.includes('sin actividad') ||
            mensaje.includes('inactivo')
          );
        });
      case 'todas':
        return lista;
    }
  }

  private esAlertaDeZona(notificacion: Notificacion): boolean {
    return (
      notificacion.tipo === 'EMERGENCIA' &&
      this.normalizarTexto(notificacion.mensaje).includes('emergencia en tu zona')
    );
  }

  private normalizarTexto(texto: string): string {
    return texto
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
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
    void this.router.navigate(['/home']);
  }
}
