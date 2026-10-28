import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { UbicacionService } from './ubicacion.service';

describe('UbicacionService', () => {
  let service: UbicacionService;
  let http: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/ubicaciones`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UbicacionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads departments', () => {
    service.getDepartamentos().subscribe();
    const request = http.expectOne(`${baseUrl}/departamentos`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('loads municipalities by department', () => {
    service.getMunicipios(3).subscribe();
    const request = http.expectOne(`${baseUrl}/departamentos/3/municipios`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('loads neighborhoods by municipality', () => {
    service.getBarrios(8).subscribe();
    const request = http.expectOne(`${baseUrl}/municipios/8/barrios`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
