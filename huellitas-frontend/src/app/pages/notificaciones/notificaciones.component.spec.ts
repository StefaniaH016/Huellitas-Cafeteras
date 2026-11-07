import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { Notificacion, NotificacionService } from '../../core/services/notificacion.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { NotificacionesComponent } from './notificaciones.component';

const nueva: Notificacion = {
  id: 1,
  tipo: 'EMERGENCIA',
  mensaje: 'x',
  leida: false,
  fecha: '',
  casoId: 5,
};
let servicio: Record<string, unknown>;
let usuarioService: Record<string, unknown>;
const perfil: UsuarioPerfil = {
  id: 2,
  nombre: 'Ana',
  correo: 'ana@example.test',
  rol: 'CIUDADANO_VOLUNTARIO',
  verificado: true,
  disponibleRescate: false,
};

function crear(): NotificacionesComponent {
  TestBed.configureTestingModule({
    imports: [NotificacionesComponent],
    providers: [
      provideRouter([]),
      { provide: NotificacionService, useValue: servicio },
      { provide: UsuarioService, useValue: usuarioService },
      { provide: AuthService, useValue: { tieneRol: vi.fn().mockReturnValue(false) } },
    ],
  });
  const fixture = TestBed.createComponent(NotificacionesComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

beforeEach(() => {
  servicio = {
    sinLeer: signal(1),
    listar: vi.fn().mockReturnValue(of({ content: [nueva] })),
    actualizarContador: vi.fn(),
    marcarLeida: vi.fn().mockReturnValue(of(undefined)),
  };
  usuarioService = {
    getMiPerfil: vi.fn().mockReturnValue(of(perfil)),
    actualizarPerfil: vi
      .fn()
      .mockImplementation((dto: { disponibleRescate: boolean }) =>
        of({ ...perfil, disponibleRescate: dto.disponibleRescate }),
      ),
  };
});

it('loads all notifications and can filter the unread ones', () => {
  const component = crear();
  expect(servicio['listar']).toHaveBeenCalledWith(false);

  component.filtrar(true);

  expect(servicio['listar']).toHaveBeenLastCalledWith(true);
  expect(component.icono('EMERGENCIA')).toBe('🚨');
  expect(component.icono('CUSTODIA')).toBe('🏠');
  expect(component.icono('VERIFICACION')).toBe('✅');
  expect(component.icono('OTRA')).toBe('🔔');
});

it('marks a notification as read and opens its case', () => {
  const component = crear();
  const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

  component.abrir(nueva);

  expect(servicio['marcarLeida']).toHaveBeenCalledWith(1);
  expect(component.notificaciones()[0].leida).toBe(true);
  expect(navigate).toHaveBeenCalledWith(['/caso-detalle', 5]);
});

it('loads and persists the rescue availability preference', () => {
  const component = crear();
  const input = document.createElement('input');
  input.type = 'checkbox';
  input.checked = true;
  const event = new Event('change');
  Object.defineProperty(event, 'target', { value: input });

  component.cambiarDisponibilidadRescate(event);

  expect(usuarioService['actualizarPerfil']).toHaveBeenCalledWith({
    nombre: 'Ana',
    telefono: undefined,
    barrioId: null,
    disponibleRescate: true,
  });
  expect(component.disponibleRescate()).toBe(true);
  expect(component.mensajePreferencia()).toBe('Preferencia actualizada.');
});
