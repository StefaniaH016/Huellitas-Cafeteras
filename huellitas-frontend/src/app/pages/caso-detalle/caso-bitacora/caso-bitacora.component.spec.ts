import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { BitacoraService } from '../../../core/services/bitacora.service';
import { CasoDetalle } from '../../../core/services/caso.service';
import { CasoBitacoraComponent } from './caso-bitacora.component';

const caso: CasoDetalle = {
  id: 4,
  tipoCaso: 'HERIDO',
  estado: 'EN_ATENCION',
  urgencia: 'ALTA',
  descripcion: 'Perro',
  municipio: 'Armenia',
  barrio: 'Centro',
};

let bitacora: Record<string, ReturnType<typeof vi.fn>>;

function crear(esVeterinaria: boolean): CasoBitacoraComponent {
  TestBed.configureTestingModule({
    imports: [CasoBitacoraComponent],
    providers: [
      { provide: BitacoraService, useValue: bitacora },
      { provide: AuthService, useValue: { tieneRol: vi.fn().mockReturnValue(esVeterinaria) } },
    ],
  });
  const fixture = TestBed.createComponent(CasoBitacoraComponent);
  fixture.componentRef.setInput('caso', caso);
  fixture.detectChanges();
  return fixture.componentInstance;
}

beforeEach(() => {
  bitacora = {
    getBitacora: vi
      .fn()
      .mockReturnValue(of({ id: 1, casoId: 4, estado: 'ABIERTA', evoluciones: [] })),
    abrirBitacora: vi.fn().mockReturnValue(of({})),
    agregarEvolucion: vi.fn().mockReturnValue(of({})),
    registrarAlta: vi.fn().mockReturnValue(of({})),
    descargarPdf: vi.fn().mockReturnValue(of(new Blob())),
  };
});

it('does not load the clinical detail for other roles', () => {
  crear(false);

  expect(bitacora['getBitacora']).not.toHaveBeenCalled();
});

it('detects a case without a medical log (404)', () => {
  bitacora['getBitacora'].mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
  const component = crear(true);

  expect(component.sinBitacora()).toBe(true);
  expect(component.carga.error()).toBe('');
});

it('opens the log, adds an entry and registers the discharge', () => {
  const component = crear(true);
  component.abrir();
  component.evolucion = { diagnostico: 'Fractura' };
  component.registrarEvolucion();
  component.alta = { condicionAlta: 'Estable' };
  component.emitirAlta();

  expect(bitacora['abrirBitacora']).toHaveBeenCalledWith(4, undefined);
  expect(bitacora['agregarEvolucion']).toHaveBeenCalledWith(4, { diagnostico: 'Fractura' });
  expect(component.evolucion.diagnostico).toBe('');
  expect(bitacora['registrarAlta']).toHaveBeenCalledWith(4, { condicionAlta: 'Estable' });
  expect(component.accion.exito()).toBe('Alta médica emitida.');
});

it('downloads the PDF through an object URL', () => {
  const create = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:pdf');
  const revoke = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
  const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);

  crear(true).descargarPdf();

  expect(create).toHaveBeenCalled();
  expect(click).toHaveBeenCalled();
  expect(revoke).toHaveBeenCalledWith('blob:pdf');
  vi.restoreAllMocks();
});
