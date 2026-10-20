import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CasoService } from '../../core/services/caso.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import { HomeComponent } from './home.component';

let casoService: Pick<CasoService, 'getMisReportes' | 'listarCasos'>;
let ubicacionService: Pick<UbicacionService, 'getBarrios' | 'getDepartamentos' | 'getMunicipios'>;
let authService: Pick<AuthService, 'getNombre' | 'getRol'>;

beforeEach(() => {
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

  TestBed.configureTestingModule({
    imports: [HomeComponent],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: casoService },
      { provide: UbicacionService, useValue: ubicacionService },
      { provide: AuthService, useValue: authService },
    ],
  });
});

it('loads the citizen dashboard, locations, feed, and personal reports', () => {
  const fixture = TestBed.createComponent(HomeComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Hola, Luna');
  expect(fixture.nativeElement.textContent).toContain('No hay casos disponibles.');
  expect(casoService.getMisReportes).toHaveBeenCalledWith(0);
  expect(ubicacionService.getDepartamentos).toHaveBeenCalledOnce();
  expect(casoService.listarCasos).toHaveBeenCalledOnce();
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
  expect(component.casosEnCustodia()).toHaveLength(1);
  expect(component.emergenciasZona()).toHaveLength(1);
  expect(component.esperandoAtencionMedica()).toHaveLength(1);
  expect(component.getPanelLink()).toBe('/fundacion');
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
