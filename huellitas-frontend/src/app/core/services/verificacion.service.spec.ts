import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { VerificacionService } from './verificacion.service';

describe('VerificacionService', () => {
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
    request.flush({ verificaciones: [] });
  });

  it('submits a verification resolution', () => {
    const dto = { aprobado: true, motivo: 'Documentación completa' };
    service.resolverVerificacion(5, dto).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/verificaciones/5/resolver`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(dto);
    request.flush(null);
  });
});
