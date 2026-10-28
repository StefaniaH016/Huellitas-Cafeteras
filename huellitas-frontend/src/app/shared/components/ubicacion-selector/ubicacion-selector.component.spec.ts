import { TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { of, throwError } from 'rxjs';
import { UbicacionService } from '../../../core/services/ubicacion.service';
import { UbicacionSelectorComponent } from './ubicacion-selector.component';

let ubicacionService: Pick<UbicacionService, 'getBarrios' | 'getDepartamentos' | 'getMunicipios'>;

beforeEach(() => {
  ubicacionService = {
    getBarrios: vi.fn().mockReturnValue(of([{ id: 3, nombre: 'Centro', municipioId: 2 }])),
    getDepartamentos: vi.fn().mockReturnValue(of([{ id: 1, nombre: 'Quindío' }])),
    getMunicipios: vi.fn().mockReturnValue(of([{ id: 2, nombre: 'Armenia', departamentoId: 1 }])),
  };
  TestBed.configureTestingModule({
    imports: [UbicacionSelectorComponent],
    providers: [{ provide: UbicacionService, useValue: ubicacionService }],
  });
});

afterEach(() => vi.restoreAllMocks());

it('loads departments and preselected child lists on init', () => {
  const fixture = TestBed.createComponent(UbicacionSelectorComponent);
  fixture.componentInstance.deptoId = 1;
  fixture.componentInstance.munId = 2;
  fixture.componentInstance.barrioId = 3;
  fixture.detectChanges();

  expect(ubicacionService.getDepartamentos).toHaveBeenCalledOnce();
  expect(ubicacionService.getMunicipios).toHaveBeenCalledWith(1);
  expect(ubicacionService.getBarrios).toHaveBeenCalledWith(2);
  expect(fixture.componentInstance.selectedDeptoId()).toBe(1);
  expect(fixture.componentInstance.selectedMunId()).toBe(2);
  expect(fixture.componentInstance.selectedBarrioId()).toBe(3);
  expect(fixture.componentInstance.loading).toBe(false);
});

it('updates dependent selections and emits their values', () => {
  const component = TestBed.createComponent(UbicacionSelectorComponent).componentInstance;
  const departamento = vi.spyOn(component.deptoChange, 'emit');
  const municipio = vi.spyOn(component.munChange, 'emit');
  const barrio = vi.spyOn(component.barrioChange, 'emit');

  component.onDeptoChange('1');
  expect(component.selectedDeptoId()).toBe(1);
  expect(component.selectedMunId()).toBeNull();
  expect(component.selectedBarrioId()).toBeNull();
  expect(departamento).toHaveBeenCalledWith(1);
  expect(municipio).toHaveBeenCalledWith(null);
  expect(barrio).toHaveBeenCalledWith(null);
  expect(component.municipios).toEqual([{ id: 2, nombre: 'Armenia', departamentoId: 1 }]);

  component.onMunChange('2');
  expect(component.selectedMunId()).toBe(2);
  expect(municipio).toHaveBeenLastCalledWith(2);
  expect(component.barrios).toEqual([{ id: 3, nombre: 'Centro', municipioId: 2 }]);

  component.onBarrioChange('3');
  expect(component.selectedBarrioId()).toBe(3);
  expect(barrio).toHaveBeenLastCalledWith(3);
});

it('clears child data when parent selections are cleared', () => {
  const component = TestBed.createComponent(UbicacionSelectorComponent).componentInstance;
  component.onDeptoChange('1');
  component.onMunChange('2');
  component.onMunChange('');
  expect(component.selectedMunId()).toBeNull();
  expect(component.selectedBarrioId()).toBeNull();
  expect(component.barrios).toEqual([]);

  component.onDeptoChange('');
  expect(component.selectedDeptoId()).toBeNull();
  expect(component.municipios).toEqual([]);
  expect(component.barrios).toEqual([]);
});

it('updates selected values when inputs change after initialization', () => {
  const component = TestBed.createComponent(UbicacionSelectorComponent).componentInstance;
  component.ngOnChanges({
    deptoId: new SimpleChange(null, 1, false),
    munId: new SimpleChange(null, 2, false),
    barrioId: new SimpleChange(null, 3, false),
  });

  expect(component.selectedDeptoId()).toBe(1);
  expect(component.selectedMunId()).toBe(2);
  expect(component.selectedBarrioId()).toBe(3);
});

it('exposes loading errors for each location level', () => {
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(ubicacionService.getDepartamentos).mockReturnValue(
    throwError(() => new Error('departments')),
  );
  const component = TestBed.createComponent(UbicacionSelectorComponent).componentInstance;
  component.loadDepartamentos();
  expect(component.error).toBe('Error al cargar departamentos');

  vi.mocked(ubicacionService.getMunicipios).mockReturnValue(
    throwError(() => new Error('municipalities')),
  );
  component.loadMunicipios(1);
  expect(component.error).toBe('Error al cargar municipios');

  vi.mocked(ubicacionService.getBarrios).mockReturnValue(throwError(() => new Error('barrios')));
  component.loadBarrios(2);
  expect(component.error).toBe('Error al cargar barrios');
  expect(component.loading).toBe(false);
  expect(error).toHaveBeenCalledTimes(3);
});
