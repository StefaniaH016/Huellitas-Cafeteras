import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CasoService } from '../../core/services/caso.service';
import {
  VerificacionPendiente,
  VerificacionService,
} from '../../core/services/verificacion.service';
import { CasoReportado, ModeracionService } from '../../core/services/moderacion.service';
import { PanelAdminComponent } from './panel-admin.component';

const pendiente: VerificacionPendiente = {
  usuarioId: 3,
  nombreEntidad: 'Fundación Huellitas',
  rolSolicitado: 'FUNDACION_REFUGIO',
  estado: 'PENDIENTE',
  documentoSoporteDisponible: true,
};

const casoReportado: CasoReportado = {
  casoId: 7,
  motivo: 'Reporte para revisión',
  estadoRevision: 'PENDIENTE',
  fechaCreacion: '2026-03-11',
};

let verificacionService: Pick<VerificacionService, 'getPendientes' | 'resolverVerificacion'>;
let moderacionService: Pick<ModeracionService, 'getCasosReportados'>;
let casoService: Pick<CasoService, 'listarCasos'>;

beforeEach(() => {
  verificacionService = {
    getPendientes: vi.fn().mockReturnValue(of([pendiente])),
    resolverVerificacion: vi.fn().mockReturnValue(of({})),
  };
  moderacionService = {
    getCasosReportados: vi.fn().mockReturnValue(of([casoReportado])),
  };
  casoService = {
    listarCasos: vi.fn().mockReturnValue(
      of({
        content: [
          {
            id: 12,
            descripcion: 'Gato rescatado',
            urgencia: 'ALTA',
            estado: 'REGISTRADO',
            municipio: 'Armenia',
            barrio: 'Centro',
          },
        ],
        totalElements: 1,
      }),
    ),
  };

  TestBed.configureTestingModule({
    imports: [PanelAdminComponent],
    providers: [
      { provide: VerificacionService, useValue: verificacionService },
      { provide: ModeracionService, useValue: moderacionService },
      { provide: CasoService, useValue: casoService },
      provideRouter([]),
    ],
  });
});

it('loads pending accounts and renders their validation actions', () => {
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  expect(verificacionService.getPendientes).toHaveBeenCalledOnce();
  expect(fixture.nativeElement.textContent).toContain('Fundación Huellitas');
  expect(fixture.nativeElement.textContent).toContain('Aprobar');
  expect(fixture.nativeElement.textContent).toContain('Rechazar');
  expect(fixture.nativeElement.textContent).toContain('NIT: No informado');
  expect(fixture.nativeElement.textContent).toContain('Ver documento');
});

it('loads general cases in addition to cases reported for moderation', () => {
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  expect(casoService.listarCasos).toHaveBeenCalledWith({ page: 0, size: 10 });
  expect(fixture.nativeElement.textContent).toContain('Casos del sistema');
  expect(fixture.nativeElement.textContent).toContain('Caso #12');
  expect(fixture.nativeElement.textContent).toContain('Gato rescatado');
});

it('loads reported cases and links to their details for moderation', () => {
  const fixture = TestBed.createComponent(PanelAdminComponent);
  fixture.detectChanges();

  expect(moderacionService.getCasosReportados).toHaveBeenCalledOnce();
  expect(fixture.nativeElement.textContent).toContain('Contenidos por moderar');
  expect(fixture.nativeElement.textContent).toContain('Caso #7');
  expect(fixture.nativeElement.textContent).toContain('Revisar caso');
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
