import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CasoService } from '../../core/services/caso.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import { VerificacionService } from '../../core/services/verificacion.service';
import { ModeracionService } from '../../core/services/moderacion.service';
import { HomeComponent } from './home.component';
import { signal } from '@angular/core';

let casoService: Pick<CasoService, 'getMisReportes' | 'listarCasos'>;
let ubicacionService: Pick<UbicacionService, 'getBarrios' | 'getDepartamentos' | 'getMunicipios'>;
let authService: Pick<AuthService, 'getNombre' | 'getRol'>;
let notificacionService: Pick<NotificacionService, 'sinLeer'>;
let verificacionService: Pick<VerificacionService, 'getPendientes'>;
let moderacionService: Pick<ModeracionService, 'getCasosReportados'>;

function configureHomeServiceMocks(): void {
  casoService = {
    getMisReportes: vi.fn().mockReturnValue(of({ casos: [] })),
    listarCasos: vi.fn().mockReturnValue(of({ content: [] })),
  };
  ubicacionService = {
    getBarrios: vi.fn().mockReturnValue(of([{ id: 3, nombre: 'Centro' }])),
    getDepartamentos: vi.fn().mockReturnValue(of([{ id: 1, nombre: 'Quindío' }])),
    getMunicipios: vi.fn().mockReturnValue(of([{ id: 2, nombre: 'Armenia' }])),
  };
  authService = {
    getNombre: vi.fn().mockReturnValue('Luna'),
    getRol: vi.fn().mockReturnValue('CIUDADANO'),
  };
  notificacionService = { sinLeer: signal(4) };
}

function configureAdminServiceMocks(): void {
  verificacionService = {
    getPendientes: vi.fn().mockReturnValue(
      of([
        {
          usuarioId: 1,
          nombreEntidad: 'Fundación',
          rolSolicitado: 'FUNDACION_REFUGIO',
          estado: 'PENDIENTE',
        },
      ]),
    ),
  };
  moderacionService = {
    getCasosReportados: vi.fn().mockReturnValue(
      of([
        {
          casoId: 2,
          motivo: 'Revisión',
          estadoRevision: 'PENDIENTE',
          fechaCreacion: '2026-01-01',
        },
      ]),
    ),
  };
}

function configureHomeTestingModule(): void {
  TestBed.configureTestingModule({
    imports: [HomeComponent],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: casoService },
      { provide: UbicacionService, useValue: ubicacionService },
      { provide: AuthService, useValue: authService },
      { provide: NotificacionService, useValue: notificacionService },
      { provide: VerificacionService, useValue: verificacionService },
      { provide: ModeracionService, useValue: moderacionService },
    ],
  });
}

beforeEach(() => {
  configureHomeServiceMocks();
  configureAdminServiceMocks();
  configureHomeTestingModule();
});

it('loads and displays administrative summary counts', () => {
  vi.mocked(authService.getRol).mockReturnValue('ADMINISTRADOR');
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of({
      content: [
        {
          id: 44,
          descripcion: 'Gato con collar rojo',
          urgencia: 'ALTA',
          estado: 'REGISTRADO',
          municipio: 'Armenia',
          barrio: 'Centro',
        },
      ],
      totalElements: 7,
    }),
  );
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();

  expect(verificacionService.getPendientes).toHaveBeenCalledOnce();
  expect(moderacionService.getCasosReportados).toHaveBeenCalledOnce();
  expect(casoService.listarCasos).toHaveBeenCalledOnce();
  expect(ubicacionService.getDepartamentos).toHaveBeenCalledOnce();
  expect(fixture.nativeElement.textContent).toContain('Cuentas por validar');
  expect(fixture.nativeElement.textContent).toContain('Contenido por moderar');
  expect(fixture.nativeElement.textContent).toContain('Feed de casos');
  expect(fixture.nativeElement.textContent).toContain('Gato con collar rojo');
  expect(fixture.nativeElement.textContent).toContain('7');
  const pageText = fixture.nativeElement.textContent as string;
  expect(pageText.indexOf('Hola, administrador')).toBeLessThan(pageText.indexOf('Feed de casos'));
  expect(fixture.componentInstance.resumenAdmin()).toEqual({
    cuentasPendientes: 1,
    casosReportados: 1,
  });
});

it('loads the citizen dashboard, locations, feed, and personal reports', () => {
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Hola, Luna');
  expect(fixture.nativeElement.textContent).toContain('No hay casos disponibles.');
  expect(casoService.getMisReportes).toHaveBeenCalledWith(0, 50);
  expect(ubicacionService.getDepartamentos).toHaveBeenCalledOnce();
  expect(casoService.listarCasos).toHaveBeenCalledOnce();
  expect(fixture.nativeElement.textContent).toContain('4');
});

it('maps feed aliases, defaults, photo and role-specific computed values', () => {
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of({
      content: [
        {
          id: 7,
          descripcion: 'Perro encontrado',
          urgencia: 'critica',
          tipo: 'herido',
          estado: 'en_atencion',
          municipio: 'Armenia',
          barrio: 'Centro',
          fotografias: [{ url: 'https://example.test/dog.jpg' }],
          fundacionCustodio: 'Luna',
        },
        { id: 8, descripcion: 'Gato', barrio: 'Norte' },
      ],
    }),
  );
  vi.mocked(authService.getRol).mockReturnValue('FUNDACION');

  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.isFundacion()).toBe(true);
  expect(component.casos()).toMatchObject([
    {
      id: 7,
      titulo: 'Perro encontrado',
      urgencia: 'CRITICA',
      tipoCaso: 'HERIDO',
      estado: 'EN_ATENCION',
      foto: 'https://example.test/dog.jpg',
      fundacion: 'Luna',
    },
    { id: 8, urgencia: 'MEDIA', estado: 'REGISTRADO', foto: '' },
  ]);
  expect(component.getPanelLink()).toBe('/fundacion');
});

it('loads every report page and counts only open and resolved reports', () => {
  vi.mocked(casoService.getMisReportes)
    .mockReturnValueOnce(
      of({
        content: [
          { id: 1, estado: 'REGISTRADO', descripcion: 'Abierto' },
          { id: 2, estado: 'ARCHIVADO', descripcion: 'Archivado' },
        ],
        number: 0,
        totalPages: 2,
      }),
    )
    .mockReturnValueOnce(
      of({
        content: [
          { id: 3, estado: 'RESUELTO', descripcion: 'Resuelto' },
          { id: 4, estado: 'EN_ATENCION', descripcion: 'En atención' },
        ],
        number: 1,
        totalPages: 2,
      }),
    );

  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(casoService.getMisReportes).toHaveBeenNthCalledWith(1, 0, 50);
  expect(casoService.getMisReportes).toHaveBeenNthCalledWith(2, 1, 50);
  expect(component.misReportesActivos().map((caso) => caso.id)).toEqual([1, 4]);
  expect(component.misReportesResueltos().map((caso) => caso.id)).toEqual([3]);
});

it('builds feed filters from selected values', () => {
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.filtros = {
    departamentoId: 1,
    municipioId: 2,
    barrioId: 3,
    urgencia: 'ALTA',
    tipoAnimal: 'PERRO',
  };

  component.onSearch();

  expect(casoService.listarCasos).toHaveBeenLastCalledWith({
    departamentoId: 1,
    municipioId: 2,
    barrioId: 3,
    urgencia: 'ALTA',
    especie: 'PERRO',
    page: 0,
    size: 10,
  });
});

it('loads dependent locations and clears child selections', () => {
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.filtros.departamentoId = 1;
  component.filtros.municipioId = 2;
  component.filtros.barrioId = 3;

  component.onDepartamentoChange();

  expect(ubicacionService.getMunicipios).toHaveBeenCalledWith(1);
  expect(component.municipios()).toEqual([{ id: 2, nombre: 'Armenia' }]);
  expect(component.filtros.municipioId).toBe('');
  expect(component.filtros.barrioId).toBe('');

  component.filtros.municipioId = 2;
  component.onMunicipioChange();
  expect(ubicacionService.getBarrios).toHaveBeenCalledWith(2);
  expect(component.barrios()).toEqual([{ id: 3, nombre: 'Centro' }]);
  expect(component.filtros.barrioId).toBe('');
});

it('clears dependent locations when a parent selection is removed', () => {
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.municipios.set([{ id: 2, nombre: 'Armenia' }]);
  component.barrios.set([{ id: 3, nombre: 'Centro' }]);
  component.filtros.departamentoId = '';

  component.onDepartamentoChange();

  expect(component.municipios()).toEqual([]);
  expect(component.barrios()).toEqual([]);
});

it('shows feed and report errors without leaving loading states active', () => {
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.listarCasos).mockReturnValue(throwError(() => new Error('feed')));
  vi.mocked(casoService.getMisReportes).mockReturnValue(throwError(() => new Error('reports')));

  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.errorMensaje()).toContain('Error al cargar los casos');
  expect(component.cargando()).toBe(false);
  expect(component.misReportes()).toEqual([]);
  expect(component.cargandoMisReportes()).toBe(false);
  expect(error).toHaveBeenCalled();
});

it('maps status labels, urgency labels, type labels, and panel links', () => {
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.getBadgeClass('baja')).toBe('badge-media');
  expect(component.getBadgeLabel('critica')).toBe('Emergencia');
  expect(component.getEstadoLabel('resuelto')).toBe('Resuelto');
  expect(component.getEstadoLabel()).toBe('Registrado');
  expect(component.getTipoLabel('en_adopcion')).toBe('En adopción');
  expect(component.getTipoLabel()).toBe('');

  component.rol.set('VETERINARIA');
  expect(component.getPanelLink()).toBe('/veterinaria');
  component.rol.set('ADMINISTRADOR');
  expect(component.getPanelLink()).toBe('/admin');
  component.rol.set(null);
  expect(component.getPanelLink()).toBe('/');
});
