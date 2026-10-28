import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  DecisionVerificacion,
  VerificacionPendiente,
  VerificacionService,
} from '../../core/services/verificacion.service';

@Component({
  selector: 'app-panel-admin',
  imports: [CommonModule],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css',
})
export class PanelAdminComponent implements OnInit {
  private verificacionService = inject(VerificacionService);

  adminValidaciones = signal<VerificacionPendiente[]>([]);
  cargando = signal(false);
  errorMensaje = signal('');
  mensaje = signal('');
  accionEnCurso = signal<number | null>(null);

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');
    this.verificacionService.getPendientes().subscribe({
      next: (verificaciones) => {
        this.adminValidaciones.set(verificaciones);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar las cuentas por validar:', error);
        this.errorMensaje.set('No fue posible cargar las cuentas. Intenta nuevamente.');
        this.cargando.set(false);
      },
    });
  }

  resolverVerificacion(usuarioId: number, decision: DecisionVerificacion): void {
    this.accionEnCurso.set(usuarioId);
    this.errorMensaje.set('');
    this.mensaje.set('');
    this.verificacionService.resolverVerificacion(usuarioId, { decision }).subscribe({
      next: () => {
        this.accionEnCurso.set(null);
        this.mensaje.set(
          decision === 'APROBAR'
            ? 'La cuenta fue aprobada correctamente.'
            : 'La cuenta fue rechazada correctamente.',
        );
        this.cargarDatos();
      },
      error: (error: unknown) => {
        console.error('Error al resolver la verificación:', error);
        this.accionEnCurso.set(null);
        this.errorMensaje.set('No fue posible resolver la verificación. Intenta nuevamente.');
      },
    });
  }
}
