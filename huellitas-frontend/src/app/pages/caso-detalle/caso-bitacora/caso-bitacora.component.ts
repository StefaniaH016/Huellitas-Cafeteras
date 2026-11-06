import {
  Component,
  OnChanges,
  SimpleChanges,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  AltaMedica,
  Bitacora,
  BitacoraService,
  NuevaEvolucion,
} from '../../../core/services/bitacora.service';
import { CasoDetalle } from '../../../core/services/caso.service';
import {
  esCasoAbierto,
  formatFecha,
  formatFechaHora,
} from '../../../core/utils/caso-display.utils';
import { Observable } from 'rxjs';
import { EstadoAccion, EstadoCarga } from '../../../core/utils/request.utils';

/** Muestra el resumen a otros roles y las entradas clínicas solo a veterinarias verificadas. */
@Component({
  selector: 'app-caso-bitacora',
  imports: [FormsModule],
  templateUrl: './caso-bitacora.component.html',
})
export class CasoBitacoraComponent implements OnChanges {
  private bitacoraService = inject(BitacoraService);

  readonly caso = input.required<CasoDetalle>();
  readonly puedeConsultar = input(false);
  readonly puedeGestionar = input(false);
  readonly formularioActivo = input<'entrada' | 'alta' | null>(null);
  readonly actualizado = output<void>();
  readonly sinBitacoraChange = output<boolean>();
  readonly formularioCerrado = output<void>();

  readonly carga = new EstadoCarga();
  readonly accion = new EstadoAccion();
  readonly bitacora = signal<Bitacora | null>(null);
  readonly sinBitacora = signal(false);
  readonly errorPdf = signal('');

  evolucion: NuevaEvolucion = { diagnostico: '' };
  alta: AltaMedica = { condicionAlta: '' };
  confirmoAlta = false;

  readonly conAlta = computed(
    () =>
      this.bitacora()?.estado === 'ALTA_REGISTRADA' ||
      this.caso().resumenMedico?.altaMedica === true,
  );
  readonly admiteEntradas = computed(
    () => this.puedeGestionar() && esCasoAbierto(this.caso().estado) && !this.conAlta(),
  );
  readonly formatFecha = formatFecha;
  readonly formatFechaHora = formatFechaHora;

  ngOnChanges(changes: SimpleChanges): void {
    if (this.puedeConsultar() && (changes['caso'] || changes['puedeConsultar'])) {
      this.cargar();
    }
  }

  cargar(): void {
    this.sinBitacora.set(false);
    this.sinBitacoraChange.emit(false);
    this.bitacora.set(null);
    this.carga.error.set('');
    this.carga.cargando.set(true);
    this.bitacoraService.getBitacora(this.caso().id).subscribe({
      next: (bitacora) => {
        this.bitacora.set(bitacora);
        this.carga.cargando.set(false);
      },
      error: (err: unknown) => {
        // 404: el caso todavía no tiene bitácora.
        this.sinBitacora.set(err instanceof HttpErrorResponse && err.status === 404);
        this.sinBitacoraChange.emit(this.sinBitacora());
        this.carga.error.set(this.sinBitacora() ? '' : 'No fue posible cargar la bitácora.');
        this.carga.cargando.set(false);
      },
    });
  }

  registrarEvolucion(): void {
    if (!this.admiteEntradas() || !this.evolucion.diagnostico.trim()) {
      return;
    }
    this.ejecutar(
      this.bitacoraService.agregarEvolucion(this.caso().id, this.evolucion),
      'Entrada registrada.',
      () => {
        this.evolucion = { diagnostico: '' };
        this.formularioCerrado.emit();
      },
    );
  }

  emitirAlta(): void {
    if (!this.admiteEntradas() || !this.alta.condicionAlta.trim() || !this.confirmoAlta) {
      return;
    }
    this.ejecutar(
      this.bitacoraService.registrarAlta(this.caso().id, this.alta),
      'Alta médica emitida.',
      () => {
        this.alta = { condicionAlta: '' };
        this.confirmoAlta = false;
        this.formularioCerrado.emit();
      },
    );
  }

  descargarPdf(): void {
    this.errorPdf.set('');
    this.bitacoraService.descargarPdf(this.caso().id).subscribe({
      next: (pdf) => {
        const url = URL.createObjectURL(pdf);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `historia-clinica-caso-${this.caso().id}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
      },
      error: (err: unknown) => {
        console.error('Error descargando el PDF:', err);
        this.errorPdf.set('No fue posible descargar el resumen clínico.');
      },
    });
  }

  private ejecutar(fuente: Observable<unknown>, exito: string, despues?: () => void): void {
    this.accion.realizar(
      fuente,
      exito,
      () => {
        despues?.();
        this.cargar();
        this.actualizado.emit();
      },
      'No fue posible guardar en la bitácora. Intenta de nuevo.',
    );
  }
}
