import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { UBICACION_VACIA } from '../../core/testing/ubicacion.mock';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { CuentaComponent } from './cuenta.component';

const perfil: UsuarioPerfil = {
  id: 1,
  nombre: 'Juliana',
  correo: 'j@x.co',
  rol: 'CIUDADANO_VOLUNTARIO',
  verificado: true,
  barrioId: 3,
};

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
