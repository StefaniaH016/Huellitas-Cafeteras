import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CierreCasoDialogComponent } from './cierre-caso-dialog.component';

let fixture: ComponentFixture<CierreCasoDialogComponent>;

beforeEach(() => {
  TestBed.configureTestingModule({ imports: [CierreCasoDialogComponent] });
  fixture = TestBed.createComponent(CierreCasoDialogComponent);
});

it('shows the reunion confirmation option for a missing animal report', () => {
  fixture.componentInstance.puedeConfirmarReencuentro = true;
  fixture.componentInstance.esCasoExtraviado = true;
  fixture.componentInstance.casoId = 1042;
  fixture.componentInstance.motivoCierre = 'REENCUENTRO';
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('Cerrar caso #1042');
  expect(fixture.nativeElement.querySelector('input[value="REENCUENTRO"]')).not.toBeNull();
  expect(fixture.nativeElement.querySelector('input[value="REENCUENTRO"]').disabled).toBe(false);
});

it('shows closure context, styled outcome choices and stops at observations without evidence', () => {
  fixture.componentInstance.casoId = 1042;
  fixture.componentInstance.tieneAltaMedica = true;
  fixture.componentInstance.veterinariaNombre = 'Veterinaria San Jorge';
  fixture.componentInstance.fechaAlta = '19 sept 2026';
  fixture.componentInstance.tieneCustodia = true;
  fixture.componentInstance.fundacionNombre = 'Fundación Patitas';
  fixture.componentInstance.requiereResponsable = true;
  fixture.detectChanges();

  const text = fixture.nativeElement.textContent;
  expect(text).toMatch(/Alta médica emitida por Veterinaria San Jorge\s+el 19 sept 2026/);
  expect(text).toContain('Fundación Patitas tiene la custodia');
  expect(fixture.nativeElement.querySelectorAll('.result-option')).toHaveLength(3);
  expect(fixture.nativeElement.querySelector('#cierre-entregado-a')).not.toBeNull();
  expect(fixture.nativeElement.querySelector('#cierre-observacion')).not.toBeNull();
  expect(fixture.nativeElement.textContent.indexOf('Observaciones')).toBeGreaterThan(
    fixture.nativeElement.textContent.indexOf('Entregado a'),
  );
  expect(text).not.toContain('Evidencia');
});

it('disables the reunion outcome for a missing animal case closed by a non-reporter', () => {
  fixture.componentInstance.esCasoExtraviado = true;
  fixture.componentInstance.puedeConfirmarReencuentro = false;
  fixture.detectChanges();

  const reunion = fixture.nativeElement.querySelector(
    'input[value="REENCUENTRO"]',
  ) as HTMLInputElement;
  expect(reunion.disabled).toBe(true);
});

it('emits confirmation when the closure form is submitted', () => {
  const confirmar = vi.fn();
  fixture.componentInstance.confirmar.subscribe(confirmar);
  fixture.detectChanges();

  fixture.nativeElement
    .querySelector('form')
    .dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

  expect(confirmar).toHaveBeenCalledOnce();
});

it('allows cancelling with Escape unless the request is in progress', () => {
  const cancelar = vi.fn();
  fixture.componentInstance.cancelar.subscribe(cancelar);
  fixture.detectChanges();

  document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
  expect(cancelar).toHaveBeenCalledOnce();

  fixture.componentInstance.accionEnCurso = true;
  document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
  expect(cancelar).toHaveBeenCalledOnce();
});
