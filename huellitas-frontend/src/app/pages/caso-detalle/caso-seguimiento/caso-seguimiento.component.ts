import { CommonModule } from '@angular/common';
import { Component, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AporteService } from '../../../core/services/aporte.service';
import { AuthService } from '../../../core/services/auth.service';
import { CasoDetalle, CasoService } from '../../../core/services/caso.service';
import {
  APORTE_LABELS,
  ESTADO_LABELS,
  esCasoAbierto,
  formatFechaHora,
  labelOf,
} from '../../../core/utils/caso-display.utils';
import { EstadoAccion, EstadoCarga } from '../../../core/utils/request.utils';
import { FotoPickerComponent } from '../../../shared/components/foto-picker/foto-picker.component';
import { UbicacionSelectorComponent } from '../../../shared/components/ubicacion-selector/ubicacion-selector.component';

interface ItemActividad {
  clave: string;
  icono: string;
  titulo: string;
  aporteId?: number;
  texto?: string;
  zona?: string;
  referencia?: string;
  fotografias?: string[];
  cambio?: string;
  fecha: string;
}

const MIN_APORTE = 10;
const MAX_APORTE = 1000;

/** Pestaña Seguimiento: aportes de la comunidad y cambios de estado, del más reciente al más viejo. */
@Component({
  selector: 'app-caso-seguimiento',
  imports: [CommonModule, FormsModule, FotoPickerComponent, UbicacionSelectorComponent],
  templateUrl: './caso-seguimiento.component.html',
})
export class CasoSeguimientoComponent {
  private aporteService = inject(AporteService);
  private casoService = inject(CasoService);
  protected readonly authService = inject(AuthService);

  readonly caso = input.required<CasoDetalle>();
  readonly usuarioId = input<number | null>(null);
  readonly organizacionId = input<number | null>(null);
  readonly actualizado = output<void>();

  private readonly picker = viewChild.required(FotoPickerComponent);

  readonly maxAporte = MAX_APORTE;
  readonly envio = new EstadoCarga();
  readonly moderacionAporte = new EstadoAccion();
  readonly aporteSeleccionado = signal<number | null>(null);
  readonly motivoOcultacion = signal('');
  readonly tipo = signal<'COMENTARIO' | 'AVISTAMIENTO'>('COMENTARIO');
  readonly fotos = signal<File[]>([]);
  readonly deptoId = signal<number | null>(null);
  readonly munId = signal<number | null>(null);
  readonly barrioId = signal<number | null>(null);
  readonly publicado = signal(false);
  descripcion = '';
  ubicacionTexto = '';

  readonly puedeAportar = computed(() => {
    const caso = this.caso();
    if (!esCasoAbierto(caso.estado)) {
      return false;
    }
    if (
      this.authService.tieneRol('CIUDADANO_VOLUNTARIO') &&
      (this.usuarioId() !== caso.autorId || !caso.fundacionCustodioId)
    ) {
      return true;
    }
    return (
      this.authService.tieneRol('FUNDACION_REFUGIO') &&
      this.organizacionId() !== null &&
      this.organizacionId() === caso.fundacionCustodioId
    );
  });
  readonly esAdministrador = computed(() => this.authService.tieneRol('ADMINISTRADOR'));

  readonly actividad = computed<ItemActividad[]>(() => {
    const caso = this.caso();
    const aportes = (caso.aportes ?? []).map((a: Record<string, unknown>) => {
      const cambioEstado =
        a['tipo'] === 'ACTUALIZACION'
          ? caso.historial?.find(
              (h: Record<string, unknown>) => h['observacion'] === a['comentario'],
            )
          : undefined;
      return {
        clave: `a${a['id']}`,
        icono: a['tipo'] === 'AVISTAMIENTO' ? '👀' : '💬',
        titulo: `${a['autorNombre'] ?? 'Alguien'} · ${labelOf(APORTE_LABELS, a['tipo'] as string)}`,
        aporteId:
          (a['tipo'] === 'COMENTARIO' || a['tipo'] === 'AVISTAMIENTO') &&
          typeof a['id'] === 'number'
            ? a['id']
            : undefined,
        texto: a['comentario'] as string | undefined,
        zona: a['zona'] as string | undefined,
        referencia: a['ubicacionTexto'] as string | undefined,
        fotografias: a['fotografias'] as string[] | undefined,
        cambio: cambioEstado
          ? `${labelOf(ESTADO_LABELS, cambioEstado['estadoAnterior'] as string)} → ${labelOf(ESTADO_LABELS, cambioEstado['estadoNuevo'] as string)}`
          : undefined,
        fecha: a['fecha'] as string,
      };
    });
    const cambios = (caso.historial ?? [])
      .filter(
        (h: Record<string, unknown>) =>
          !(caso.aportes ?? []).some(
            (a: Record<string, unknown>) =>
              a['tipo'] === 'ACTUALIZACION' && a['comentario'] === h['observacion'],
          ),
      )
      .map((h: Record<string, unknown>) => ({
        clave: `h${h['id']}`,
        icono: '🔄',
        titulo: (h['observacion'] as string) || 'Cambio de estado',
        cambio: `${labelOf(ESTADO_LABELS, h['estadoAnterior'] as string)} → ${labelOf(ESTADO_LABELS, h['estadoNuevo'] as string)}`,
        fecha: h['fecha'] as string,
      }));
    return [...aportes, ...cambios].sort((x, y) => y.fecha.localeCompare(x.fecha));
  });

  readonly formatFechaHora = formatFechaHora;

  abrirModeracionAporte(aporteId: number): void {
    this.aporteSeleccionado.set(aporteId);
    this.motivoOcultacion.set('');
    this.moderacionAporte.error.set('');
    this.moderacionAporte.exito.set('');
  }

  cancelarModeracionAporte(): void {
    if (!this.moderacionAporte.cargando()) {
      this.aporteSeleccionado.set(null);
      this.motivoOcultacion.set('');
      this.moderacionAporte.error.set('');
    }
  }

  ocultarAporte(aporteId: number): void {
    const motivo = this.motivoOcultacion().trim();
    if (
      !this.esAdministrador() ||
      this.aporteSeleccionado() !== aporteId ||
      !motivo ||
      this.moderacionAporte.cargando()
    ) {
      return;
    }

    this.moderacionAporte.realizar(
      this.aporteService.ocultarAporte(aporteId, motivo),
      'Aporte ocultado correctamente.',
      () => {
        this.aporteSeleccionado.set(null);
        this.motivoOcultacion.set('');
        this.actualizado.emit();
      },
      'No fue posible ocultar el aporte. Intenta de nuevo.',
    );
  }

  errorFormulario(): string {
    const largo = this.descripcion.trim().length;
    if (largo < MIN_APORTE) {
      return `Escribe al menos ${MIN_APORTE} caracteres.`;
    }
    if (this.tipo() === 'AVISTAMIENTO' && !this.ubicacionTexto.trim()) {
      return 'Cuéntanos dónde lo viste.';
    }
    return '';
  }

  /** Sube las fotos (si hay) y publica el aporte con sus URL. */
  publicar(): void {
    if (this.errorFormulario() || this.envio.cargando()) {
      return;
    }
    const casoId = this.caso().id;
    this.publicado.set(false);
    this.envio.error.set('');
    this.envio.cargando.set(true);

    if (this.fotos().length === 0) {
      this.publicarSinFotos(casoId);
    } else {
      this.publicarConFotos(casoId);
    }
  }

  private publicarSinFotos(casoId: number | string): void {
    const numCasoId = typeof casoId === 'string' ? Number.parseInt(casoId, 10) : casoId;
    this.aporteService
      .crear(numCasoId, {
        descripcion: this.descripcion.trim(),
        barrioId: this.tipo() === 'AVISTAMIENTO' ? (this.barrioId() ?? undefined) : undefined,
        ubicacionTexto: this.tipo() === 'AVISTAMIENTO' ? this.ubicacionTexto.trim() : undefined,
        fotografias: [],
      })
      .subscribe({
        next: () => this.finalizarPublicacion(),
        error: () => {
          this.envio.cargando.set(false);
          this.envio.error.set('No fue posible publicar el aporte. Intenta de nuevo.');
        },
      });
  }

  private publicarConFotos(casoId: number | string): void {
    const fotografias: string[] = [];
    let fotosSubidas = 0;

    const subirSiguiente = (): void => {
      if (fotosSubidas >= this.fotos().length) {
        this.crearAporteConFotos(casoId, fotografias);
        return;
      }

      this.casoService.subirImagen(this.fotos()[fotosSubidas], casoId).subscribe({
        next: (response: unknown) => {
          const url = (response as Record<string, unknown>)?.['url'] as string | undefined;
          if (url) {
            fotografias.push(url);
          }
          fotosSubidas++;
          subirSiguiente();
        },
        error: () => {
          this.envio.cargando.set(false);
          this.envio.error.set('No fue posible subir las fotos. Intenta de nuevo.');
        },
      });
    };

    subirSiguiente();
  }

  private crearAporteConFotos(casoId: number | string, fotografias: string[]): void {
    const numCasoId = typeof casoId === 'string' ? Number.parseInt(casoId, 10) : casoId;
    this.aporteService
      .crear(numCasoId, {
        descripcion: this.descripcion.trim(),
        barrioId: this.tipo() === 'AVISTAMIENTO' ? (this.barrioId() ?? undefined) : undefined,
        ubicacionTexto: this.tipo() === 'AVISTAMIENTO' ? this.ubicacionTexto.trim() : undefined,
        fotografias,
      })
      .subscribe({
        next: () => this.finalizarPublicacion(),
        error: () => {
          this.envio.cargando.set(false);
          this.envio.error.set('No fue posible publicar el aporte. Intenta de nuevo.');
        },
      });
  }

  private finalizarPublicacion(): void {
    this.descripcion = '';
    this.ubicacionTexto = '';
    this.deptoId.set(null);
    this.munId.set(null);
    this.barrioId.set(null);
    this.picker().limpiar();
    this.publicado.set(true);
    this.envio.cargando.set(false);
    this.actualizado.emit();
  }
}
