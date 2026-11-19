import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap, Params, provideRouter, Router } from '@angular/router';
import { BehaviorSubject, NEVER, of, throwError } from 'rxjs';
import { CasoService } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { CasoReportado, ModeracionService } from '../../core/services/moderacion.service';
import { BitacoraService } from '../../core/services/bitacora.service';
import { CasoDetalleComponent } from './caso-detalle.component';

let casoService: Pick<CasoService, 'getDetalle' | 'cerrarCaso'>;
let route: Pick<ActivatedRoute, 'params' | 'queryParamMap'>;
let routeParams: BehaviorSubject<Params>;
let router: Router;
let authService: Pick<AuthService, 'tieneRol'>;
let moderacionService: Pick<ModeracionService, 'getCasosReportados' | 'resolverModeracion'>;
let usuarioService: Pick<UsuarioService, 'getMiPerfil'>;
let bitacoraService: Pick<BitacoraService, 'getBitacora' | 'abrirBitacora'>;

const casoReportado: CasoReportado = {
  casoId: 12,
  motivo: 'Contenido duplicado',
  estadoRevision: 'PENDIENTE',
  fechaCreacion: '2026-10-01',
};

const casoCompleto = {
  id: 12,
  tipoCaso: 'HERIDO',
  estado: 'REGISTRADO',
  urgencia: 'ALTA',
  descripcion: 'Perro herido',
  fechaCreacion: '2026-10-01T12:00:00Z',
  animal: {
    especie: 'CANINO',
    raza: 'Criollo',
    edadAproximada: '2 años',
    color: 'Negro',
    senasParticulares: 'Mancha blanca',
  },
  ubicacion: {
    municipio: 'Armenia',
    barrio: 'Centro',
    direccionReferencia: 'Cerca al parque',
  },
  fotografias: [
    { url: 'https://example.test/second.jpg' },
    { url: 'https://example.test/main.jpg', principal: true },
    { url: ' ' },
  ],
  autorId: 21,
  autorNombre: 'Ana',
  fundacionCustodioId: 5,
  fundacionCustodioNombre: 'Fundación Patitas',
  resumenMedico: {
    id: 33,
    fechaApertura: '2026-10-02T12:00:00Z',
    altaMedica: false,
    fechaAlta: '',
    veterinariaNombre: 'Dra. Vet',
  },
  aportes: [{ id: 2, comentario: 'Actualización', fecha: '2026-10-02T12:00:00Z' }],
  historial: [{ id: 1, estadoNuevo: 'REGISTRADO', fecha: '2026-10-01T12:00:00Z' }],
};

beforeEach(() => {
  casoService = {
    getDetalle: vi.fn().mockReturnValue(of({ id: 12, descripcion: 'Caso de prueba' })),
    cerrarCaso: vi.fn().mockReturnValue(of({ id: 12, estado: 'RESUELTO' })),
  };
  authService = { tieneRol: vi.fn().mockReturnValue(false) };
  usuarioService = {
    getMiPerfil: vi.fn().mockReturnValue(
      of({
        id: 21,
        nombre: 'Usuario',
        correo: 'user@test.com',
        rol: 'CIUDADANO_VOLUNTARIO',
        verificado: true,
        disponibleRescate: false,
      }),
    ),
  };
  bitacoraService = {
    getBitacora: vi.fn().mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 }))),
    abrirBitacora: vi.fn().mockReturnValue(of({})),
  };
  moderacionService = {
    getCasosReportados: vi.fn().mockReturnValue(of([])),
    resolverModeracion: vi
      .fn()
      .mockReturnValue(of({ casoId: 12, decision: 'OCULTADO', fechaRevision: '2026-10-02' })),
  };
  routeParams = new BehaviorSubject<Params>({ id: '12' });
  route = {
    params: routeParams.asObservable(),
    queryParamMap: of(convertToParamMap({})),
  };
  TestBed.configureTestingModule({
    imports: [CasoDetalleComponent],
    providers: [
      provideRouter([]),
      { provide: ActivatedRoute, useValue: route },
      { provide: CasoService, useValue: casoService },
      { provide: AuthService, useValue: authService },
      { provide: ModeracionService, useValue: moderacionService },
      { provide: UsuarioService, useValue: usuarioService },
      { provide: BitacoraService, useValue: bitacoraService },
    ],
  });
  router = TestBed.inject(Router);
  vi.spyOn(router, 'navigate').mockResolvedValue(true);
});

afterEach(() => vi.restoreAllMocks());

it('marks a new case for moderation before showing review decisions', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'ADMINISTRADOR');
  vi.mocked(moderacionService.getCasosReportados).mockReturnValue(of([]));
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.casoModeracion()).toBeNull();
  expect(fixture.nativeElement.textContent).toContain('Marcar para moderación');
  expect(fixture.nativeElement.textContent).not.toContain('Mantener publicado');
  expect(fixture.nativeElement.textContent).not.toContain('Ocultar del feed');

  component.motivoMarcaModeracion = 'Duplicado de otro reporte';
  component.marcarParaModeracion();

  expect(moderacionService.resolverModeracion).not.toHaveBeenCalled();
  fixture.detectChanges();
  expect(component.marcacionModeracionLocal()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Mantener publicado');
  expect(fixture.nativeElement.textContent).toContain('Ocultar del feed');

  component.motivoDecisionModeracion = 'Contenido inapropiado';
  component.resolverModeracion('OCULTAR');
  expect(moderacionService.resolverModeracion).toHaveBeenCalledWith(12, {
    decision: 'OCULTAR',
    motivo: 'Motivo de marcación: Duplicado de otro reporte. Decisión: Contenido inapropiado',
  });
});

it('loads moderation details and applies the administrator decision with its reason', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'ADMINISTRADOR');
  vi.mocked(moderacionService.getCasosReportados).mockReturnValue(of([casoReportado]));
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(moderacionService.getCasosReportados).toHaveBeenCalledOnce();
  expect(component.casoModeracion()).toEqual(casoReportado);
  expect(fixture.nativeElement.textContent).toContain('Motivo de la decisión');
  expect(fixture.nativeElement.textContent).toContain('Duplicado de otro reporte');

  component.motivoDecisionModeracion = 'Información posiblemente falsa';
  component.resolverModeracion('MANTENER');

  expect(moderacionService.resolverModeracion).toHaveBeenCalledWith(12, {
    decision: 'MANTENER',
    motivo: 'Motivo de marcación: Contenido duplicado. Decisión: Información posiblemente falsa',
  });
  fixture.detectChanges();
  expect(component.mensajeModeracion()).toBe('El caso se mantuvo publicado.');
  expect(component.moderacionFinalizada()).toBe(true);
  expect(fixture.nativeElement.textContent).not.toContain('Mantener publicado');
  expect(fixture.nativeElement.textContent).not.toContain('Ocultar del feed');
});

it('does not request moderation data for non-administrator roles', () => {
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(moderacionService.getCasosReportados).not.toHaveBeenCalled();
  expect(fixture.nativeElement.textContent).not.toContain('Moderación');
});

it('allows the case reporter to read the medical log', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 21,
      nombre: 'Reportante',
      correo: 'reportante@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, autorId: 21, estado: 'EN_ATENCION', descripcion: 'Caso propio' }),
  );

  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  fixture.componentInstance.selectTab('bitacora');
  fixture.detectChanges();

  expect(fixture.componentInstance.puedeConsultarBitacora()).toBe(true);
  expect(bitacoraService.getBitacora).toHaveBeenCalledWith(12);
});

it('allows the custodial foundation to read the medical log', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'FUNDACION_REFUGIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 31,
      nombre: 'Fundación Patitas',
      correo: 'fundacion@test.com',
      rol: 'FUNDACION_REFUGIO',
      verificado: true,
      organizacionId: 5,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, autorId: 21, fundacionCustodioId: 5, estado: 'EN_ATENCION' }),
  );

  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  fixture.componentInstance.selectTab('bitacora');
  fixture.detectChanges();

  expect(fixture.componentInstance.puedeConsultarBitacora()).toBe(true);
  expect(bitacoraService.getBitacora).toHaveBeenCalledWith(12);
});

it('does not allow a foundation without custody or a citizen unrelated to the case to read the medical log', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'FUNDACION_REFUGIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 31,
      nombre: 'Otra fundación',
      correo: 'fundacion@test.com',
      rol: 'FUNDACION_REFUGIO',
      verificado: true,
      organizacionId: 99,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, autorId: 21, fundacionCustodioId: 5, estado: 'EN_ATENCION' }),
  );

  const foundationFixture = TestBed.createComponent(CasoDetalleComponent);
  foundationFixture.detectChanges();
  foundationFixture.componentInstance.selectTab('bitacora');
  foundationFixture.detectChanges();
  expect(foundationFixture.componentInstance.puedeConsultarBitacora()).toBe(false);
  expect(bitacoraService.getBitacora).not.toHaveBeenCalled();

  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Ciudadano externo',
      correo: 'externo@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  const citizenFixture = TestBed.createComponent(CasoDetalleComponent);
  citizenFixture.detectChanges();
  citizenFixture.componentInstance.selectTab('bitacora');
  citizenFixture.detectChanges();
  expect(citizenFixture.componentInstance.puedeConsultarBitacora()).toBe(false);
  expect(bitacoraService.getBitacora).not.toHaveBeenCalled();
});

it('allows the case reporter to download the PDF without reading entries', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 21,
      nombre: 'Reportante',
      correo: 'reportante@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      autorId: 21,
      estado: 'EN_ATENCION',
      resumenMedico: { id: 5, altaMedica: true },
    }),
  );

  const reporterFixture = TestBed.createComponent(CasoDetalleComponent);
  reporterFixture.detectChanges();
  expect(reporterFixture.componentInstance.puedeDescargarBitacoraPdf()).toBe(true);

  expect(bitacoraService.getBitacora).not.toHaveBeenCalled();
});

it('allows the custodial foundation to download the PDF without reading entries', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'FUNDACION_REFUGIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 31,
      nombre: 'Fundación Patitas',
      correo: 'fundacion@test.com',
      rol: 'FUNDACION_REFUGIO',
      verificado: true,
      organizacionId: 5,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      autorId: 21,
      fundacionCustodioId: 5,
      estado: 'EN_ATENCION',
      resumenMedico: { id: 5, altaMedica: true },
    }),
  );
  const foundationFixture = TestBed.createComponent(CasoDetalleComponent);
  foundationFixture.detectChanges();
  expect(foundationFixture.componentInstance.puedeDescargarBitacoraPdf()).toBe(true);

  expect(bitacoraService.getBitacora).not.toHaveBeenCalled();
});

it('does not offer citizen case reporting for moderation', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Otro ciudadano',
      correo: 'otro@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, autorId: 21, estado: 'REGISTRADO', descripcion: 'Caso de otro ciudadano' }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).not.toContain('Reportar caso');
});

it('keeps the medical-log tab selectable without bypassing read permissions', () => {
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  const tab = fixture.nativeElement.querySelectorAll('.tabs .tab')[2] as HTMLButtonElement;
  expect(tab.hasAttribute('aria-disabled')).toBe(false);
  tab.click();
  fixture.detectChanges();

  expect(fixture.componentInstance.activeTab()).toBe('bitacora');
  expect(bitacoraService.getBitacora).not.toHaveBeenCalled();
  expect(fixture.nativeElement.textContent).toContain('Resumen médico');
  expect(fixture.nativeElement.textContent).toContain('Solo ves el resumen médico.');
});

it('does not render a duplicate assisted summary on the information tab', () => {
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).not.toContain('Resumen asistencial');
});

it('allows a verified veterinarian to open a log only when the case is already in attention', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'VETERINARIA');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Dra. Vet',
      correo: 'vet@test.com',
      rol: 'VETERINARIA',
      verificado: true,
      organizacionId: 3,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, estado: 'EN_ATENCION', descripcion: 'Caso en atención' }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.puedeConsultarBitacora()).toBe(true);
  expect(component.puedeTomarAtencion()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Tomar atención médica');
  expect(fixture.nativeElement.textContent).not.toContain('Nueva entrada');
  expect(fixture.nativeElement.textContent).not.toContain('Emitir alta médica');

  component.tomarAtencionMedica();

  expect(bitacoraService.abrirBitacora).toHaveBeenCalledWith(12, undefined);
  expect(component.mensajeAccion()).toContain('a tu cargo');
});

it('does not offer to open a medical log while the case is still registered', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'VETERINARIA');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Dra. Vet',
      correo: 'vet@test.com',
      rol: 'VETERINARIA',
      verificado: true,
      organizacionId: 3,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, estado: 'REGISTRADO', descripcion: 'Caso registrado' }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.componentInstance.puedeTomarAtencion()).toBe(false);
  expect(fixture.nativeElement.textContent).not.toContain('Tomar atención médica');
});

it('waits for the case detail before rendering the veterinarian bitacora', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'VETERINARIA');
  vi.mocked(casoService.getDetalle).mockReturnValue(NEVER);

  const fixture = TestBed.createComponent(CasoDetalleComponent);
  expect(() => fixture.detectChanges()).not.toThrow();
  expect(fixture.nativeElement.textContent).not.toContain('Resumen médico');
});

it('allows administrators to consult clinical entries', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'ADMINISTRADOR');
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  fixture.componentInstance.selectTab('bitacora');
  fixture.detectChanges();

  expect(fixture.componentInstance.puedeConsultarBitacora()).toBe(true);
  expect(bitacoraService.getBitacora).toHaveBeenCalledWith(12);
});

it('offers the reportant an action to add case information from the Actions panel', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, estado: 'REGISTRADO', autorId: 21, descripcion: 'Caso registrado' }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.puedeAportarCaso()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Agregar aporte');

  component.selectTab('seguimiento');

  expect(component.activeTab()).toBe('seguimiento');
});

it('allows only the reporter to close their open case', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'REGISTRADO',
      tipoCaso: 'VULNERABLE',
      autorId: 21,
      descripcion: 'Caso registrado',
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.puedeCerrarCaso()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Cerrar caso');

  component.iniciarCierre();
  component.motivoCierre = 'ENTREGADO_A_CUIDADOR';
  component.entregadoA = 'Familia Pérez';
  component.observacionCierre = 'Fue adoptado';
  component.cerrarCaso();

  expect(casoService.cerrarCaso).toHaveBeenCalledWith(12, {
    motivoCierre: 'ENTREGADO_A_CUIDADOR',
    entregadoA: 'Familia Pérez',
    observacion: 'Fue adoptado',
  });
});

it('opens and cancels the closure dialog without calling the close endpoint', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'REGISTRADO',
      tipoCaso: 'VULNERABLE',
      autorId: 21,
      descripcion: 'Caso registrado',
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  component.iniciarCierre();
  fixture.detectChanges();
  expect(fixture.nativeElement.querySelector('[role="dialog"]')).not.toBeNull();

  fixture.nativeElement.querySelector('[aria-label="Cerrar diálogo"]').click();
  fixture.detectChanges();

  expect(component.mostrandoCierre()).toBe(false);
  expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeNull();
  expect(casoService.cerrarCaso).not.toHaveBeenCalled();
});

it('shows the reunion banner only when closure history records a reunion', () => {
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'RESUELTO',
      tipoCaso: 'EXTRAVIADO',
      descripcion: 'Gato extraviado',
      historial: [
        {
          id: 2,
          estadoNuevo: 'RESUELTO',
          observacion: 'Cierre de caso: REENCUENTRO',
        },
      ],
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('¡El animal volvió a casa!');
  expect(fixture.componentInstance.resultadoCierre()).toBe('REENCUENTRO');
});

it('does not claim a reunion for another resolved stray-animal outcome', () => {
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'RESUELTO',
      tipoCaso: 'EXTRAVIADO',
      descripcion: 'Gato extraviado',
      historial: [
        {
          id: 2,
          estadoNuevo: 'RESUELTO',
          observacion: 'Cierre de caso: RECUPERADO',
        },
      ],
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).not.toContain('¡El animal volvió a casa!');
  expect(fixture.nativeElement.textContent).toContain('Caso resuelto · Recuperado');
});

it('does not show case closure to another citizen or when a foundation has custody', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Otro ciudadano',
      correo: 'otro@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'REGISTRADO',
      autorId: 21,
      descripcion: 'Caso registrado',
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.puedeCerrarCaso()).toBe(false);
  expect(component.puedeAportarCaso()).toBe(true);
  expect(fixture.nativeElement.textContent).not.toContain('Cerrar caso');
  expect(fixture.nativeElement.textContent).toContain('Agregar aporte');
  expect(casoService.cerrarCaso).not.toHaveBeenCalled();
});

it('allows the verified foundation with custody to close the case and sends the required recipient', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'FUNDACION_REFUGIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 31,
      nombre: 'Fundación Patitas',
      correo: 'fundacion@test.co',
      rol: 'FUNDACION_REFUGIO',
      verificado: true,
      disponibleRescate: false,
      organizacionId: 5,
      estadoVerificacionOrganizacion: 'VERIFICADA',
      aceptaCustodia: true,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'EN_ATENCION',
      tipoCaso: 'VULNERABLE',
      fundacionCustodioId: 5,
      descripcion: 'Caso bajo custodia',
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.puedeCerrarCaso()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Cerrar caso');

  component.iniciarCierre();
  component.entregadoA = 'Familia Pérez';
  component.cerrarCaso();

  expect(casoService.cerrarCaso).toHaveBeenCalledWith(12, {
    motivoCierre: 'RECUPERADO',
    entregadoA: 'Familia Pérez',
    observacion: undefined,
  });
});

it('allows administrators to close cases and archive any state accepted by the backend', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'ADMINISTRADOR');
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 12, estado: 'REGISTRADO', tipoCaso: 'VULNERABLE', descripcion: 'Caso nuevo' }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.puedeCerrarCaso()).toBe(true);
  expect(component.puedeArchivarCaso()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Cerrar caso');
  expect(fixture.nativeElement.textContent).toContain('Archivar ahora');

  component.iniciarCierre();
  component.cerrarCaso();
  expect(casoService.cerrarCaso).toHaveBeenCalledWith(12, {
    motivoCierre: 'RECUPERADO',
    entregadoA: undefined,
    observacion: undefined,
  });
});

it('allows citizens outside the case to contribute when a foundation has custody', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'CIUDADANO_VOLUNTARIO');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Otro ciudadano',
      correo: 'otro@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'REGISTRADO',
      autorId: 21,
      fundacionCustodioId: 8,
      descripcion: 'Caso registrado',
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.componentInstance.puedeAportarCaso()).toBe(true);
  expect(fixture.nativeElement.textContent).toContain('Agregar aporte');
});

it('does not let a different veterinarian edit an existing medical log', () => {
  vi.mocked(authService.tieneRol).mockImplementation((role) => role === 'VETERINARIA');
  vi.mocked(usuarioService.getMiPerfil).mockReturnValue(
    of({
      id: 22,
      nombre: 'Dra. Vet',
      correo: 'vet@test.com',
      rol: 'VETERINARIA',
      verificado: true,
      disponibleRescate: false,
    }),
  );
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
      id: 12,
      estado: 'EN_ATENCION',
      descripcion: 'Caso en atención',
      resumenMedico: { id: 33, responsableId: 23, altaMedica: false },
    }),
  );
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();

  expect(fixture.componentInstance.puedeGestionarBitacora()).toBe(false);
  expect(fixture.nativeElement.textContent).not.toContain('Nueva entrada');
  expect(fixture.nativeElement.textContent).not.toContain('Emitir alta médica');
});

function createComponent(): CasoDetalleComponent {
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

it('loads and maps case, animal, location, date, and prioritized photos', () => {
  vi.mocked(casoService.getDetalle).mockReturnValue(of(casoCompleto));
  const component = createComponent();

  expect(casoService.getDetalle).toHaveBeenCalledWith(12);
  expect(component.cargando()).toBe(false);
  expect(component.caso()).toMatchObject({
    id: 12,
    titulo: 'Perro herido',
    especie: 'Perro',
    tamanio: 'Criollo',
    edad: '2 años',
    color: 'Negro · Mancha blanca',
    municipio: 'Armenia',
    barrio: 'Centro (Cerca al parque)',
    foto: 'https://example.test/main.jpg',
  });
  expect(component.fotos()).toEqual([
    'https://example.test/main.jpg',
    'https://example.test/second.jpg',
  ]);
  expect(component.fotoActual()).toBe('https://example.test/main.jpg');
  expect(component.casoDet()).toMatchObject({
    autorId: 21,
    fundacionCustodioId: 5,
    resumenMedico: { altaMedica: false, veterinariaNombre: 'Dra. Vet' },
    aportes: [{ id: 2, comentario: 'Actualización' }],
    historial: [{ id: 1, estadoNuevo: 'REGISTRADO' }],
  });
  expect(component.caso()?.veterinaria).toBe('Dra. Vet');
});

it('uses fallback values and the legacy single-photo fields', () => {
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({ id: 8, fotoPrincipal: ' https://example.test/fallback.jpg ' }),
  );
  const component = createComponent();

  expect(component.caso()).toMatchObject({
    id: 8,
    urgencia: 'MEDIA',
    especie: 'N/A',
    tamanio: 'Mediano',
    edad: 'N/A',
    color: 'N/A',
    municipio: 'N/A',
    barrio: 'N/A',
    foto: 'https://example.test/fallback.jpg',
  });
  expect(component.fotoActual()).toBe('https://example.test/fallback.jpg');
});

it('selects photos and wraps previous/next navigation', () => {
  const component = createComponent();
  component.fotos.set(['first', 'second', 'third']);

  component.selectFoto(2);
  expect(component.fotoActual()).toBe('third');
  component.selectFoto(9);
  expect(component.fotoActual()).toBe('third');

  component.nextFoto();
  expect(component.selectedFotoIndex()).toBe(0);
  component.prevFoto();
  expect(component.selectedFotoIndex()).toBe(2);
});

it('does not move through a gallery with zero or one photo', () => {
  const component = createComponent();
  component.nextFoto();
  component.prevFoto();
  expect(component.selectedFotoIndex()).toBe(0);

  component.fotos.set(['only']);
  component.nextFoto();
  component.prevFoto();
  expect(component.selectedFotoIndex()).toBe(0);
});

it('removes a broken photo and updates the case cover', () => {
  const component = createComponent();
  component.fotos.set(['broken', 'valid']);
  component.selectedFotoIndex.set(0);
  component.caso.set({
    id: 12,
    urgencia: 'MEDIA',
    fechaReporte: 'today',
    fotos: ['broken', 'valid'],
    foto: 'broken',
  });

  component.onFotoError();

  expect(component.fotos()).toEqual(['valid']);
  expect(component.selectedFotoIndex()).toBe(0);
  expect(component.caso()?.foto).toBe('valid');
});

it('handles a broken photo when no case has been loaded', () => {
  const component = createComponent();
  component.fotos.set(['broken']);

  component.onFotoError();

  expect(component.fotos()).toEqual([]);
});

it('logs detail loading errors and ends the loading state', () => {
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.getDetalle).mockReturnValue(throwError(() => new Error('detail')));

  const component = createComponent();

  expect(component.cargando()).toBe(false);
  expect(component.caso()).toBeNull();
  expect(error).toHaveBeenCalled();
});

it('does not request a case when the route has no id', () => {
  routeParams.next({});
  const component = createComponent();

  expect(casoService.getDetalle).not.toHaveBeenCalled();
  expect(component.cargando()).toBe(true);
});

it('changes tabs and formats urgency badges', () => {
  const component = createComponent();
  component.selectTab('bitacora');

  expect(component.activeTab()).toBe('bitacora');
  expect(component.getBadgeClass('critica')).toBe('badge-emergencia');
  expect(component.getBadgeClass('baja')).toBe('badge-media');
  expect(component.getBadgeLabel('ALTA')).toBe('Alta');
  expect(component.getBadgeLabel()).toBe('Media');
});

it('navigates back to the case list', () => {
  const component = createComponent();

  component.goBack();

  expect(router.navigate).toHaveBeenCalledWith(['/casos']);
});
