import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { VerificacionService } from '../../core/services/verificacion.service';
import { ModeracionService } from '../../core/services/moderacion.service';
import { CasoService } from '../../core/services/caso.service';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

type AdminSection =
  'resumen' | 'validaciones' | 'moderacion' | 'usuarios' | 'casos' | 'catalogos' | 'reportes';

@Component({
  selector: 'app-panel-admin',
  imports: [CommonModule, TitleCasePipe],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css',
})
export class PanelAdminComponent implements OnInit {
  private verificacionService = inject(VerificacionService);
  private moderacionService = inject(ModeracionService);
  private casoService = inject(CasoService);

  activeSection = signal<AdminSection>('resumen');
  verificacionesPendientes = signal<ApiResponse[]>([]);
  casosReportados = signal<ApiResponse[]>([]);
  casosActivos = signal<ApiResponse[]>([]);
  cargando = signal(false);
  errorMensaje = signal('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  setSection(section: AdminSection): void {
    this.activeSection.set(section);
    this.cargarSiNecesario(section);
  }

  private cargarSiNecesario(section: AdminSection): void {
    if (
      (section === 'resumen' || section === 'validaciones') &&
      this.verificacionesPendientes().length === 0 &&
      !this.cargando()
    ) {
      this.cargarVerificaciones();
    } else if (
      (section === 'resumen' || section === 'moderacion') &&
      this.casosReportados().length === 0 &&
      !this.cargando()
    ) {
      this.cargarModeración();
    } else if (section === 'casos' && this.casosActivos().length === 0 && !this.cargando()) {
      this.cargarCasosActivos();
    }
  }

  cargarDatos(): void {
    this.cargarVerificaciones();
    this.cargarModeración();
    this.cargarCasosActivos();
  }

  cargarVerificaciones(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    this.verificacionService.getPendientes().subscribe({
      next: (response: ApiResponse) => {
        this.verificacionesPendientes.set(
          response.verificaciones || response.content || response || [],
        );
        this.cargando.set(false);
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar verificaciones:', error);
        this.errorMensaje.set('Error al cargar verificaciones pendientes.');
        this.cargando.set(false);
      },
    });
  }

  cargarModeración(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    this.moderacionService.getCasosReportados().subscribe({
      next: (response: ApiResponse) => {
        this.casosReportados.set(response.casos || response.content || response || []);
        this.cargando.set(false);
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar casos reportados:', error);
        this.errorMensaje.set('Error al cargar casos reportados.');
        this.cargando.set(false);
      },
    });
  }

  cargarCasosActivos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    this.casoService.listarCasos().subscribe({
      next: (response: ApiResponse) => {
        this.casosActivos.set(response.casos || response.content || response || []);
        this.cargando.set(false);
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar casos activos:', error);
        this.errorMensaje.set('Error al cargar casos activos.');
        this.cargando.set(false);
      },
    });
  }

  aprobarVerificacion(usuarioId: number): void {
    const dto = { aprobado: true };
    this.verificacionService.resolverVerificacion(usuarioId, dto).subscribe({
      next: () => {
        this.cargarVerificaciones();
      },
      error: (error: ApiResponse) => {
        console.error('Error al aprobar verificación:', error);
        this.errorMensaje.set('Error al aprobar la verificación.');
      },
    });
  }

  rechazarVerificacion(usuarioId: number): void {
    const dto = { aprobado: false };
    this.verificacionService.resolverVerificacion(usuarioId, dto).subscribe({
      next: () => {
        this.cargarVerificaciones();
      },
      error: (error: ApiResponse) => {
        console.error('Error al rechazar verificación:', error);
        this.errorMensaje.set('Error al rechazar la verificación.');
      },
    });
  }
}
