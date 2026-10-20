import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CasoService } from '../../core/services/caso.service';
import { PanelFundacionComponent } from './panel-fundacion.component';

beforeEach(() => {
  TestBed.configureTestingModule({
    imports: [PanelFundacionComponent],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: {} },
      { provide: AuthService, useValue: { getNombre: vi.fn().mockReturnValue('Patitas') } },
    ],
  });
});

it('renders the foundation dashboard and initializes empty state', () => {
  const fixture = TestBed.createComponent(PanelFundacionComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Fundación Patitas');
  expect(fixture.nativeElement.textContent).toContain('No hay emergencias en tu zona.');
  expect(fixture.componentInstance.fundacionStats()).toEqual({
    enCustodia: 0,
    esperandoAtencion: 0,
    emergenciasZona: 0,
  });
  expect(fixture.componentInstance.cargando()).toBe(false);
});

it('resets foundation data when reloading', () => {
  const component = TestBed.createComponent(PanelFundacionComponent).componentInstance;
  component.fundacionStats.set({ enCustodia: 4, esperandoAtencion: 2, emergenciasZona: 1 });
  component.fundacionAlertas.set([
    { id: 1, descripcion: 'Caso', urgencia: 'ALTA', municipio: '', barrio: '' },
  ]);

  component.cargarDatos();

  expect(component.fundacionStats()).toEqual({
    enCustodia: 0,
    esperandoAtencion: 0,
    emergenciasZona: 0,
  });
  expect(component.fundacionAlertas()).toEqual([]);
  expect(component.fundacionAnimales()).toEqual([]);
  expect(component.errorMensaje()).toBe('');
  expect(component.cargando()).toBe(false);
});

it('delegates badge helpers and scrolls to the panel', () => {
  const component = TestBed.createComponent(PanelFundacionComponent).componentInstance;
  const scrollIntoView = vi.fn();
  vi.spyOn(document, 'getElementById').mockReturnValue({
    scrollIntoView,
  } as unknown as HTMLElement);

  expect(component.getBadgeClass('CRITICA')).toBe('badge-emergencia');
  expect(component.getBadgeLabel('ALTA')).toBe('Extraviado');
  component.scrollToPanel();
  expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
});
