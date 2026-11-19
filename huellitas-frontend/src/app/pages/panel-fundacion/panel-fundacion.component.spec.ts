import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CasoService } from '../../core/services/caso.service';
import { PanelFundacionComponent } from './panel-fundacion.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';

let gestionarCustodia: ReturnType<typeof vi.fn>;

beforeEach(() => {
  gestionarCustodia = vi.fn().mockReturnValue(of({}));
  TestBed.configureTestingModule({
    imports: [PanelFundacionComponent, HttpClientTestingModule],
    providers: [
      provideRouter([]),
      {
        provide: CasoService,
        useValue: {
          listarCasos: vi.fn().mockReturnValue(of({ content: [] })),
          listarCasosEnCustodia: vi.fn().mockReturnValue(of({ content: [] })),
          listarCasosSinCustodia: vi.fn().mockReturnValue(of({ content: [] })),
          gestionarCustodia,
        },
      },
      { provide: AuthService, useValue: { getNombre: vi.fn().mockReturnValue('Patitas') } },
    ],
  });
});

afterEach(() => {
  vi.restoreAllMocks();
});

it('renders the foundation dashboard and initializes empty state', () => {
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Mi panel · Patitas');
  expect(fixture.nativeElement.textContent).toContain('Cargando casos...');
  expect(fixture.componentInstance.fundacionStats()).toEqual({
    enCustodia: 0,
    casosSinCustodia: 0,
  });
  expect(fixture.componentInstance.cargando()).toBe(true);
});

it('resets foundation data when reloading', () => {
  const component = TestBed.createComponent(PanelFundacionComponent).componentInstance;
  // Set initial data
  component.fundacionStats.set({ enCustodia: 4, casosSinCustodia: 1 });
  component.fundacionAlertas.set([
    { id: 1, descripcion: 'Caso', urgencia: 'ALTA', municipio: '', barrio: '' },
  ]);

  // cargarDatos() resets the UI state
  component.cargarDatos();

  // After reset, stats should be back to initial empty state
  expect(component.fundacionStats()).toEqual({
    enCustodia: 0,
    casosSinCustodia: 0,
  });
  expect(component.fundacionAlertas()).toEqual([]);
  expect(component.fundacionAnimales()).toEqual([]);
  expect(component.errorMensaje()).toBe('');
  expect(component.cargando()).toBe(true);
});

it('delegates badge helpers and scrolls to the panel', () => {
  const component = TestBed.createComponent(PanelFundacionComponent).componentInstance;
  const scrollIntoView = vi.fn();
  const getElementByIdSpy = vi.spyOn(document, 'getElementById').mockReturnValue({
    scrollIntoView,
  } as unknown as HTMLElement);

  expect(component.getBadgeClass('CRITICA')).toBe('badge-emergencia');
  expect(component.getBadgeLabel('ALTA')).toBe('Alta');
  component.scrollToPanel();
  expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
  getElementByIdSpy.mockRestore();
});

it('scrolls cupo figures without navigating to a login route', () => {
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();
  const _component = fixture.componentInstance;
  const scrollIntoView = vi.fn();
  const getElementByIdSpy = vi.spyOn(document, 'getElementById').mockReturnValue({
    scrollIntoView,
  } as unknown as HTMLElement);

  const figures = (fixture.nativeElement as HTMLElement).querySelectorAll('.dash-figures button');
  expect(figures).toHaveLength(3);
  expect((fixture.nativeElement as HTMLElement).querySelector('.dash-figures a')).toBeNull();

  (figures[2] as HTMLButtonElement).click();

  expect(getElementByIdSpy).toHaveBeenCalledWith('custodia');
  expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
  getElementByIdSpy.mockRestore();
});

it('routes the foundation close action to the case detail closure dialog', () => {
  const component = TestBed.createComponent(PanelFundacionComponent).componentInstance;
  const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

  component.cerrarDesdePanel(12);

  expect(navigate).toHaveBeenCalledWith(['/caso-detalle', 12], {
    queryParams: { accion: 'cerrar' },
  });
});

it('shows active custody cases before resolved cases', () => {
  const service = TestBed.inject(CasoService);
  vi.spyOn(service, 'listarCasosEnCustodia').mockReturnValue(
    of({
      content: [
        {
          id: 2,
          descripcion: 'En atención',
          estado: 'EN_ATENCION',
          urgencia: 'MEDIA',
          municipio: 'Armenia',
          barrio: 'Centro',
        },
      ],
      totalElements: 1,
    }),
  );
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();

  TestBed.inject(HttpTestingController)
    .expectOne((request) => request.url === `${environment.apiUrl}/usuarios/me`)
    .flush({
      id: 1,
      nombre: 'Patitas',
      correo: 'fundacion@example.test',
      rol: 'FUNDACION_REFUGIO',
      verificado: true,
      municipioId: 1,
    });
  fixture.detectChanges();

  const statusBadges = (fixture.nativeElement as HTMLElement).querySelectorAll(
    '#custodia .list-row > .badge',
  );
  expect(Array.from(statusBadges).map((badge) => badge.textContent?.trim())).toEqual([
    'En atención',
  ]);
});

it('allows reloading the custody panel', () => {
  const service = TestBed.inject(CasoService);
  vi.spyOn(service, 'listarCasosEnCustodia').mockReturnValue(
    of({
      content: [],
      totalElements: 0,
    }),
  );
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();

  TestBed.inject(HttpTestingController)
    .expectOne((request) => request.url === `${environment.apiUrl}/usuarios/me`)
    .flush({
      id: 1,
      nombre: 'Patitas',
      correo: 'fundacion@example.test',
      rol: 'FUNDACION_REFUGIO',
      verificado: true,
      municipioId: 1,
    });
  fixture.detectChanges();

  const component = fixture.componentInstance;
  const cargarDatos = vi.spyOn(component, 'cargarDatos');
  component.cargando.set(false);
  fixture.detectChanges();
  const refreshButton = Array.from(
    (fixture.nativeElement as HTMLElement).querySelectorAll('button'),
  ).find((button) => button.textContent?.trim() === 'Actualizar');

  refreshButton?.click();

  expect(cargarDatos).toHaveBeenCalledOnce();
});

it('does not show custody release button for resolved or archived cases', () => {
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.cargando.set(false);

  component.fundacionAnimales.set([
    {
      id: 1,
      descripcion: 'Caso resuelto',
      urgencia: 'MEDIA',
      municipio: 'Armenia',
      barrio: 'Centro',
      estado: 'RESUELTO',
    },
    {
      id: 2,
      descripcion: 'Caso archivado',
      urgencia: 'MEDIA',
      municipio: 'Armenia',
      barrio: 'Centro',
      estado: 'ARCHIVADO',
    },
  ]);
  fixture.detectChanges();

  const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
  const buttonLabels = Array.from(buttons).map((button) => button.textContent?.trim());
  expect(buttonLabels).not.toContain('Liberar custodia');
  const statusBadges = (fixture.nativeElement as HTMLElement).querySelectorAll(
    '#custodia .list-row > .badge',
  );
  expect(Array.from(statusBadges).map((badge) => badge.textContent?.trim())).toEqual([
    'Resuelto',
    'Archivado',
  ]);
});

it('does not allow custody release submission for resolved or archived cases', () => {
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.cargando.set(false);

  component.fundacionAnimales.set([
    {
      id: 1,
      descripcion: 'Caso resuelto',
      urgencia: 'MEDIA',
      municipio: 'Armenia',
      barrio: 'Centro',
      estado: 'RESUELTO',
    },
    {
      id: 2,
      descripcion: 'Caso archivado',
      urgencia: 'MEDIA',
      municipio: 'Armenia',
      barrio: 'Centro',
      estado: 'ARCHIVADO',
    },
  ]);
  fixture.detectChanges();

  const closeButtons = Array.from(
    (fixture.nativeElement as HTMLElement).querySelectorAll('button'),
  ).filter((button) => button.textContent?.trim() === 'Cerrar caso');
  expect(closeButtons).toHaveLength(2);
  expect(closeButtons.every((button) => button.disabled)).toBe(true);

  component.iniciarLiberacion(1);
  component.iniciarLiberacion(2);
  expect(component.liberandoId()).toBeNull();

  component.motivoLiberacion = 'Caso cerrado';
  component.liberarCustodia(1);
  component.liberarCustodia(2);
  expect(gestionarCustodia).not.toHaveBeenCalled();
});

it('allows releasing custody for an active case', () => {
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.cargando.set(false);
  component.fundacionAnimales.set([
    {
      id: 3,
      descripcion: 'Caso activo',
      urgencia: 'MEDIA',
      municipio: 'Armenia',
      barrio: 'Centro',
      estado: 'EN_ATENCION',
    },
  ]);
  fixture.detectChanges();

  expect(
    Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button')).some(
      (button) => button.textContent?.trim() === 'Liberar custodia',
    ),
  ).toBe(true);
  expect(
    (fixture.nativeElement as HTMLElement)
      .querySelector('#custodia .list-row > .badge')
      ?.textContent?.trim(),
  ).toBe('En atención');

  component.iniciarLiberacion(3);
  expect(component.liberandoId()).toBe(3);
});
