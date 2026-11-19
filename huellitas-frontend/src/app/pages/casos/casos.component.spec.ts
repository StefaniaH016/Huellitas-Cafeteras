import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CasoService } from '../../core/services/caso.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import { CasosComponent } from './casos.component';

let casoService: Pick<CasoService, 'listarCasos'>;
let ubicacionService: Pick<UbicacionService, 'getBarrios' | 'getDepartamentos' | 'getMunicipios'>;
let router: Router;

beforeEach(() => {
  casoService = { listarCasos: vi.fn().mockReturnValue(of({ content: [] })) };
  ubicacionService = {
    getBarrios: vi.fn().mockReturnValue(of([])),
    getDepartamentos: vi.fn().mockReturnValue(of([])),
    getMunicipios: vi.fn().mockReturnValue(of([])),
  };
  TestBed.configureTestingModule({
    imports: [CasosComponent],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: casoService },
      { provide: UbicacionService, useValue: ubicacionService },
    ],
  });
  router = TestBed.inject(Router);
  vi.spyOn(router, 'navigate').mockResolvedValue(true);
});

afterEach(() => vi.restoreAllMocks());

function createComponent(): CasosComponent {
  const fixture = TestBed.createComponent(CasosComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

it('loads and maps the cases feed on initialization', () => {
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of({
      content: [
        {
          id: 1,
          descripcion: 'Animal herido',
          urgencia: 'critica',
          tipoCaso: 'herido',
          estado: 'en_atencion',
          municipio: 'Armenia',
          barrio: 'Centro',
          fechaCreacion: '2026-10-01T00:00:00Z',
          fotoPrincipal: 'https://example.test/animal.jpg',
        },
        { id: 2, tipo: 'vulnerable', fechaReporte: 'Ayer' },
      ],
      totalElements: 2,
    }),
  );
  const fixture = TestBed.createComponent(CasosComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;

  expect(component.casos()).toMatchObject([
    {
      id: 1,
      titulo: 'Animal herido',
      urgencia: 'CRITICA',
      tipo: 'HERIDO',
      estado: 'EN_ATENCION',
      municipio: 'Armenia',
      barrio: 'Centro',
      foto: 'https://example.test/animal.jpg',
    },
    { id: 2, urgencia: 'MEDIA', tipo: 'VULNERABLE', estado: 'REGISTRADO', fechaReporte: 'Ayer' },
  ]);
  expect(component.total()).toBe(2);
  expect(component.cargando()).toBe(false);
  expect(casoService.listarCasos).toHaveBeenCalledWith({
    page: 0,
    size: 50,
    estados: 'REGISTRADO,EN_ATENCION',
  });
});

it('shows case filters in a horizontal toolbar with a responsive location selector', () => {
  const fixture = TestBed.createComponent(CasosComponent);
  fixture.detectChanges();

  const filtersBar = fixture.nativeElement.querySelector('.filters-bar') as HTMLElement;
  expect(filtersBar).not.toBeNull();
  expect(filtersBar.querySelectorAll('details.filter-dropdown')).toHaveLength(4);
  expect(filtersBar.querySelector('#check-estado-resuelto')).toBeNull();
  expect(filtersBar.querySelector('app-ubicacion-selector fieldset')?.classList).toContain(
    'stacked',
  );
});

it('shows each case primary photo and falls back to the paw icon when it fails', () => {
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of({
      content: [
        {
          id: 8,
          descripcion: 'Caso con fotografía',
          tipoCaso: 'HERIDO',
          estado: 'REGISTRADO',
          urgencia: 'ALTA',
          fotoPrincipal: 'https://example.test/primary.jpg',
        },
      ],
    }),
  );
  const fixture = TestBed.createComponent(CasosComponent);
  fixture.detectChanges();

  const image = fixture.nativeElement.querySelector('.list-thumb-img') as HTMLImageElement;
  expect(image.getAttribute('src')).toBe('https://example.test/primary.jpg');

  image.dispatchEvent(new Event('error'));
  fixture.detectChanges();

  expect(fixture.nativeElement.querySelector('.list-thumb-img')).toBeNull();
  expect(fixture.nativeElement.querySelector('.list-thumb-fallback')?.textContent).toContain('🐾');
});

it('shows adoption as an available case type filter', () => {
  const fixture = TestBed.createComponent(CasosComponent);
  fixture.detectChanges();
  const adoptionFilter = fixture.nativeElement.querySelector(
    '#check-tipo-adopcion',
  ) as HTMLInputElement;

  expect(adoptionFilter.checked).toBe(true);
});

it('builds applied location and multi-choice filter parameters for the backend', () => {
  const component = createComponent();
  component.deptoId.set(1);
  component.munId.set(2);
  component.barrioId.set(3);
  component.urgencias.set(['ALTA']);
  component.tipos.set(['HERIDO']);
  component.estados.set(['REGISTRADO']);

  expect(component.buildFilterParams()).toEqual({
    page: 0,
    size: 50,
    departamentoId: 1,
    municipioId: 2,
    barrioId: 3,
    urgencias: 'ALTA',
    tiposCaso: 'HERIDO',
    estados: 'REGISTRADO',
  });
});

it('toggles urgency, type, and status filters in both directions', () => {
  const component = createComponent();
  component.toggleUrgencia('CRITICA');
  component.toggleTipo('HERIDO');
  component.toggleEstado('REGISTRADO');
  expect(component.urgenciasSeleccionadas()).not.toContain('CRITICA');
  expect(component.tiposSeleccionados()).not.toContain('HERIDO');
  expect(component.estadosSeleccionados()).not.toContain('REGISTRADO');
  expect(component.urgencias()).toContain('CRITICA');
  expect(component.tipos()).toContain('HERIDO');
  expect(component.estados()).toContain('REGISTRADO');

  component.toggleUrgencia('CRITICA');
  component.toggleTipo('HERIDO');
  component.toggleEstado('REGISTRADO');
  expect(component.urgenciasSeleccionadas()).toContain('CRITICA');
  expect(component.tiposSeleccionados()).toContain('HERIDO');
  expect(component.estadosSeleccionados()).toContain('REGISTRADO');
});

it('keeps current results until applying draft filter selections', () => {
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of([
      { id: 1, urgencia: 'CRITICA', tipoCaso: 'HERIDO', estado: 'REGISTRADO' },
      { id: 2, urgencia: 'BAJA', tipoCaso: 'EXTRAVIADO', estado: 'RESUELTO' },
    ]),
  );
  const component = createComponent();

  expect(component.casos().map((caso) => caso.id)).toEqual([1]);
  expect(component.total()).toBe(1);

  vi.mocked(casoService.listarCasos).mockClear();
  component.toggleUrgencia('CRITICA');
  component.toggleTipo('HERIDO');
  component.toggleEstado('REGISTRADO');
  component.toggleEstado('RESUELTO');

  expect(casoService.listarCasos).not.toHaveBeenCalled();
  expect(component.casos().map((caso) => caso.id)).toEqual([1]);

  component.aplicarFiltros();

  expect(component.casos().map((caso) => caso.id)).toEqual([2]);
  expect(component.total()).toBe(1);
});

it('sends checkbox selections to the API when applying filters', () => {
  const component = createComponent();
  component.toggleUrgencia('CRITICA');
  component.toggleTipo('HERIDO');
  component.toggleEstado('REGISTRADO');
  component.toggleEstado('RESUELTO');

  vi.mocked(casoService.listarCasos).mockClear();
  expect(casoService.listarCasos).not.toHaveBeenCalled();
  component.aplicarFiltros();

  expect(casoService.listarCasos).toHaveBeenLastCalledWith({
    page: 0,
    size: 50,
    urgencias: 'ALTA,MEDIA,BAJA',
    tiposCaso: 'VULNERABLE,EXTRAVIADO,EN_ADOPCION',
    estados: 'EN_ATENCION,RESUELTO',
  });
});

it('supports array responses and legacy case type aliases', () => {
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of([{ id: 4, tipo: 'herido', foto: 'https://example.test/photo.jpg' }]),
  );
  const component = createComponent();

  expect(component.casos()[0]).toMatchObject({
    id: 4,
    tipo: 'HERIDO',
    foto: 'https://example.test/photo.jpg',
    titulo: '',
  });
});

it('updates the selected location ids', () => {
  const component = createComponent();
  component.onDeptoChange(1);
  component.onMunChange(2);
  component.onBarrioChange(3);

  expect(component.filtroDeptoId()).toBe(1);
  expect(component.filtroMunId()).toBe(2);
  expect(component.filtroBarrioId()).toBe(3);
  expect(component.deptoId()).toBeNull();
  expect(component.munId()).toBeNull();
  expect(component.barrioId()).toBeNull();

  component.aplicarFiltros();

  expect(component.deptoId()).toBe(1);
  expect(component.munId()).toBe(2);
  expect(component.barrioId()).toBe(3);
});

it('applies filters from the first page', () => {
  const component = createComponent();
  vi.mocked(casoService.listarCasos).mockClear();
  component.page.set(4);

  component.onAplicarFiltros();

  expect(component.page()).toBe(0);
  expect(casoService.listarCasos).toHaveBeenCalledOnce();
});

it('moves between pages only when the boundary permits', () => {
  const component = createComponent();
  vi.mocked(casoService.listarCasos).mockClear();
  component.prevPage();
  expect(component.page()).toBe(0);
  expect(casoService.listarCasos).not.toHaveBeenCalled();

  component.page.set(1);
  component.prevPage();
  expect(component.page()).toBe(0);
  expect(casoService.listarCasos).toHaveBeenCalledOnce();

  vi.mocked(casoService.listarCasos).mockClear();
  component.pageSize = 1;
  component.casos.set([
    {
      id: 5,
      descripcion: 'Caso',
      urgencia: 'ALTA',
      tipo: 'HERIDO',
      estado: 'REGISTRADO',
      municipio: '',
      barrio: '',
      fechaReporte: '',
    },
  ]);
  component.nextPage();
  expect(component.page()).toBe(1);
  expect(casoService.listarCasos).toHaveBeenCalledOnce();

  component.casos.set([]);
  component.nextPage();
  expect(component.page()).toBe(1);
});

it('stops the loading indicator and logs when loading fails', () => {
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.listarCasos).mockReturnValue(throwError(() => new Error('feed')));
  const component = createComponent();

  expect(component.cargando()).toBe(false);
  expect(component.casos()).toEqual([]);
  expect(error).toHaveBeenCalled();
});

it('navigates to a selected case and maps urgency badges', () => {
  const component = createComponent();
  component.onVerCaso(13);

  expect(router.navigate).toHaveBeenCalledWith(['/caso-detalle', 13]);
  expect(component.getBadgeClass('ALTA')).toBe('badge-alta');
  expect(component.getBadgeClass('unknown')).toBe('badge-atencion');
  expect(component.getBadgeLabel('CRITICA')).toBe('Emergencia');
  expect(component.getBadgeLabel('unknown')).toBe('unknown');
});
