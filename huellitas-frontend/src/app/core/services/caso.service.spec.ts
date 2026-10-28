import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CasoService } from './caso.service';

let service: CasoService;
let http: HttpTestingController;
const baseUrl = `${environment.apiUrl}/casos`;

beforeEach(() => {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  service = TestBed.inject(CasoService);
  http = TestBed.inject(HttpTestingController);
});

afterEach(() => http.verify());

it('requests a paginated feed with defaults', () => {
  service.getFeed().subscribe();

  const request = http.expectOne(`${baseUrl}?page=0&size=10`);
  expect(request.request.method).toBe('GET');
  request.flush([]);
});

it('requests the case list with filters', () => {
  service.listarCasos({ municipioId: 4 }).subscribe();

  const request = http.expectOne(`${baseUrl}?municipioId=4`);
  expect(request.request.method).toBe('GET');
  request.flush([]);
});

it('requests the current user reports', () => {
  service.getMisReportes(2).subscribe();

  const request = http.expectOne(`${baseUrl}/mis-reportes?page=2`);
  expect(request.request.method).toBe('GET');
  request.flush([]);
});

it('requests a case detail', () => {
  service.getDetalle(12).subscribe();

  const request = http.expectOne(`${baseUrl}/12`);
  expect(request.request.method).toBe('GET');
  request.flush({ id: 12 });
});

it('creates a report', () => {
  const dto = { descripcion: 'Reporte' };
  service.crearReporte(dto).subscribe();

  const request = http.expectOne(baseUrl);
  expect(request.request.method).toBe('POST');
  expect(request.request.body).toEqual(dto);
  request.flush({ id: 1 });
});

it('uploads an image and includes an optional case id', () => {
  const file = new File(['image'], 'animal.png', { type: 'image/png' });
  service.subirImagen(file, 8).subscribe();

  const request = http.expectOne(`${environment.apiUrl}/imagenes?casoId=8`);
  expect(request.request.method).toBe('POST');
  expect((request.request.body as FormData).get('archivo')).toBe(file);
  request.flush({ url: 'https://example.test/animal.png' });
});

it('uploads an image without a case id', () => {
  const file = new File(['image'], 'animal.png', { type: 'image/png' });
  service.subirImagen(file).subscribe();

  const request = http.expectOne(`${environment.apiUrl}/imagenes`);
  expect((request.request.body as FormData).get('archivo')).toBe(file);
  request.flush({});
});

it('updates case state with PATCH', () => {
  const dto = { estado: 'RESUELTO' };
  service.actualizarEstado(3, dto).subscribe();

  const request = http.expectOne(`${baseUrl}/3/estado`);
  expect(request.request.method).toBe('PATCH');
  expect(request.request.body).toEqual(dto);
  request.flush({});
});

it('loads a case medical log', () => {
  service.getBitacora(3).subscribe();

  const request = http.expectOne(`${baseUrl}/3/bitacora`);
  expect(request.request.method).toBe('GET');
  request.flush({});
});

it('opens a medical log with an empty body by default', () => {
  service.abrirBitacora(3).subscribe();

  const request = http.expectOne(`${baseUrl}/3/bitacora`);
  expect(request.request.method).toBe('POST');
  expect(request.request.body).toEqual({});
  request.flush({});
});

it('adds a medical evolution', () => {
  const dto = { descripcion: 'Mejoría' };
  service.agregarEvolucion(3, dto).subscribe();

  const request = http.expectOne(`${baseUrl}/3/bitacora/evoluciones`);
  expect(request.request.method).toBe('POST');
  expect(request.request.body).toEqual(dto);
  request.flush({});
});

it('registers a medical discharge', () => {
  const dto = { observacion: 'Alta' };
  service.registrarAlta(3, dto).subscribe();

  const request = http.expectOne(`${baseUrl}/3/bitacora/alta`);
  expect(request.request.method).toBe('PATCH');
  expect(request.request.body).toEqual(dto);
  request.flush({});
});
