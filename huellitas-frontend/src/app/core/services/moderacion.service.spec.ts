import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ModeracionService } from './moderacion.service';

describe('ModeracionService', () => {
  let service: ModeracionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ModeracionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads reported cases', () => {
    service.getCasosReportados().subscribe();
    const request = http.expectOne(`${environment.apiUrl}/moderacion/casos`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('submits a moderation decision with its reason', () => {
    const dto = { decision: 'OCULTAR' as const, motivo: 'No cumple las reglas' };
    service.resolverModeracion(7, dto).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/moderacion/casos/7`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(dto);
    request.flush({ casoId: 7, decision: 'OCULTADO', fechaRevision: '2026-03-11' });
  });
});
