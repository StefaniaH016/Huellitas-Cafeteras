import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
  TestRequest,
} from '@angular/common/http/testing';
import { CasoService, CierreCasoDTO } from './caso.service';
import { BitacoraService } from './bitacora.service';
import { environment } from '../../../environments/environment';
import { CustodiaDTO } from '../../../core/models/dtos';

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

function expectSinCustodiaRequest(
  httpMock: HttpTestingController,
  municipioId: number,
  pageable?: { page: number; size: number },
): TestRequest {
  return httpMock.expectOne((request) => {
    const matchesMunicipio =
      request.url === `${apiUrl}/sin-custodia` &&
      request.params.get('municipioId') === String(municipioId);
    const matchesPage =
      !pageable ||
      (request.params.get('page') === String(pageable.page) &&
        request.params.get('size') === String(pageable.size));
    return matchesMunicipio && matchesPage;
  });
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
  function testPatchRequest(casoId: number, dto: CustodiaDTO): void {
    service.gestionarCustodia(casoId, dto).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/${casoId}/custodia`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  }
  it('should PATCH /api/casos/{id}/custodia to assume', () =>
    testPatchRequest(5, { accion: 'ASUMIR' }));
  it('should PATCH /api/casos/{id}/custodia with a release reason', () =>
    testPatchRequest(10, { accion: 'LIBERAR', motivo: 'Requiere atención especializada' }));
  it('should return server response', () => {
    const casoId = 3;
    return new Promise<void>((resolve) => {
      service.gestionarCustodia(casoId, { accion: 'ASUMIR' }).subscribe((result) => {
        expect(result).toEqual({ id: casoId, custodia: true });
        resolve();
      });
      httpMock.expectOne(`${apiUrl}/${casoId}/custodia`).flush({ id: casoId, custodia: true });
    });
  });
  it('should use PATCH not POST or PUT', () => {
    const casoId = 7;
    service.gestionarCustodia(casoId, { accion: 'LIBERAR', motivo: 'Motivo' }).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/${casoId}/custodia`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });
});

describe('CasoService: archivo', () => {
  let service: CasoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    const setup = setupCasoServiceTest();
    service = setup.service;
    httpMock = setup.httpMock;
  });

  afterEach(() => httpMock.verify());

  it('sends case archive as PATCH with a required reason', () => {
    const dto = { motivo: 'Caso resuelto hace más de siete días' };
    service.archivarCaso(8, dto).subscribe();
    const request = httpMock.expectOne(`${apiUrl}/8/archivar`);

    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(dto);
    request.flush({});
  });
});

describe('CasoService: cierre de caso', () => {
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

  it('sends case closure with POST and the selected result', () => {
    const dto: CierreCasoDTO = {
      motivoCierre: 'ENTREGADO_A_CUIDADOR',
      entregadoA: 'Familia Pérez',
      observacion: 'Adoptado',
    };

    service.cerrarCaso(17, dto).subscribe();
    const request = httpMock.expectOne(`${apiUrl}/17/cierre`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(dto);
    request.flush({});
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
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    req.flush([]);
  });
  it('should return array', () => {
    const next = vi.fn();
    service.listarCasosEnCustodia().subscribe(next);
    const req = httpMock.expectOne(`${apiUrl}/en-custodia`);
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 5, titulo: 'Caso A' }]);
    expect(next).toHaveBeenCalledWith([{ id: 5, titulo: 'Caso A' }]);
  });
  it('should not include params when not provided', () => {
    service.listarCasosEnCustodia().subscribe();
    const req = httpMock.expectOne(`${apiUrl}/en-custodia`);
    expect(req.request.params.keys().length).toBe(0);
    req.flush([]);
  });
});

describe('CasoService: listarCasosSinCustodia filters', () => {
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
  it('should GET with municipioId param', () => {
    service.listarCasosSinCustodia(15).subscribe();
    const req = expectSinCustodiaRequest(httpMock, 15);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
  it('should include pageable params', () => {
    service.listarCasosSinCustodia(20, { page: 2, size: 15 }).subscribe();
    const req = expectSinCustodiaRequest(httpMock, 20, { page: 2, size: 15 });
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('municipioId')).toBe('20');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('15');
    req.flush([]);
  });
});

describe('CasoService: listarCasosSinCustodia results', () => {
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
  it('should return array', () => {
    const next = vi.fn();
    service.listarCasosSinCustodia(10).subscribe(next);
    const req = expectSinCustodiaRequest(httpMock, 10);
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 100, titulo: 'Caso 1' }]);
    expect(next).toHaveBeenCalledWith([{ id: 100, titulo: 'Caso 1' }]);
  });
  it('should combine municipioId with pageable', () => {
    service.listarCasosSinCustodia(25, { page: 0, size: 20 }).subscribe();
    const req = expectSinCustodiaRequest(httpMock, 25, { page: 0, size: 20 });
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
