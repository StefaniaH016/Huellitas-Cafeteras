import { of, throwError } from 'rxjs';
import { ERROR_CARGA, EstadoAccion, EstadoCarga } from './request.utils';

describe('request state helpers', () => {
  afterEach(() => vi.restoreAllMocks());

  it('tracks loading and passes the value on success', () => {
    const estado = new EstadoCarga();
    const recibido = vi.fn();

    estado.ejecutar(of(5), recibido);

    expect(recibido).toHaveBeenCalledWith(5);
    expect(estado.cargando()).toBe(false);
    expect(estado.error()).toBe('');
  });

  it('stores the error message on failure', () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const estado = new EstadoCarga();

    estado.ejecutar(
      throwError(() => new Error('x')),
      vi.fn(),
    );

    expect(estado.error()).toBe(ERROR_CARGA);
    expect(estado.cargando()).toBe(false);
  });

  it('sets the success message of an action and runs the follow-up', () => {
    const accion = new EstadoAccion();
    const despues = vi.fn();

    accion.realizar(of('ok'), 'Guardado', despues);

    expect(accion.exito()).toBe('Guardado');
    expect(despues).toHaveBeenCalledWith('ok');
  });
});
