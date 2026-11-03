import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { UBICACION_VACIA } from '../../core/testing/ubicacion.mock';
import { RegistroComponent } from './registro.component';

let registro: ReturnType<typeof vi.fn>;

function crear(): RegistroComponent {
  TestBed.configureTestingModule({
    imports: [RegistroComponent],
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: { registro } },
      UBICACION_VACIA,
    ],
  });
  const fixture = TestBed.createComponent(RegistroComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

function llenar(component: RegistroComponent): void {
  component.step2Form.setValue({
    nombre: ' Patitas ',
    email: 'patitas@x.co',
    telefono: '3001234567',
    municipio: 'Armenia',
    password: 'secreta123',
    confirmPassword: 'secreta123',
    nombreEntidad: 'Fundación Patitas',
    nit: '900123',
    tarjetaProf: '',
    municipioOperacion: 'Armenia',
    docSoporte: '',
    aceptaDatos: true,
  });
}

beforeEach(() => {
  registro = vi.fn().mockReturnValue(of({}));
});

it('requires the entity data only for institutional accounts', () => {
  const component = crear();
  component.goToStep2();
  expect(component.step()).toBe(1);

  component.selectTipo('FUNDACION_REFUGIO');
  component.goToStep2();
  component.step2Form.controls.nit.markAsTouched();

  expect(component.step()).toBe(2);
  expect(component.step2Form.get('nit')?.invalid).toBe(true);
});

it('sends the signup with the consent and goes to login', () => {
  const component = crear();
  const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  component.selectTipo('FUNDACION_REFUGIO');
  component.goToStep2();
  llenar(component);

  component.onSubmit();

  expect(registro).toHaveBeenCalledWith(
    expect.objectContaining({
      nombre: 'Patitas',
      correo: 'patitas@x.co',
      rol: 'FUNDACION_REFUGIO',
      aceptoTratamientoDatos: true,
      nit: '900123',
    }),
  );
  expect(navigate).toHaveBeenCalledWith(['/login']);
});

it('does not send an invalid form and shows backend errors', () => {
  const component = crear();
  component.selectTipo('CIUDADANO_VOLUNTARIO');
  component.goToStep2();
  component.onSubmit();
  expect(registro).not.toHaveBeenCalled();

  llenar(component);
  registro.mockReturnValue(
    throwError(() => new HttpErrorResponse({ error: { message: 'Correo ya registrado' } })),
  );
  component.onSubmit();
  expect(component.errorMessage()).toBe('Correo ya registrado');
});
