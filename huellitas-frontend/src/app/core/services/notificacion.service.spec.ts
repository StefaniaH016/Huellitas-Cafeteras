import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { NotificacionService } from './notificacion.service';

let service: NotificacionService;
let http: HttpTestingController;
const baseUrl = `${environment.apiUrl}/notificaciones`;

beforeEach(() => {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  service = TestBed.inject(NotificacionService);
  http = TestBed.inject(HttpTestingController);
});

afterEach(() => http.verify());

it('lists all notifications or only the unread ones', () => {
  service.listar().subscribe();
  service.listar(true, 1, 5).subscribe();

  http.expectOne(`${baseUrl}?page=0&size=30`).flush({ content: [] });
  http.expectOne(`${baseUrl}?leidas=false&page=1&size=5`).flush({ content: [] });
});

it('updates the unread counter from the dedicated count endpoint', () => {
  service.actualizarContador();
  http.expectOne(`${baseUrl}/no-leidas/count`).flush(4);

  expect(service.sinLeer()).toBe(4);
});

it('preserves the last counter and reports an error when the count request fails', () => {
  service.sinLeer.set(2);
  const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  service.actualizarContador();
  http.expectOne(`${baseUrl}/no-leidas/count`).flush(null, { status: 500, statusText: 'x' });

  expect(service.sinLeer()).toBe(2);
  expect(errorSpy).toHaveBeenCalledOnce();
});

it('marks one as read and lowers the counter', () => {
  service.sinLeer.set(1);
  service.marcarLeida(9).subscribe();
  http.expectOne(`${baseUrl}/9/leer`).flush({});

  expect(service.sinLeer()).toBe(0);
});
