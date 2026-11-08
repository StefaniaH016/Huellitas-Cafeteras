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

it('shows edit and delete actions for registered cases', () => {
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();
  const actions = fixture.nativeElement.querySelector('.report-actions');

  expect(actions.textContent).toContain('Editar');
  expect(actions.textContent).toContain('Eliminar');
});

it('separates active cases from resolved and archived history', () => {
  vi.mocked(casoService.getMisReportes).mockReturnValue(
    of({
      content: [
        { id: 11, estado: 'EN_ATENCION', urgencia: 'MEDIA', descripcion: 'Caso activo' },
        { id: 12, estado: 'RESUELTO', urgencia: 'BAJA', descripcion: 'Caso resuelto' },
        { id: 13, estado: 'ARCHIVADO', urgencia: 'BAJA', descripcion: 'Caso archivado' },
      ],
    }),
  );
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Caso activo');
  expect(fixture.nativeElement.textContent).not.toContain('Caso resuelto');
  expect(fixture.nativeElement.textContent).not.toContain('Caso archivado');
  expect(fixture.nativeElement.querySelector('#tab-activos').getAttribute('aria-selected')).toBe(
    'true',
  );

  fixture.nativeElement.querySelector('#tab-historial').click();
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).not.toContain('Caso activo');
  expect(fixture.nativeElement.textContent).toContain('Caso resuelto');
  expect(fixture.nativeElement.textContent).toContain('Caso archivado');
  expect(fixture.nativeElement.textContent).toContain('Resuelto');
  expect(fixture.nativeElement.textContent).toContain('Archivado');
});

it('keeps closed cases read-only in history', () => {
  vi.mocked(casoService.getMisReportes).mockReturnValue(
    of({
      content: [
        { id: 14, estado: 'ARCHIVADO', urgencia: 'BAJA', descripcion: 'Caso de solo lectura' },
      ],
    }),
  );
  const fixture = TestBed.createComponent(MisReportesComponent);
  fixture.detectChanges();
  fixture.nativeElement.querySelector('#tab-historial').click();
  fixture.detectChanges();

  const actions = fixture.nativeElement.querySelector('.report-actions');
  expect(actions.textContent).toContain('Ver caso');
  expect(actions.textContent).not.toContain('Editar');
  expect(actions.textContent).not.toContain('Eliminar');
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
