import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { UBICACION_VACIA } from '../../core/testing/ubicacion.mock';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { VerificacionService } from '../../core/services/verificacion.service';
import { CuentaComponent } from './cuenta.component';

const perfil: UsuarioPerfil = {
  id: 1,
  nombre: 'Juliana',
  correo: 'j@x.co',
  rol: 'CIUDADANO_VOLUNTARIO',
  verificado: true,
  barrioId: 3,
};

function configureCuentaTest(usuario: unknown, verificacion: unknown): void {
  TestBed.configureTestingModule({
    imports: [CuentaComponent],
    providers: [
      { provide: UsuarioService, useValue: usuario },
      { provide: AuthService, useValue: { logout: vi.fn() } },
      { provide: VerificacionService, useValue: verificacion },
      UBICACION_VACIA,
    ],
  });
}

it('loads the profile and saves the changes', () => {
  const usuario = {
    getMiPerfil: vi.fn().mockReturnValue(of(perfil)),
    actualizarPerfil: vi.fn().mockReturnValue(of({ ...perfil, nombre: 'Juli' })),
  };
  TestBed.configureTestingModule({
    imports: [CuentaComponent],
    providers: [
      { provide: UsuarioService, useValue: usuario },
      { provide: AuthService, useValue: { logout: vi.fn() } },
      {
        provide: VerificacionService,
        useValue: { solicitarRevision: vi.fn().mockReturnValue(of({ estado: 'PENDIENTE' })) },
      },
      UBICACION_VACIA,
    ],
  });
  const fixture = TestBed.createComponent(CuentaComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  expect(component.nombre).toBe('Juliana');
  expect(component.zona.barrioId).toBe(3);

  component.nombre = ' Juli ';
  component.guardar();

  expect(usuario.actualizarPerfil).toHaveBeenCalledWith({
    nombre: 'Juli',
    telefono: undefined,
    barrioId: 3,
  });
  expect(component.perfil()?.nombre).toBe('Juli');
  expect(component.guardado.exito()).toContain('guardados');
});

it('allows an institutional account to request a new review with support', () => {
  const usuario = {
    getMiPerfil: vi.fn().mockReturnValue(
      of({
        ...perfil,
        rol: 'FUNDACION_REFUGIO',
        organizacionId: 8,
        estadoVerificacionOrganizacion: 'RECHAZADA',
      }),
    ),
    actualizarPerfil: vi.fn(),
  };
  const verificacion = {
    solicitarRevision: vi
      .fn()
      .mockReturnValue(of({ estado: 'PENDIENTE', verificado: false, usuarioId: 1 })),
  };
  configureCuentaTest(usuario, verificacion);
  const fixture = TestBed.createComponent(CuentaComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  const documento = new File(['%PDF-1.7'], 'soporte.pdf', { type: 'application/pdf' });
  component.documentoRevision.set(documento);

  component.solicitarRevision();

  expect(verificacion.solicitarRevision).toHaveBeenCalledWith(documento, undefined);
  expect(component.perfil()?.estadoVerificacionOrganizacion).toBe('PENDIENTE');
  expect(component.mensajeSolicitud()).toContain('enviada');
});

it('collects organization data when an institutional account has no organization association', () => {
  const usuario = {
    getMiPerfil: vi
      .fn()
      .mockReturnValueOnce(
        of({
          ...perfil,
          rol: 'FUNDACION_REFUGIO',
          estadoVerificacionOrganizacion: null,
        }),
      )
      .mockReturnValueOnce(
        of({
          ...perfil,
          rol: 'FUNDACION_REFUGIO',
          organizacionId: 9,
          estadoVerificacionOrganizacion: 'PENDIENTE',
        }),
      ),
    actualizarPerfil: vi.fn(),
  };
  const verificacion = {
    solicitarRevision: vi
      .fn()
      .mockReturnValue(of({ estado: 'PENDIENTE', verificado: false, usuarioId: 1 })),
  };
  configureCuentaTest(usuario, verificacion);
  const fixture = TestBed.createComponent(CuentaComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  const documento = new File(['%PDF-1.7'], 'soporte.pdf', { type: 'application/pdf' });
  component.documentoRevision.set(documento);
  component.nombreEntidad = 'Fundación Huellitas';
  component.nitEntidad = '900123';
  component.municipioOperacion = 'Armenia';

  component.solicitarRevision();

  expect(verificacion.solicitarRevision).toHaveBeenCalledWith(documento, {
    nombreEntidad: 'Fundación Huellitas',
    nit: '900123',
    municipioOperacion: 'Armenia',
  });
  expect(component.errorSolicitud()).toBe('');
  expect(component.perfil()?.organizacionId).toBe(9);
  expect(component.requiereAsociacionOrganizacion()).toBe(false);
});
