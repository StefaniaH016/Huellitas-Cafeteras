import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { NEVER, of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { Notificacion, NotificacionService } from '../../core/services/notificacion.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { NotificacionesComponent } from './notificaciones.component';

const nueva: Notificacion = {
  id: 1,
  tipo: 'EMERGENCIA',
  mensaje: '🚨 EMERGENCIA en tu zona: Caso #5 - ALTA. ¿Puedes ayudar?',
  leida: false,
  fecha: '',
  casoId: 5,
};
const notificacionesDePrueba: Notificacion[] = [
  nueva,
  {
    id: 2,
    tipo: 'CIERRE',
    mensaje: 'Tu caso #2 ha sido cerrado.',
    leida: true,
    fecha: '',
    casoId: 2,
  },
  {
    id: 3,
    tipo: 'RECORDATORIO',
    mensaje: 'Tu caso #3 lleva varios días sin actividad.',
    leida: false,
    fecha: '',
    casoId: 3,
  },
  {
    id: 4,
    tipo: 'VERIFICACION',
    mensaje: 'Tu organización fue verificada.',
    leida: true,
    fecha: '',
    casoId: null,
  },
];
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

function crearFixture(admin = false): ComponentFixture<NotificacionesComponent> {
  TestBed.configureTestingModule({
    imports: [NotificacionesComponent],
    providers: [
      provideRouter([]),
      { provide: NotificacionService, useValue: servicio },
      { provide: UsuarioService, useValue: usuarioService },
      {
        provide: AuthService,
        useValue: {
          tieneRol: vi.fn().mockImplementation((rol: string) => admin && rol === 'ADMIN'),
        },
      },
    ],
  });
  const fixture = TestBed.createComponent(NotificacionesComponent);
  fixture.detectChanges();
  return fixture;
}

function crear(admin = false): NotificacionesComponent {
  return crearFixture(admin).componentInstance;
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

  expect(servicio['listar']).toHaveBeenCalledOnce();
  expect(servicio['listar']).toHaveBeenLastCalledWith(false);
  expect(component.notificacionesFiltradas()).toEqual([nueva]);
  expect(component.icono('EMERGENCIA')).toBe('🚨');
  expect(component.icono('CUSTODIA')).toBe('🏠');
  expect(component.icono('EVOLUCION_MEDICA')).toBe('🩺');
  expect(component.icono('ALTA_MEDICA')).toBe('🎉');
  expect(component.icono('VERIFICACION')).toBe('✅');
  expect(component.icono('OTRA')).toBe('🔔');
});

it('shows notification skeleton rows while the initial request is pending', () => {
  servicio['listar'] = vi.fn().mockReturnValue(NEVER);
  const fixture = crearFixture();

  expect(fixture.nativeElement.querySelectorAll('.notif-skeleton')).toHaveLength(4);
  expect(fixture.nativeElement.querySelector('output')?.textContent).toContain(
    'Cargando notificaciones',
  );
  expect(
    fixture.nativeElement.querySelector('.notification-skeletons').getAttribute('aria-hidden'),
  ).toBe('true');
});

it('filters case, area and reminder notifications from the loaded list', () => {
  const component = crear();
  component.notificaciones.set(notificacionesDePrueba);

  component.filtrar('mi-zona');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([1]);

  component.filtrar('mis-casos');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([2, 3]);
  expect(component.contadores().misCasos).toBe(2);

  component.filtrar('recordatorios');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([3]);

  component.filtrar('sin-leer');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([1, 3]);
});

it('filters administrative notices by account, moderation and inactivity', () => {
  const component = crear(true);
  component.notificaciones.set([
    notificacionesDePrueba[3],
    {
      id: 5,
      tipo: 'CAMBIO_ESTADO',
      mensaje: 'Un contenido quedó marcado para moderación.',
      leida: false,
      fecha: '',
      casoId: null,
    },
    {
      id: 6,
      tipo: 'CAMBIO_ESTADO',
      mensaje: 'W-04 encontró un caso sin actividad.',
      leida: false,
      fecha: '',
      casoId: null,
    },
  ]);

  component.filtrar('cuentas');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([4]);

  component.filtrar('moderacion');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([5]);

  component.filtrar('sin-actividad');
  expect(component.notificacionesFiltradas().map((item) => item.id)).toEqual([6]);
});

it('enables filter chips and displays only notifications from the selected category', () => {
  const fixture = crearFixture();
  fixture.componentInstance.notificaciones.set(notificacionesDePrueba);
  fixture.detectChanges();

  const chips = fixture.nativeElement.querySelectorAll('.chip') as NodeListOf<HTMLButtonElement>;
  const zonaChip = Array.from(chips).find((chip) => chip.textContent?.includes('Mi zona'));
  expect(zonaChip?.disabled).toBe(false);
  zonaChip?.click();
  fixture.detectChanges();

  const filas = fixture.nativeElement.querySelectorAll('.notif');
  expect(filas).toHaveLength(1);
  expect(filas[0].textContent).toContain('EMERGENCIA en tu zona');
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
