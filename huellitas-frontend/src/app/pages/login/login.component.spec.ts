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

it('switches to recovery and prevents the link default action', () => {
  const fixture = TestBed.createComponent(LoginComponent);
  const event = new Event('click');
  vi.spyOn(event, 'preventDefault');

  fixture.componentInstance.toggleRecovery(event);

  expect(event.preventDefault).toHaveBeenCalledOnce();
  expect(fixture.componentInstance.showRecovery()).toBe(true);
});

it('marks an invalid recovery form as touched', () => {
  const fixture = TestBed.createComponent(LoginComponent);
  fixture.componentInstance.onRecovery();

  expect(fixture.componentInstance.recoveryForm.controls.recoveryEmail.touched).toBe(true);
});

it('shows the recovery confirmation for a valid email', () => {
  const fixture = TestBed.createComponent(LoginComponent);
  vi.spyOn(window, 'alert').mockImplementation(() => undefined);
  fixture.componentInstance.recoveryForm.controls.recoveryEmail.setValue('user@example.test');

  fixture.componentInstance.onRecovery();

  expect(window.alert).toHaveBeenCalledWith(
    'Si el correo existe, recibirás un enlace de recuperación.',
  );
});
