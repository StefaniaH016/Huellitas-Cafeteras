import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { BitacoraService } from './bitacora.service';

describe('BitacoraService', () => {
  let service: BitacoraService;
  let http: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/casos/6/bitacora`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BitacoraService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('gets the medical log', () => {
    service.getBitacora(6).subscribe();
    const request = http.expectOne(baseUrl);
    expect(request.request.method).toBe('GET');
    request.flush({});
  });

  it('opens a medical log with an empty default body', () => {
    service.abrirBitacora(6).subscribe();
    const request = http.expectOne(baseUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush({});
  });

  it('adds evolutions and registers discharges', () => {
    const evolution = { descripcion: 'Mejora' };
    service.agregarEvolucion(6, evolution).subscribe();
    const evolutionRequest = http.expectOne(`${baseUrl}/evoluciones`);
    expect(evolutionRequest.request.method).toBe('POST');
    expect(evolutionRequest.request.body).toEqual(evolution);
    evolutionRequest.flush({});

    const discharge = { observacion: 'Alta' };
    service.registrarAlta(6, discharge).subscribe();
    const dischargeRequest = http.expectOne(`${baseUrl}/alta`);
    expect(dischargeRequest.request.method).toBe('PATCH');
    expect(dischargeRequest.request.body).toEqual(discharge);
    dischargeRequest.flush({});
  });

  it('builds the PDF URL', () => {
    expect(service.getPdfUrl(6)).toBe(`${baseUrl}/pdf`);
  });
});
