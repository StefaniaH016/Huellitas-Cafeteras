import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { Notificacion, NotificacionService } from '../../core/services/notificacion.service';
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

function crear(): NotificacionesComponent {
  TestBed.configureTestingModule({
    imports: [NotificacionesComponent],
    providers: [provideRouter([]), { provide: NotificacionService, useValue: servicio }],
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
});

it('loads all notifications and can filter the unread ones', () => {
  const component = crear();
  expect(servicio['listar']).toHaveBeenCalledWith(false);

  component.filtrar(true);

  expect(servicio['listar']).toHaveBeenLastCalledWith(true);
  expect(component.icono('EMERGENCIA')).toBe('🚨');
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
