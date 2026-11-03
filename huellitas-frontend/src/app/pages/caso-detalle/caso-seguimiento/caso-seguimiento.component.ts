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
import { EstadoCarga } from '../../../core/utils/request.utils';
import { FotoPickerComponent } from '../../../shared/components/foto-picker/foto-picker.component';
import { UbicacionSelectorComponent } from '../../../shared/components/ubicacion-selector/ubicacion-selector.component';

interface ItemActividad {
  clave: string;
  icono: string;
  titulo: string;
  texto?: string;
  cambio?: string;
  fecha: string;
}

const MIN_APORTE = 10;
const MAX_APORTE = 1000;

/** Pestaña Seguimiento: aportes de la comunidad y cambios de estado, del más reciente al más viejo. */
@Component({
  selector: 'app-caso-seguimiento',
  imports: [FormsModule, FotoPickerComponent, UbicacionSelectorComponent],
  templateUrl: './caso-seguimiento.component.html',
})
export class CasoSeguimientoComponent {
  private aporteService = inject(AporteService);
  private casoService = inject(CasoService);
  private authService = inject(AuthService);

  readonly caso = input.required<CasoDetalle>();
  readonly actualizado = output<void>();

  private readonly picker = viewChild.required(FotoPickerComponent);

  readonly maxAporte = MAX_APORTE;
  readonly envio = new EstadoCarga();
  readonly tipo = signal<'COMENTARIO' | 'AVISTAMIENTO'>('COMENTARIO');
  readonly fotos = signal<File[]>([]);
  readonly deptoId = signal<number | null>(null);
  readonly munId = signal<number | null>(null);
  readonly barrioId = signal<number | null>(null);
  readonly publicado = signal(false);
  descripcion = '';
  ubicacionTexto = '';

  readonly puedeAportar = computed(
    () => esCasoAbierto(this.caso().estado) && !this.authService.tieneRol('ADMINISTRADOR'),
  );

  readonly actividad = computed<ItemActividad[]>(() => {
    const caso = this.caso();
    const aportes = (caso.aportes ?? []).map((a: Record<string, unknown>) => ({
      clave: `a${a['id']}`,
      icono: a['tipo'] === 'AVISTAMIENTO' ? '👀' : '💬',
      titulo: `${a['autorNombre'] ?? 'Alguien'} · ${labelOf(APORTE_LABELS, a['tipo'] as string)}`,
      texto: a['comentario'] as string | undefined,
      fecha: a['fecha'] as string,
    }));
    const cambios = (caso.historial ?? []).map((h: Record<string, unknown>) => ({
      clave: `h${h['id']}`,
      icono: '🔄',
      titulo: (h['observacion'] as string) || 'Cambio de estado',
      cambio: `${labelOf(ESTADO_LABELS, h['estadoAnterior'] as string)} → ${labelOf(ESTADO_LABELS, h['estadoNuevo'] as string)}`,
      fecha: h['fecha'] as string,
    }));
    return [...aportes, ...cambios].sort((x, y) => y.fecha.localeCompare(x.fecha));
  });

  readonly formatFechaHora = formatFechaHora;

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
    if (this.errorFormulario()) {
      return;
    }
    const casoId = this.caso().id;
    this.publicado.set(false);

    if (this.fotos().length === 0) {
      this.publicarSinFotos(casoId);
    } else {
      this.publicarConFotos(casoId);
    }
  }

  private publicarSinFotos(casoId: number | string): void {
    const numCasoId = typeof casoId === 'string' ? parseInt(casoId, 10) : casoId;
    this.aporteService
      .crear(numCasoId, {
        descripcion: this.descripcion.trim(),
        deptoId: this.tipo() === 'AVISTAMIENTO' ? (this.deptoId() ?? undefined) : undefined,
        munId: this.tipo() === 'AVISTAMIENTO' ? (this.munId() ?? undefined) : undefined,
        barrioId: this.tipo() === 'AVISTAMIENTO' ? (this.barrioId() ?? undefined) : undefined,
        ubicacionTexto: this.tipo() === 'AVISTAMIENTO' ? this.ubicacionTexto.trim() : undefined,
        fotografias: [],
      })
      .subscribe({
        next: () => this.finalizarPublicacion(),
        error: () => {
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
          this.envio.error.set('No fue posible subir las fotos. Intenta de nuevo.');
        },
      });
    };

    subirSiguiente();
  }

  private crearAporteConFotos(casoId: number | string, fotografias: string[]): void {
    const numCasoId = typeof casoId === 'string' ? parseInt(casoId, 10) : casoId;
    this.aporteService
      .crear(numCasoId, {
        descripcion: this.descripcion.trim(),
        deptoId: this.tipo() === 'AVISTAMIENTO' ? (this.deptoId() ?? undefined) : undefined,
        munId: this.tipo() === 'AVISTAMIENTO' ? (this.munId() ?? undefined) : undefined,
        barrioId: this.tipo() === 'AVISTAMIENTO' ? (this.barrioId() ?? undefined) : undefined,
        ubicacionTexto: this.tipo() === 'AVISTAMIENTO' ? this.ubicacionTexto.trim() : undefined,
        fotografias,
      })
      .subscribe({
        next: () => this.finalizarPublicacion(),
        error: () => {
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
    this.actualizado.emit();
  }
}
