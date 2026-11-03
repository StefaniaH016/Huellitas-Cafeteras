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

it('updates the unread counter from the unread page total', () => {
  service.actualizarContador();
  http.expectOne(`${baseUrl}?leidas=false&page=0&size=1`).flush({ content: [], totalElements: 4 });

  expect(service.sinLeer()).toBe(4);
});

it('resets the counter when the request fails', () => {
  service.sinLeer.set(2);
  service.actualizarContador();
  http
    .expectOne(`${baseUrl}?leidas=false&page=0&size=1`)
    .flush(null, { status: 500, statusText: 'x' });

  expect(service.sinLeer()).toBe(0);
});

it('marks one as read and lowers the counter', () => {
  service.sinLeer.set(1);
  service.marcarLeida(9).subscribe();
  http.expectOne(`${baseUrl}/9/leer`).flush({});

  expect(service.sinLeer()).toBe(0);
});
