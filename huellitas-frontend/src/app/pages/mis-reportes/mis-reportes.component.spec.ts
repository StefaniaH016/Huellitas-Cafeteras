import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CasoService } from '../../core/services/caso.service';
import { MisReportesComponent } from './mis-reportes.component';

let casoService: Pick<CasoService, 'eliminarCaso' | 'getMisReportes'>;

beforeEach(() => {
  casoService = {
    eliminarCaso: vi.fn().mockReturnValue(of(undefined)),
    getMisReportes: vi.fn().mockReturnValue(
      of({
        content: [
          {
            id: 10,
            estado: 'REGISTRADO',
            urgencia: 'ALTA',
            descripcion: 'Caso de prueba',
          },
        ],
      }),
    ),
  };
  TestBed.configureTestingModule({
    imports: [MisReportesComponent],
    providers: [provideRouter([]), { provide: CasoService, useValue: casoService }],
  });
});

it('loads and displays the current user reports', () => {
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();

  expect(casoService.getMisReportes).toHaveBeenCalledOnce();
  expect(fixture.nativeElement.textContent).toContain('Caso de prueba');
});

it('deletes a registered case after user confirmation', () => {
  vi.spyOn(globalThis, 'confirm').mockReturnValue(true);
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  const caso = component.reportes()[0];

  component.eliminar(caso);

  expect(casoService.eliminarCaso).toHaveBeenCalledWith(10);
  expect(component.reportes()).toEqual([]);
});

it('does not delete when the user cancels confirmation', () => {
  vi.spyOn(globalThis, 'confirm').mockReturnValue(false);
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();

  fixture.componentInstance.eliminar(fixture.componentInstance.reportes()[0]);

  expect(casoService.eliminarCaso).not.toHaveBeenCalled();
});

it('shows an error when report deletion fails', () => {
  vi.spyOn(globalThis, 'confirm').mockReturnValue(true);
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.eliminarCaso).mockReturnValue(throwError(() => new Error('delete')));
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();

  fixture.componentInstance.eliminar(fixture.componentInstance.reportes()[0]);

  expect(fixture.componentInstance.error()).toContain('No fue posible eliminar');
});

it('explains when the backend has not deployed case deletion support', () => {
  vi.spyOn(globalThis, 'confirm').mockReturnValue(true);
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.eliminarCaso).mockReturnValue(
    throwError(() => new HttpErrorResponse({ status: 405 })),
  );
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();

  fixture.componentInstance.eliminar(fixture.componentInstance.reportes()[0]);

  expect(fixture.componentInstance.error()).toContain('eliminación de casos');
  expect(fixture.componentInstance.eliminandoId()).toBeNull();
});
