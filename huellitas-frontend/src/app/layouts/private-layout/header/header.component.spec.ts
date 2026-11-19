import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { TokenService } from '../../../core/services/token.service';
import { HeaderComponent } from './header.component';

let notificacionService: Pick<NotificacionService, 'sinLeer' | 'actualizarContador'>;
let authService: Pick<AuthService, 'getRol' | 'logout'>;
let nombreUsuario: string;
let router: Router;

beforeEach(() => {
  nombreUsuario = 'Ana';
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
      provideRouter([
        { path: 'home', children: [] },
        { path: 'mis-reportes', children: [] },
      ]),
      { provide: AuthService, useValue: authService },
      {
        provide: TokenService,
        useValue: { decodePayload: (): { nombre: string } => ({ nombre: nombreUsuario }) },
      },
      { provide: NotificacionService, useValue: notificacionService },
    ],
  });
  router = TestBed.inject(Router);
});

it('shows only the first name and keeps initials from the full name', () => {
  nombreUsuario = 'Jose Ramirez Montoya';
  const fixture = TestBed.createComponent(HeaderComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.querySelector('.user-name')?.textContent?.trim()).toBe('Jose');
  expect(fixture.nativeElement.querySelector('.avatar')?.textContent?.trim()).toBe('JR');
});

it('keeps the selected navigation item active after changing screens', async () => {
  const fixture = TestBed.createComponent(HeaderComponent);
  fixture.detectChanges();

  await router.navigateByUrl('/mis-reportes');
  fixture.detectChanges();

  const reportLinks = Array.from(
    fixture.nativeElement.querySelectorAll('a[href="/mis-reportes"]'),
  ) as HTMLAnchorElement[];
  expect(reportLinks.length).toBeGreaterThan(0);
  expect(reportLinks.every((link) => link.classList.contains('active'))).toBe(true);

  await router.navigateByUrl('/home');
  fixture.detectChanges();

  const homeLinks = Array.from(
    fixture.nativeElement.querySelectorAll('nav.header-nav a[href="/home"]'),
  ) as HTMLAnchorElement[];
  expect(homeLinks.every((link) => link.classList.contains('active'))).toBe(true);
  expect(reportLinks.every((link) => !link.classList.contains('active'))).toBe(true);
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
