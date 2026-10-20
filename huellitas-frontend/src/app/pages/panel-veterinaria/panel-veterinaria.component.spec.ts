import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CasoService } from '../../core/services/caso.service';
import { PanelVeterinariaComponent } from './panel-veterinaria.component';

beforeEach(() => {
  TestBed.configureTestingModule({
    imports: [PanelVeterinariaComponent],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: {} },
      { provide: AuthService, useValue: { getNombre: vi.fn().mockReturnValue('Vet Norte') } },
    ],
  });
});

it('renders the veterinary dashboard and initializes empty state', () => {
  const fixture = TestBed.createComponent(PanelVeterinariaComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Veterinaria Vet Norte');
  expect(fixture.nativeElement.textContent).toContain('No hay solicitudes pendientes.');
  expect(fixture.componentInstance.veterinariaStats()).toEqual({
    solicitudes: 0,
    pacientesActivos: 0,
  });
  expect(fixture.componentInstance.cargando()).toBe(false);
});

it('resets veterinary data when reloading', () => {
  const component = TestBed.createComponent(PanelVeterinariaComponent).componentInstance;
  component.veterinariaStats.set({ solicitudes: 3, pacientesActivos: 4 });
  component.veterinariaSolicitudes.set([
    { id: 1, descripcion: 'Caso', urgencia: 'ALTA', municipio: '', barrio: '' },
  ]);

  component.cargarDatos();

  expect(component.veterinariaStats()).toEqual({ solicitudes: 0, pacientesActivos: 0 });
  expect(component.veterinariaSolicitudes()).toEqual([]);
  expect(component.veterinariaPacientes()).toEqual([]);
  expect(component.errorMensaje()).toBe('');
  expect(component.cargando()).toBe(false);
});

it('delegates badge helpers and scrolls to the panel', () => {
  const component = TestBed.createComponent(PanelVeterinariaComponent).componentInstance;
  const scrollIntoView = vi.fn();
  vi.spyOn(document, 'getElementById').mockReturnValue({
    scrollIntoView,
  } as unknown as HTMLElement);

  expect(component.getBadgeClass('EXTRAVIADO')).toBe('badge-alta');
  expect(component.getBadgeLabel('CRITICA')).toBe('Emergencia');
  component.scrollToPanel();
  expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
});
