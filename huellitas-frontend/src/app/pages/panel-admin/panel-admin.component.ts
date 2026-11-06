import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CasoListItem, CasoService } from '../../core/services/caso.service';
import {
  DecisionVerificacion,
  VerificacionPendiente,
  VerificacionService,
} from '../../core/services/verificacion.service';
import { CasoReportado, ModeracionService } from '../../core/services/moderacion.service';
import { pageContent, pageTotal, Page } from '../../core/utils/page.utils';
import { getPanelBadgeClass, getPanelBadgeLabel } from '../../core/utils/panel.utils';
import { CasoMiniaturaComponent } from '../../shared/components/caso-miniatura/caso-miniatura.component';

@Component({
  selector: 'app-panel-admin',
  imports: [CommonModule, RouterLink, CasoMiniaturaComponent],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css',
})
export class PanelAdminComponent implements OnInit, OnDestroy {
  private verificacionService = inject(VerificacionService);
  private moderacionService = inject(ModeracionService);
  private casoService = inject(CasoService);

  adminValidaciones = signal<VerificacionPendiente[]>([]);
  casosReportados = signal<CasoReportado[]>([]);
  casosSistema = signal<CasoListItem[]>([]);
  totalCasos = signal(0);
  paginaCasos = signal(0);
  cargandoCasos = signal(false);
  errorCasos = signal('');
  documentosSoporte = signal<Record<number, string>>({});
  documentoEnCurso = signal<number | null>(null);
  errorDocumento = signal('');
  cargando = signal(false);
  cargandoModeracion = signal(false);
  errorMensaje = signal('');
  errorModeracion = signal('');
  mensaje = signal('');
  accionEnCurso = signal<number | null>(null);
  selectedTab = signal<'validar' | 'moderacion' | 'casos'>('validar');

  selectTab(tab: 'validar' | 'moderacion' | 'casos'): void {
    this.selectedTab.set(tab);
  }

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarCasosReportados();
    this.cargarCasos();
  }

  ngOnDestroy(): void {
    Object.values(this.documentosSoporte()).forEach((url) => URL.revokeObjectURL(url));
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

  cargarCasosReportados(): void {
    this.cargandoModeracion.set(true);
    this.errorModeracion.set('');
    this.moderacionService.getCasosReportados().subscribe({
      next: (casos) => {
        this.casosReportados.set(casos);
        this.cargandoModeracion.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar los casos reportados:', error);
        this.errorModeracion.set('No fue posible cargar los casos reportados. Intenta nuevamente.');
        this.cargandoModeracion.set(false);
      },
    });
  }

  cargarCasos(pagina = this.paginaCasos()): void {
    this.cargandoCasos.set(true);
    this.errorCasos.set('');
    this.casoService.listarCasos({ page: pagina, size: 10 }).subscribe({
      next: (respuesta: Page<CasoListItem> | CasoListItem[]) => {
        this.casosSistema.set(pageContent(respuesta));
        this.totalCasos.set(pageTotal(respuesta));
        this.paginaCasos.set(pagina);
        this.cargandoCasos.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar los casos para administración:', error);
        this.errorCasos.set('No fue posible cargar los casos. Intenta nuevamente.');
        this.cargandoCasos.set(false);
      },
    });
  }

  cambiarPaginaCasos(delta: number): void {
    const pagina = this.paginaCasos() + delta;
    if (pagina < 0 || pagina * 10 >= this.totalCasos()) {
      return;
    }
    this.cargarCasos(pagina);
  }

  cargarDocumentoSoporte(usuarioId: number): void {
    this.documentoEnCurso.set(usuarioId);
    this.errorDocumento.set('');
    this.verificacionService.obtenerDocumentoSoporte(usuarioId).subscribe({
      next: (documento) => {
        const anterior = this.documentosSoporte()[usuarioId];
        if (anterior) {
          URL.revokeObjectURL(anterior);
        }
        this.documentosSoporte.update((urls) => ({
          ...urls,
          [usuarioId]: URL.createObjectURL(documento),
        }));
        this.documentoEnCurso.set(null);
      },
      error: (error: unknown) => {
        console.error('Error al cargar el documento de soporte:', error);
        this.errorDocumento.set('No fue posible abrir el documento. Intenta nuevamente.');
        this.documentoEnCurso.set(null);
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

  getBadgeClass = getPanelBadgeClass;
  getBadgeLabel = getPanelBadgeLabel;
}
