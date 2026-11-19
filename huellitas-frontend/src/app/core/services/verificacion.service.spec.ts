import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { VerificacionService } from './verificacion.service';

let service: VerificacionService;
let http: HttpTestingController;

beforeEach(() => {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  service = TestBed.inject(VerificacionService);
  http = TestBed.inject(HttpTestingController);
});

afterEach(() => http.verify());

it('loads pending verifications', () => {
  service.getPendientes().subscribe();
  const request = http.expectOne(`${environment.apiUrl}/verificaciones/pendientes`);
  expect(request.request.method).toBe('GET');
  request.flush([]);
});

it('submits a verification resolution', () => {
  const dto = { decision: 'APROBAR' as const, observacion: 'Documentación completa' };
  service.resolverVerificacion(5, dto).subscribe();
  const request = http.expectOne(`${environment.apiUrl}/verificaciones/5`);
  expect(request.request.method).toBe('PATCH');
  expect(request.request.body).toEqual(dto);
  request.flush(null);
});

it('submits a support document to request a new review', () => {
  const documento = new File(['%PDF-1.7'], 'soporte.pdf', { type: 'application/pdf' });
  service.solicitarRevision(documento).subscribe();
  const request = http.expectOne(`${environment.apiUrl}/verificaciones/solicitar-revision`);
  expect(request.request.method).toBe('POST');
  expect((request.request.body as FormData).get('documentoSoporte')).toMatchObject({
    name: 'soporte.pdf',
    type: 'application/pdf',
  });
  request.flush({ usuarioId: 5, estado: 'PENDIENTE', verificado: false });
});

it('includes organization details when repairing a legacy institutional account', () => {
  const documento = new File(['%PDF-1.7'], 'soporte.pdf', { type: 'application/pdf' });
  service
    .solicitarRevision(documento, {
      nombreEntidad: 'Fundación Huellitas',
      nit: '900123',
      municipioOperacion: 'Armenia',
    })
    .subscribe();
  const request = http.expectOne(`${environment.apiUrl}/verificaciones/solicitar-revision`);
  const formData = request.request.body as FormData;
  expect(formData.get('nombreEntidad')).toBe('Fundación Huellitas');
  expect(formData.get('nit')).toBe('900123');
  expect(formData.get('municipioOperacion')).toBe('Armenia');
  request.flush({ usuarioId: 5, estado: 'PENDIENTE', verificado: false });
});

it('loads support documents as blobs', () => {
  service.obtenerDocumentoSoporte(5).subscribe();
  const request = http.expectOne(`${environment.apiUrl}/verificaciones/5/documento-soporte`);
  expect(request.request.method).toBe('GET');
  expect(request.request.responseType).toBe('blob');
  request.flush(new Blob(['%PDF-1.7'], { type: 'application/pdf' }));
});
