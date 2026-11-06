import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
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

function crear(
  puedeConsultar: boolean,
  puedeGestionar = puedeConsultar,
): ComponentFixture<CasoBitacoraComponent> {
  TestBed.configureTestingModule({
    imports: [CasoBitacoraComponent],
    providers: [{ provide: BitacoraService, useValue: bitacora }],
  });
  const fixture = TestBed.createComponent(CasoBitacoraComponent);
  fixture.componentRef.setInput('caso', caso);
  fixture.componentRef.setInput('puedeConsultar', puedeConsultar);
  fixture.componentRef.setInput('puedeGestionar', puedeGestionar);
  fixture.detectChanges();
  return fixture;
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

it('loads the clinical detail for an authorized read-only user', () => {
  bitacora['getBitacora'].mockReturnValue(
    of({
      id: 1,
      casoId: 4,
      estado: 'ABIERTA',
      evoluciones: [
        {
          id: 2,
          fecha: '2026-11-04T10:00:00',
          diagnostico: 'Fractura en recuperación',
          procedimiento: 'Inmovilización',
          tratamiento: 'Reposo',
          medicamentos: 'Analgésico',
          evolucion: 'Mejora',
          observaciones: 'Mejora',
          firmanteNombre: 'Dra. Vet',
        },
      ],
    }),
  );
  const fixture = crear(true, false);

  expect(bitacora['getBitacora']).toHaveBeenCalledWith(4);
  expect(fixture.nativeElement.textContent).toContain('Fractura en recuperación');
  expect(fixture.nativeElement.textContent).toContain('Inmovilización');
  expect(fixture.nativeElement.textContent).toContain('Analgésico');
  expect(fixture.nativeElement.textContent).not.toContain('Nueva entrada');
});

it('shows medical entries in the MOCKUPS2 card structure', () => {
  bitacora['getBitacora'].mockReturnValue(
    of({
      id: 1,
      casoId: 4,
      estado: 'ABIERTA',
      evoluciones: [
        {
          id: 2,
          fecha: '2026-11-04T10:00:00',
          diagnostico: 'Fractura en recuperación',
          procedimiento: 'Inmovilización',
          tratamiento: 'Reposo',
          medicamentos: 'Analgésico',
          evolucion: 'Mejora',
          firmanteNombre: 'Dra. Vet',
          fechaFirma: '2026-11-04T10:00:00',
        },
      ],
    }),
  );
  const fixture = crear(true, false);

  const entry = fixture.nativeElement.querySelector('.entry') as HTMLElement;
  expect(entry.querySelector('.entry-head')?.textContent).toContain('Evolución clínica');
  expect(entry.querySelector('.badge')?.textContent).toContain('Evolución');
  expect(entry.querySelector('.entry-grid')?.textContent).toContain('Diagnóstico');
  expect(entry.querySelector('.entry-grid')?.textContent).toContain('Tratamiento y medicamentos');
  expect(entry.querySelector('.entry-grid')?.textContent).not.toContain('MedicamentosAnalgésico');
  expect(entry.querySelector('.entry-sign')?.textContent).toContain('Firmada por Dra. Vet');
});

it('shows only the form selected from the Actions panel', () => {
  const fixture = crear(true);

  expect(fixture.nativeElement.textContent).not.toContain('Firmar y guardar entrada');
  expect(fixture.nativeElement.textContent).not.toContain('Emitir alta');

  fixture.componentRef.setInput('formularioActivo', 'entrada');
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Firmar y guardar entrada');
  expect(fixture.nativeElement.textContent).not.toContain('Condición al alta');
});

it('detects a case without a medical log (404)', () => {
  bitacora['getBitacora'].mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
  const component = crear(true).componentInstance;

  expect(component.sinBitacora()).toBe(true);
  expect(component.carga.error()).toBe('');
});

it('reports when the case has no medical log', () => {
  bitacora['getBitacora'].mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
  const component = crear(true).componentInstance;

  expect(component.sinBitacora()).toBe(true);
});

it('adds an entry and registers the discharge', () => {
  const component = crear(true).componentInstance;
  component.evolucion = { diagnostico: 'Fractura' };
  component.registrarEvolucion();
  component.alta = { condicionAlta: 'Estable' };
  component.confirmoAlta = true;
  component.emitirAlta();

  expect(bitacora['abrirBitacora']).not.toHaveBeenCalled();
  expect(bitacora['agregarEvolucion']).toHaveBeenCalledWith(4, { diagnostico: 'Fractura' });
  expect(component.evolucion.diagnostico).toBe('');
  expect(bitacora['registrarAlta']).toHaveBeenCalledWith(4, { condicionAlta: 'Estable' });
  expect(component.accion.exito()).toBe('Alta médica emitida.');
});

it('downloads the PDF through an object URL', () => {
  const create = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:pdf');
  const revoke = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
  const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);

  crear(true).componentInstance.descargarPdf();

  expect(create).toHaveBeenCalled();
  expect(click).toHaveBeenCalled();
  expect(revoke).toHaveBeenCalledWith('blob:pdf');
  vi.restoreAllMocks();
});
