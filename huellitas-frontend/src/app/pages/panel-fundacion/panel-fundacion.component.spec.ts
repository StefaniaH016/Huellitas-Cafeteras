import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CasoService } from '../../core/services/caso.service';
import { PanelFundacionComponent } from './panel-fundacion.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

beforeEach(() => {
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
        },
      },
      { provide: AuthService, useValue: { getNombre: vi.fn().mockReturnValue('Patitas') } },
    ],
  });
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
  vi.spyOn(document, 'getElementById').mockReturnValue({
    scrollIntoView,
  } as unknown as HTMLElement);

  expect(component.getBadgeClass('CRITICA')).toBe('badge-emergencia');
  expect(component.getBadgeLabel('ALTA')).toBe('Alta');
  component.scrollToPanel();
  expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
});
