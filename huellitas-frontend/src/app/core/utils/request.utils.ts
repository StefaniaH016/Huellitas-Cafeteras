import { signal } from '@angular/core';
import { Observable } from 'rxjs';

export const ERROR_CARGA = 'No fue posible cargar la información. Intenta de nuevo.';

/**
 * Estado de una petición al backend (cargando y mensaje de error) y la forma única de
 * suscribirse a ella, para no repetir el mismo next/error en cada pantalla.
 */
export class EstadoCarga {
  readonly cargando = signal(false);
  readonly error = signal('');

  ejecutar<T>(
    fuente: Observable<T>,
    alTerminar: (valor: T) => void,
    mensajeError = ERROR_CARGA,
  ): void {
    this.cargando.set(true);
    this.error.set('');
    fuente.subscribe({
      next: (valor) => {
        alTerminar(valor);
        this.cargando.set(false);
      },
      error: (err: unknown) => {
        console.error(mensajeError, err);
        this.error.set(mensajeError);
        this.cargando.set(false);
      },
    });
  }
}

export const ERROR_ACCION = 'No fue posible completar la acción. Intenta de nuevo.';

/** Una acción del usuario (guardar, aprobar, cerrar…) con su mensaje de éxito. */
export class EstadoAccion extends EstadoCarga {
  readonly exito = signal('');

  realizar<T>(
    fuente: Observable<T>,
    mensajeExito: string,
    alTerminar?: (valor: T) => void,
    mensajeError = ERROR_ACCION,
  ): void {
    this.exito.set('');
    this.ejecutar(
      fuente,
      (valor) => {
        this.exito.set(mensajeExito);
        alTerminar?.(valor);
      },
      mensajeError,
    );
  }
}
