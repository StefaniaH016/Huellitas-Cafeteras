import { CommonModule } from '@angular/common';
import { Component, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AporteService } from '../../../core/services/aporte.service';
import { AuthService } from '../../../core/services/auth.service';
import { CasoDetalle, CasoService } from '../../../core/services/caso.service';
import {
  APORTE_LABELS,
  ESTADO_LABELS,
  RESULTADO_LABELS,
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

function obtenerCambioEstado(
  aporte: Record<string, unknown>,
  historial: Record<string, unknown>[],
): Record<string, unknown> | undefined {
  if (aporte['tipo'] !== 'ACTUALIZACION') {
    return undefined;
  }
  return historial.find((item) => coincideAporteHistorial(aporte, item));
}

function obtenerEtiquetaCierre(cierre: { resultado: string; observacion: string } | null): string {
  if (cierre?.resultado === 'ENTREGADO_A_CUIDADOR') {
    return 'Entregado a cuidador';
  }
  if (cierre?.resultado === 'FALLECIDO') {
    return 'Falleció';
  }
  return cierre ? labelOf(RESULTADO_LABELS, cierre.resultado) : '';
}

function obtenerTituloAporte(aporte: Record<string, unknown>, cierre: boolean): string {
  const etiqueta = cierre ? 'Cierre de caso' : labelOf(APORTE_LABELS, aporte['tipo'] as string);
  return `${aporte['autorNombre'] ?? 'Alguien'} · ${etiqueta}`;
}

function obtenerIdAporte(aporte: Record<string, unknown>): number | undefined {
  const tipo = aporte['tipo'];
  const id = aporte['id'];
  if (tipo !== 'COMENTARIO' && tipo !== 'AVISTAMIENTO') {
    return undefined;
  }
  return typeof id === 'number' ? id : undefined;
}

function obtenerTextoAporte(
  aporte: Record<string, unknown>,
  cierre: { resultado: string; observacion: string } | null,
  etiquetaCierre: string,
): string | undefined {
  if (!cierre) {
    return aporte['comentario'] as string | undefined;
  }
  return [etiquetaCierre, cierre.observacion].filter(Boolean).join('. ');
}

function obtenerCambioActividad(
  cambioEstado: Record<string, unknown> | undefined,
): string | undefined {
  if (!cambioEstado) {
    return undefined;
  }
  return `${labelOf(ESTADO_LABELS, cambioEstado['estadoAnterior'] as string)} → ${labelOf(ESTADO_LABELS, cambioEstado['estadoNuevo'] as string)}`;
}

function crearItemAporte(
  aporte: Record<string, unknown>,
  historial: Record<string, unknown>[],
): ItemActividad {
  const cierre = aporte['tipo'] === 'ACTUALIZACION' ? extraerCierre(aporte['comentario']) : null;
  const cambioEstado = obtenerCambioEstado(aporte, historial);
  const etiquetaCierre = obtenerEtiquetaCierre(cierre);
  const tipoAporte = aporte['tipo'];

  return {
    clave: `a${aporte['id']}`,
    icono: tipoAporte === 'AVISTAMIENTO' ? '👀' : '💬',
    titulo: obtenerTituloAporte(aporte, cierre !== null),
    aporteId: obtenerIdAporte(aporte),
    texto: obtenerTextoAporte(aporte, cierre, etiquetaCierre),
    zona: aporte['zona'] as string | undefined,
    referencia: aporte['ubicacionTexto'] as string | undefined,
    fotografias: aporte['fotografias'] as string[] | undefined,
    cambio: obtenerCambioActividad(cambioEstado),
    fecha: aporte['fecha'] as string,
  };
}

function crearItemCambio(historial: Record<string, unknown>): ItemActividad {
  return {
    clave: `h${historial['id']}`,
    icono: '🔄',
    titulo: (historial['observacion'] as string) || 'Cambio de estado',
    cambio: `${labelOf(ESTADO_LABELS, historial['estadoAnterior'] as string)} → ${labelOf(ESTADO_LABELS, historial['estadoNuevo'] as string)}`,
    fecha: historial['fecha'] as string,
  };
}

const MIN_APORTE = 10;
const MAX_APORTE = 1000;

function extraerCierre(comentario: unknown): { resultado: string; observacion: string } | null {
  if (typeof comentario !== 'string') {
    return null;
  }
  const coincidencia = /^Caso cerrado con resultado:\s*([A-Z_]+)\.?\s*(.*)$/i.exec(comentario);
  if (!coincidencia) {
    return null;
  }
  return {
    resultado: coincidencia[1].toUpperCase(),
    observacion: coincidencia[2].trim(),
  };
}

function extraerCierreHistorial(observacion: unknown): {
  resultado: string;
  observacion: string;
} | null {
  if (typeof observacion !== 'string') {
    return null;
  }
  const coincidencia = /^Cierre de caso:\s*([A-Z_]+)\.?\s*(.*)$/i.exec(observacion);
  if (!coincidencia) {
    return null;
  }
  return {
    resultado: coincidencia[1].toUpperCase(),
    observacion: coincidencia[2].trim(),
  };
}

function coincidenCierres(
  aporte: Record<string, unknown>,
  historial: Record<string, unknown>,
): boolean {
  if (historial['estadoNuevo'] !== 'RESUELTO') {
    return false;
  }
  const cierre = extraerCierre(aporte['comentario']);
  const cierreHistorial = extraerCierreHistorial(historial['observacion']);
  return (
    cierre !== null &&
    cierreHistorial !== null &&
    cierre.resultado === cierreHistorial.resultado &&
    cierre.observacion === cierreHistorial.observacion
  );
}

function normalizarNovedadCustodia(novedad: unknown): string | null {
  if (typeof novedad !== 'string') {
    return null;
  }
  const normalizada = novedad
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
  if (normalizada.startsWith('custodia asumida por la fundacion:')) {
    return normalizada.replace(/^custodia asumida por la fundacion:\s*/, 'custodia asumida por ');
  }
  if (normalizada.startsWith('custodia liberada por la fundacion')) {
    return normalizada.replace(
      /^custodia liberada por la fundacion[.:]?\s*/,
      'custodia liberada. ',
    );
  }
  return normalizada.startsWith('custodia asumida por ') ||
    normalizada.startsWith('custodia liberada.')
    ? normalizada
    : null;
}

function coincideAporteHistorial(
  aporte: Record<string, unknown>,
  historial: Record<string, unknown>,
): boolean {
  if (aporte['tipo'] !== 'ACTUALIZACION') {
    return false;
  }
  if (
    typeof aporte['comentario'] === 'string' &&
    aporte['comentario'] === historial['observacion']
  ) {
    return true;
  }
  const novedadAporte = normalizarNovedadCustodia(aporte['comentario']);
  const novedadHistorial = normalizarNovedadCustodia(historial['observacion']);
  return (
    coincidenCierres(aporte, historial) ||
    (novedadAporte !== null && novedadAporte === novedadHistorial)
  );
}

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
    const historial = caso.historial ?? [];
    const aportes = (caso.aportes ?? []).map((aporte) => crearItemAporte(aporte, historial));
    const cambios = (caso.historial ?? [])
      .filter(
        (item) => !(caso.aportes ?? []).some((aporte) => coincideAporteHistorial(aporte, item)),
      )
      .map((item) => crearItemCambio(item));
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
