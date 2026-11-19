import { TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { of, Subject, throwError } from 'rxjs';
import { Municipio, UbicacionService } from '../../../core/services/ubicacion.service';
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
  expect(component.errorDepartamentos).toBe('Error al cargar departamentos');

  vi.mocked(ubicacionService.getMunicipios).mockReturnValue(
    throwError(() => new Error('municipalities')),
  );
  component.loadMunicipios(1);
  expect(component.errorMunicipios).toBe('Error al cargar municipios');

  vi.mocked(ubicacionService.getBarrios).mockReturnValue(throwError(() => new Error('barrios')));
  component.loadBarrios(2);
  expect(component.errorBarrios).toBe('Error al cargar barrios');
  expect(component.loading).toBe(false);
  expect(error).toHaveBeenCalledTimes(3);
});

it('shows loading and allows retrying after a department request fails', () => {
  vi.mocked(ubicacionService.getDepartamentos)
    .mockReturnValueOnce(throwError(() => new Error('network')))
    .mockReturnValueOnce(of([{ id: 1, nombre: 'Quindío' }]));
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  const fixture = TestBed.createComponent(UbicacionSelectorComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('No se pudieron cargar los departamentos');
  fixture.nativeElement.querySelector('button').click();
  fixture.detectChanges();

  expect(fixture.componentInstance.departamentos).toEqual([{ id: 1, nombre: 'Quindío' }]);
  expect(fixture.nativeElement.textContent).toContain('Selecciona el departamento');
});

it('unlocks the municipality selector after loading without editing the account name', async () => {
  const departamentosResponse = new Subject<{ id: number; nombre: string }[]>();
  const municipiosResponse = new Subject<Municipio[]>();
  vi.mocked(ubicacionService.getDepartamentos).mockReturnValue(departamentosResponse);
  vi.mocked(ubicacionService.getMunicipios).mockReturnValue(municipiosResponse);
  const fixture = TestBed.createComponent(UbicacionSelectorComponent);
  fixture.detectChanges();

  departamentosResponse.next([{ id: 1, nombre: 'Quindío' }]);
  departamentosResponse.complete();
  await fixture.whenStable();

  const component = fixture.componentInstance;
  const departamentos = fixture.nativeElement.querySelector('#zona-depto-sel') as HTMLSelectElement;
  const municipios = fixture.nativeElement.querySelector('#zona-mun-sel') as HTMLSelectElement;
  expect(departamentos.textContent).toContain('Quindío');

  component.onDeptoChange('1');
  fixture.detectChanges();
  expect(municipios.disabled).toBe(true);
  expect(municipios.textContent).toContain('Cargando municipios...');

  municipiosResponse.next([{ id: 2, nombre: 'Armenia', departamentoId: 1 }]);
  municipiosResponse.complete();
  await fixture.whenStable();

  expect(municipios.disabled).toBe(false);
  expect(municipios.textContent).toContain('Armenia');
});

it('ignores municipality responses from an earlier department selection', () => {
  const olderResponse = new Subject<{ id: number; nombre: string; departamentoId: number }[]>();
  const newerResponse = new Subject<{ id: number; nombre: string; departamentoId: number }[]>();
  vi.mocked(ubicacionService.getMunicipios)
    .mockReturnValueOnce(olderResponse)
    .mockReturnValueOnce(newerResponse);
  const component = TestBed.createComponent(UbicacionSelectorComponent).componentInstance;

  component.loadMunicipios(1);
  component.loadMunicipios(2);
  newerResponse.next([{ id: 20, nombre: 'Armenia', departamentoId: 2 }]);
  olderResponse.next([{ id: 10, nombre: 'Municipio viejo', departamentoId: 1 }]);

  expect(component.municipios).toEqual([{ id: 20, nombre: 'Armenia', departamentoId: 2 }]);
});
