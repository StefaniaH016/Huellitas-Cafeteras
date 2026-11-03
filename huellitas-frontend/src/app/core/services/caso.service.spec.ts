import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CasoService } from './caso.service';
import { BitacoraService } from './bitacora.service';
import { environment } from '../../../environments/environment';

const apiUrl = `${environment.apiUrl}/casos`;

function setupCasoServiceTest(): { service: CasoService; httpMock: HttpTestingController } {
  TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      CasoService,
      {
        provide: BitacoraService,
        useValue: {
          getBitacora: vi.fn(),
          abrirBitacora: vi.fn(),
          agregarEvolucion: vi.fn(),
          registrarAlta: vi.fn(),
        },
      },
    ],
  });
  return {
    service: TestBed.inject(CasoService),
    httpMock: TestBed.inject(HttpTestingController),
  };
}

describe('CasoService: gestionarCustodia', () => {
  let service: CasoService;
  let httpMock: HttpTestingController;
  beforeEach(() => {
    const setup = setupCasoServiceTest();
    service = setup.service;
    httpMock = setup.httpMock;
  });
  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });
  function testPatchRequest(casoId: number, dto: { asumir: boolean }): void {
    service.gestionarCustodia(casoId, dto).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/${casoId}/custodia`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  }
  it('should PATCH /api/casos/{id}/custodia', () => testPatchRequest(5, { asumir: true }));
  it('should use correct id and dto', () => testPatchRequest(10, { asumir: false }));
  it('should return server response', () => {
    const casoId = 3;
    return new Promise<void>((resolve) => {
      service.gestionarCustodia(casoId, { asumir: true }).subscribe((result) => {
        expect(result).toEqual({ id: casoId, custodia: true });
        resolve();
      });
      httpMock.expectOne(`${apiUrl}/${casoId}/custodia`).flush({ id: casoId, custodia: true });
    });
  });
  it('should use PATCH not POST or PUT', () => {
    const casoId = 7;
    service.gestionarCustodia(casoId, { asumir: false }).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/${casoId}/custodia`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });
});

describe('CasoService: listarCasosEnCustodia', () => {
  let service: CasoService;
  let httpMock: HttpTestingController;
  beforeEach(() => {
    const setup = setupCasoServiceTest();
    service = setup.service;
    httpMock = setup.httpMock;
  });
  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });
  it('should GET /api/casos/en-custodia', () => {
    service.listarCasosEnCustodia().subscribe();
    const req = httpMock.expectOne(`${apiUrl}/en-custodia`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
  it('should include pageable params', () => {
    service.listarCasosEnCustodia({ page: 1, size: 10 }).subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === `${apiUrl}/en-custodia` && r.params.get('page') === '1',
    );
    req.flush([]);
  });
  it('should return array', () => {
    return new Promise<void>((resolve) => {
      service.listarCasosEnCustodia().subscribe((result) => {
        expect(Array.isArray(result)).toBe(true);
        resolve();
      });
      httpMock.expectOne(`${apiUrl}/en-custodia`).flush([{ id: 5, titulo: 'Caso A' }]);
    });
  });
  it('should not include params when not provided', () => {
    service.listarCasosEnCustodia().subscribe();
    const req = httpMock.expectOne(`${apiUrl}/en-custodia`);
    expect(req.request.params.keys().length).toBe(0);
    req.flush([]);
  });
});

describe('CasoService: listarCasosSinCustodia', () => {
  let service: CasoService;
  let httpMock: HttpTestingController;
  beforeEach(() => {
    const setup = setupCasoServiceTest();
    service = setup.service;
    httpMock = setup.httpMock;
  });
  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });
  function expectSinCustodiaRequest(
    municipioId: number,
    pageable?: { page: number; size: number },
  ): ReturnType<typeof httpMock.expectOne> {
    return httpMock.expectOne((r) => {
      const ok =
        r.url === `${apiUrl}/sin-custodia` && r.params.get('municipioId') === String(municipioId);
      return (
        ok &&
        (!pageable ||
          (r.params.get('page') === String(pageable.page) &&
            r.params.get('size') === String(pageable.size)))
      );
    });
  }
  it('should GET with municipioId param', () => {
    service.listarCasosSinCustodia(15).subscribe();
    const req = expectSinCustodiaRequest(15);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
  it('should include pageable params', () => {
    service.listarCasosSinCustodia(20, { page: 2, size: 15 }).subscribe();
    expectSinCustodiaRequest(20, { page: 2, size: 15 }).flush([]);
  });
  it('should return array', () => {
    return new Promise<void>((resolve) => {
      service.listarCasosSinCustodia(10).subscribe((result) => {
        expect(Array.isArray(result)).toBe(true);
        resolve();
      });
      expectSinCustodiaRequest(10).flush([{ id: 100, titulo: 'Caso 1' }]);
    });
  });
  it('should combine municipioId with pageable', () => {
    service.listarCasosSinCustodia(25, { page: 0, size: 20 }).subscribe();
    const req = expectSinCustodiaRequest(25, { page: 0, size: 20 });
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
