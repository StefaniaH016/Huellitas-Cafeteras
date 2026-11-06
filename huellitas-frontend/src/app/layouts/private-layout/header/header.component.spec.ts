import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { TokenService } from '../../../core/services/token.service';
import { HeaderComponent } from './header.component';

let notificacionService: Pick<NotificacionService, 'sinLeer' | 'actualizarContador'>;
let authService: Pick<AuthService, 'getRol' | 'logout'>;

beforeEach(() => {
  notificacionService = {
    sinLeer: signal(3),
    actualizarContador: vi.fn(),
  };
  authService = {
    getRol: vi.fn().mockReturnValue('CIUDADANO_VOLUNTARIO'),
    logout: vi.fn(),
  };

  TestBed.configureTestingModule({
    imports: [HeaderComponent],
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: authService },
      {
        provide: TokenService,
        useValue: { decodePayload: (): { nombre: string } => ({ nombre: 'Ana' }) },
      },
      { provide: NotificacionService, useValue: notificacionService },
    ],
  });
});

it('provides Inicio for administrators and links the user chip to the account page', () => {
  vi.mocked(authService.getRol).mockReturnValue('ADMINISTRADOR');
  const fixture = TestBed.createComponent(HeaderComponent);
  fixture.detectChanges();

  const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
  expect(
    links.some((link) => link.textContent?.trim() === 'Inicio' && link.pathname === '/home'),
  ).toBe(true);
  expect(
    links.some((link) => link.textContent?.trim() === 'Panel' && link.pathname === '/admin'),
  ).toBe(true);
  expect(
    links.some((link) => link.textContent?.trim() === 'Casos' && link.pathname === '/casos'),
  ).toBe(true);
  expect(fixture.nativeElement.querySelector('a.user-chip')?.getAttribute('href')).toBe('/cuenta');
  expect(fixture.nativeElement.textContent).not.toContain('Mi cuenta');
});

it('links the veterinarian Mi panel navigation to its dedicated panel', () => {
  vi.mocked(authService.getRol).mockReturnValue('VETERINARIA');
  const fixture = TestBed.createComponent(HeaderComponent);
  fixture.detectChanges();

  const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
  const panel = links.find((link) => link.textContent?.trim() === 'Mi panel');

  expect(panel?.pathname).toBe('/veterinaria');
});

it.each(['CIUDADANO_VOLUNTARIO', 'FUNDACION_REFUGIO', 'VETERINARIA'])(
  'shows the Casos link for %s',
  (rol): void => {
    vi.mocked(authService.getRol).mockReturnValue(rol);
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();

    const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
    expect(
      links.some((link) => link.textContent?.trim() === 'Casos' && link.pathname === '/casos'),
    ).toBe(true);
  },
);

it('shows the live unread count instead of a fixed badge', () => {
  const fixture = TestBed.createComponent(HeaderComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).toContain('3');
  expect(notificacionService.actualizarContador).toHaveBeenCalledOnce();

  notificacionService.sinLeer.set(0);
  fixture.detectChanges();

  expect(fixture.nativeElement.querySelector('.badge')).toBeNull();
});

it('refreshes the unread count when the window regains focus', () => {
  const fixture = TestBed.createComponent(HeaderComponent);
  fixture.detectChanges();

  window.dispatchEvent(new Event('focus'));

  expect(notificacionService.actualizarContador).toHaveBeenCalledTimes(2);
});
