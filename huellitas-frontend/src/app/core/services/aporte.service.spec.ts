import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AporteService } from './aporte.service';
import { OcultarAporteDTO } from '../../../core/models/dtos';
import { environment } from '../../../environments/environment';

// eslint-disable-next-line max-lines-per-function
describe('AporteService', () => {
  let service: AporteService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/casos`;
  const moderacionUrl = `${environment.apiUrl}/moderacion/aportes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AporteService],
    });
    service = TestBed.inject(AporteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  // eslint-disable-next-line max-lines-per-function
  describe('listar', () => {
    it('should make GET request to /api/casos/{casoId}/aportes', () => {
      const casoId = 5;
      service.listar(casoId).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should pass correct casoId in URL', () => {
      const casoId = 42;
      service.listar(casoId).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      expect(req.request.url).toContain(`/casos/${casoId}/aportes`);
      req.flush([]);
    });

    it('should return Observable with array of aportes', () => {
      const casoId = 10;
      const mockResponse = [
        {
          id: 1,
          casoId: 10,
          autorId: 1,
          descripcion: 'Aporte 1',
          ubicacionTexto: 'Calle 1',
          fotografias: [],
          fecha: '2024-01-01',
        },
        {
          id: 2,
          casoId: 10,
          autorId: 2,
          descripcion: 'Aporte 2',
          ubicacionTexto: 'Calle 2',
          fotografias: [],
          fecha: '2024-01-02',
        },
      ];

      let result: unknown;
      service.listar(casoId).subscribe((r) => {
        result = r;
      });

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      req.flush(mockResponse);
      expect(Array.isArray(result)).toBe(true);
      expect((result as typeof mockResponse).length).toBe(2);
      expect(result).toEqual(mockResponse);
    });

    it('should handle empty array response', () => {
      const casoId = 99;
      let result: unknown;

      service.listar(casoId).subscribe((r) => {
        result = r;
      });

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      req.flush([]);
      expect(Array.isArray(result)).toBe(true);
      expect((result as unknown[]).length).toBe(0);
    });
  });

  // eslint-disable-next-line max-lines-per-function
  describe('crear', () => {
    it('should make POST request to /api/casos/{casoId}/aportes', () => {
      const casoId = 5;
      const dto = {
        descripcion: 'Nueva información',
        fotografias: ['url1.jpg', 'url2.jpg'],
      };

      service.crear(casoId, dto).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });

    it('should send CrearAporteDTO in request body', () => {
      const casoId = 7;
      const dto = {
        descripcion: 'Avistamiento en parque',
        deptoId: 76,
        munId: 5,
        barrioId: 10,
        ubicacionTexto: 'Parque central',
        fotografias: ['photo1.jpg', 'photo2.jpg'],
      };

      service.crear(casoId, dto).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      expect(req.request.body).toEqual(dto);
      req.flush({});
    });

    it('should pass correct casoId in URL', () => {
      const casoId = 15;
      const dto = {
        descripcion: 'Test',
        fotografias: [],
      };

      service.crear(casoId, dto).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      expect(req.request.url).toContain(`/casos/${casoId}/aportes`);
      req.flush({});
    });

    it('should return Observable with created Aporte response', () => {
      const casoId = 3;
      const dto = {
        descripcion: 'Información adicional',
        fotografias: ['test.jpg'],
      };
      const mockResponse = {
        id: 100,
        casoId: 3,
        autorId: 5,
        descripcion: 'Información adicional',
        ubicacionTexto: '',
        fotografias: ['test.jpg'],
        fecha: '2024-01-15',
      };

      let result: unknown;
      service.crear(casoId, dto).subscribe((r) => {
        result = r;
      });

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      req.flush(mockResponse);
      expect(result).toEqual(mockResponse);
      expect((result as typeof mockResponse).id).toBe(100);
    });

    it('should include all optional fields when provided', () => {
      const casoId = 20;
      const dto = {
        descripcion: 'Reporte completo',
        deptoId: 76,
        munId: 8,
        barrioId: 25,
        ubicacionTexto: 'Esquina de calle principal',
        fotografias: ['image1.jpg', 'image2.jpg', 'image3.jpg'],
      };

      service.crear(casoId, dto).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${casoId}/aportes`);
      expect(req.request.body).toEqual(dto);
      expect(req.request.body.deptoId).toBe(76);
      expect(req.request.body.munId).toBe(8);
      expect(req.request.body.barrioId).toBe(25);
      expect(req.request.body.ubicacionTexto).toBe('Esquina de calle principal');
      req.flush({});
    });
  });

  // eslint-disable-next-line max-lines-per-function
  describe('ocultarAporte', () => {
    it('should make PATCH request to /api/moderacion/aportes/{id}/ocultar', () => {
      const aporteId = 5;
      const motivo = 'Contenido inapropiado';

      service.ocultarAporte(aporteId, motivo).subscribe();

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      expect(req.request.method).toBe('PATCH');
      req.flush({});
    });

    it('should send OcultarAporteDTO as object, not just string', () => {
      const aporteId = 10;
      const motivo = 'Spam detectado';

      service.ocultarAporte(aporteId, motivo).subscribe();

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      expect(req.request.body).toEqual({ motivo });
      expect(typeof req.request.body).toBe('object');
      expect(req.request.body.motivo).toBe('Spam detectado');
      req.flush({});
    });

    it('should verify DTO structure has motivo property', () => {
      const aporteId = 7;
      const motivo = 'Violación de términos de servicio';

      service.ocultarAporte(aporteId, motivo).subscribe();

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      const body = req.request.body as OcultarAporteDTO;
      expect(body).toHaveProperty('motivo');
      expect(body.motivo).toBe('Violación de términos de servicio');
      req.flush({});
    });

    it('should pass correct id in URL path', () => {
      const aporteId = 42;
      const motivo = 'Prueba';

      service.ocultarAporte(aporteId, motivo).subscribe();

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      expect(req.request.url).toContain(`/aportes/${aporteId}/ocultar`);
      req.flush({});
    });

    it('should pass correct id and motivo in single call', () => {
      const aporteId = 88;
      const motivo = 'Imagen ofensiva';

      service.ocultarAporte(aporteId, motivo).subscribe();

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.url).toContain(`/${aporteId}/ocultar`);
      expect(req.request.body.motivo).toBe('Imagen ofensiva');
      req.flush({});
    });

    it('should return Observable<Aporte> with server response', () => {
      const aporteId = 15;
      const motivo = 'Contenido duplicado';
      const mockResponse = {
        id: 15,
        casoId: 5,
        autorId: 2,
        descripcion: 'Aporte oculto',
        ubicacionTexto: 'Ubicación',
        fotografias: [],
        fecha: '2024-01-10',
      };

      let result: unknown;
      service.ocultarAporte(aporteId, motivo).subscribe((r) => {
        result = r;
      });

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      req.flush(mockResponse);
      expect(result).toEqual(mockResponse);
      expect((result as typeof mockResponse).id).toBe(15);
    });

    it('should use PATCH method, not POST or PUT', () => {
      const aporteId = 20;
      const motivo = 'Test';

      service.ocultarAporte(aporteId, motivo).subscribe();

      const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.method).not.toBe('POST');
      expect(req.request.method).not.toBe('PUT');
      req.flush({});
    });

    it('should handle different motivo texts', () => {
      const testCases = [
        { aporteId: 1, motivo: 'Motivo corto' },
        { aporteId: 2, motivo: 'Este es un motivo más largo con múltiples palabras' },
        { aporteId: 3, motivo: 'Motivo con acentuación: é, á, ó, ú' },
        { aporteId: 4, motivo: 'Motivo con caracteres especiales: @#$%' },
      ];

      testCases.forEach(({ aporteId, motivo }) => {
        service.ocultarAporte(aporteId, motivo).subscribe();

        const req = httpMock.expectOne(`${moderacionUrl}/${aporteId}/ocultar`);
        expect(req.request.body.motivo).toBe(motivo);
        req.flush({});
      });
    });
  });
});
