import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
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

/**
 * Pestaña Bitácora médica. La veterinaria la abre, registra evoluciones y emite el alta.
 * Los demás roles solo ven el resumen (Ley 576 de 2000: reserva de la historia clínica).
 */
@Component({
  selector: 'app-caso-bitacora',
  imports: [FormsModule],
  templateUrl: './caso-bitacora.component.html',
})
export class CasoBitacoraComponent implements OnInit {
  private bitacoraService = inject(BitacoraService);
  private authService = inject(AuthService);

  readonly caso = input.required<CasoDetalle>();
  readonly actualizado = output<void>();

  readonly esVeterinaria = this.authService.tieneRol('VETERINARIA');
  readonly carga = new EstadoCarga();
  readonly accion = new EstadoAccion();
  readonly bitacora = signal<Bitacora | null>(null);
  readonly sinBitacora = signal(false);

  observacionInicial = '';
  evolucion: NuevaEvolucion = { diagnostico: '' };
  alta: AltaMedica = { condicionAlta: '' };

  readonly conAlta = computed(() => this.caso().resumenMedico?.altaMedica === true);
  readonly admiteEntradas = computed(
    () => this.esVeterinaria && esCasoAbierto(this.caso().estado) && !this.conAlta(),
  );
  readonly formatFecha = formatFecha;
  readonly formatFechaHora = formatFechaHora;

  ngOnInit(): void {
    if (this.esVeterinaria) {
      this.cargar();
    }
  }

  cargar(): void {
    this.sinBitacora.set(false);
    this.carga.cargando.set(true);
    this.bitacoraService.getBitacora(this.caso().id).subscribe({
      next: (bitacora) => {
        this.bitacora.set(bitacora);
        this.carga.cargando.set(false);
      },
      error: (err: unknown) => {
        // 404: el caso todavía no tiene bitácora.
        this.sinBitacora.set(err instanceof HttpErrorResponse && err.status === 404);
        this.carga.error.set(this.sinBitacora() ? '' : 'No fue posible cargar la bitácora.');
        this.carga.cargando.set(false);
      },
    });
  }

  abrir(): void {
    this.ejecutar(
      this.bitacoraService.abrirBitacora(
        this.caso().id,
        this.observacionInicial.trim() || undefined,
      ),
      'Abriste la bitácora: quedas como veterinaria responsable.',
    );
  }

  registrarEvolucion(): void {
    this.ejecutar(
      this.bitacoraService.agregarEvolucion(this.caso().id, this.evolucion),
      'Entrada registrada.',
      () => {
        this.evolucion = { diagnostico: '' };
      },
    );
  }

  emitirAlta(): void {
    this.ejecutar(
      this.bitacoraService.registrarAlta(this.caso().id, this.alta),
      'Alta médica emitida.',
    );
  }

  descargarPdf(): void {
    this.bitacoraService.descargarPdf(this.caso().id).subscribe({
      next: (pdf) => {
        const url = URL.createObjectURL(pdf);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `historia-clinica-caso-${this.caso().id}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
      },
      error: (err: unknown) => console.error('Error descargando el PDF:', err),
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
