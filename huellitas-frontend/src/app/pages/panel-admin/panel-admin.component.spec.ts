import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import {
  VerificacionPendiente,
  VerificacionService,
} from '../../core/services/verificacion.service';
import { PanelAdminComponent } from './panel-admin.component';

const pendiente: VerificacionPendiente = {
  usuarioId: 3,
  nombreEntidad: 'Fundación Huellitas',
  rolSolicitado: 'FUNDACION_REFUGIO',
  estado: 'PENDIENTE',
};

let verificacionService: Pick<VerificacionService, 'getPendientes' | 'resolverVerificacion'>;

beforeEach(() => {
  verificacionService = {
    getPendientes: vi.fn().mockReturnValue(of([pendiente])),
    resolverVerificacion: vi.fn().mockReturnValue(of({})),
  };

  TestBed.configureTestingModule({
    imports: [PanelAdminComponent],
    providers: [{ provide: VerificacionService, useValue: verificacionService }],
  });
});

it('loads pending accounts and renders their validation actions', () => {
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  expect(verificacionService.getPendientes).toHaveBeenCalledOnce();
  expect(fixture.nativeElement.textContent).toContain('Fundación Huellitas');
  expect(fixture.nativeElement.textContent).toContain('Aprobar');
  expect(fixture.nativeElement.textContent).toContain('Rechazar');
});

it('submits an approval and refreshes the pending list', () => {
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  fixture.componentInstance.resolverVerificacion(pendiente.usuarioId, 'APROBAR');

  expect(verificacionService.resolverVerificacion).toHaveBeenCalledWith(pendiente.usuarioId, {
    decision: 'APROBAR',
  });
  expect(fixture.componentInstance.mensaje()).toBe('La cuenta fue aprobada correctamente.');
  expect(verificacionService.getPendientes).toHaveBeenCalledTimes(2);
});

it('submits a rejection and reports the result', () => {
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  fixture.componentInstance.resolverVerificacion(pendiente.usuarioId, 'RECHAZAR');

  expect(verificacionService.resolverVerificacion).toHaveBeenCalledWith(pendiente.usuarioId, {
    decision: 'RECHAZAR',
  });
  expect(fixture.componentInstance.mensaje()).toBe('La cuenta fue rechazada correctamente.');
});

it('reports a failure when a verification cannot be resolved', () => {
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(verificacionService.resolverVerificacion).mockReturnValue(
    throwError(() => new Error('Request failed')),
  );
  const fixture = TestBed.createComponent(PanelAdminComponent);

  fixture.componentInstance.resolverVerificacion(pendiente.usuarioId, 'APROBAR');

  expect(fixture.componentInstance.errorMensaje()).toBe(
    'No fue posible resolver la verificación. Intenta nuevamente.',
  );
  expect(fixture.componentInstance.accionEnCurso()).toBe(null);
});

it('reports a failure when pending accounts cannot be loaded', () => {
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(verificacionService.getPendientes).mockReturnValue(
    throwError(() => new Error('Request failed')),
  );
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  expect(fixture.componentInstance.errorMensaje()).toBe(
    'No fue posible cargar las cuentas. Intenta nuevamente.',
  );
  expect(fixture.componentInstance.cargando()).toBe(false);
});
