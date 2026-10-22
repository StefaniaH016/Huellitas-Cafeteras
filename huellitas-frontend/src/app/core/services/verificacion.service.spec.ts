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
});
