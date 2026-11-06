import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { LoginComponent } from './login.component';

const authService = {
  login: vi.fn(),
  navigateAfterLogin: vi.fn(),
};

beforeEach(() => {
  vi.resetAllMocks();
  vi.mocked(authService.login).mockReturnValue(of('CIUDADANO_VOLUNTARIO'));

  TestBed.configureTestingModule({
    imports: [LoginComponent],
    providers: [{ provide: AuthService, useValue: authService }, provideRouter([])],
  });
});

it('marks invalid credentials as touched without calling the service', () => {
  const fixture = TestBed.createComponent(LoginComponent);
  fixture.componentInstance.onSubmit();

  expect(fixture.componentInstance.loginForm.controls.email.touched).toBe(true);
  expect(authService.login).not.toHaveBeenCalled();
});

it('logs in and navigates after a successful response', () => {
  const fixture = TestBed.createComponent(LoginComponent);
  fixture.componentInstance.loginForm.setValue({
    email: 'user@example.test',
    password: 'password',
    mantenerSesion: false,
  });

  fixture.componentInstance.onSubmit();

  expect(authService.login).toHaveBeenCalledWith('user@example.test', 'password');
  expect(authService.navigateAfterLogin).toHaveBeenCalledOnce();
  expect(fixture.componentInstance.isLoading()).toBe(false);
});

it('shows an error when login fails', () => {
  vi.mocked(authService.login).mockReturnValue(throwError(() => new Error('Request failed')));
  const fixture = TestBed.createComponent(LoginComponent);
  fixture.componentInstance.loginForm.setValue({
    email: 'user@example.test',
    password: 'password',
    mantenerSesion: false,
  });

  fixture.componentInstance.onSubmit();

  expect(fixture.componentInstance.showError()).toBe(true);
  expect(fixture.componentInstance.isLoading()).toBe(false);
});

it('does not show the unavailable password-recovery link or form', () => {
  const fixture = TestBed.createComponent(LoginComponent);
  fixture.detectChanges();

  expect(fixture.nativeElement.textContent).not.toContain('¿Olvidaste tu contraseña?');
  expect(fixture.nativeElement.textContent).not.toContain('Recuperar acceso');
  expect(fixture.nativeElement.querySelector('input[type="email"]')).not.toBeNull();
  expect(fixture.nativeElement.querySelector('input[type="password"]')).not.toBeNull();
});
