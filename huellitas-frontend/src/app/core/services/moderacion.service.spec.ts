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
    const request = http.expectOne(`${environment.apiUrl}/moderacion/casos-reportados`);
    expect(request.request.method).toBe('GET');
    request.flush({ casos: [] });
  });

  it('submits a moderation resolution', () => {
    const dto = { aprobado: false, razon: 'No cumple las reglas' };
    service.resolverModeración(7, dto).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/moderacion/7/resolver`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(dto);
    request.flush(null);
  });
});
