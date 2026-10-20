import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Params, provideRouter, Router } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { CasoService } from '../../core/services/caso.service';
import { CasoDetalleComponent } from './caso-detalle.component';

let casoService: Pick<CasoService, 'getDetalle'>;
let route: Pick<ActivatedRoute, 'params'>;
let routeParams: BehaviorSubject<Params>;
let router: Router;

beforeEach(() => {
  casoService = {
    getDetalle: vi.fn().mockReturnValue(of({ id: 12, descripcion: 'Caso de prueba' })),
  };
  routeParams = new BehaviorSubject<Params>({ id: '12' });
  route = { params: routeParams.asObservable() };
  TestBed.configureTestingModule({
    imports: [CasoDetalleComponent],
    providers: [
      provideRouter([]),
      { provide: ActivatedRoute, useValue: route },
      { provide: CasoService, useValue: casoService },
    ],
  });
  router = TestBed.inject(Router);
  vi.spyOn(router, 'navigate').mockResolvedValue(true);
});

afterEach(() => vi.restoreAllMocks());

function createComponent(): CasoDetalleComponent {
  const fixture = TestBed.createComponent(CasoDetalleComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

it('loads and maps case, animal, location, date, and prioritized photos', () => {
  vi.mocked(casoService.getDetalle).mockReturnValue(
    of({
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
    }),
  );
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
