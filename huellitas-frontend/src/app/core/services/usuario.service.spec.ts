import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ActualizarPerfilRequest, UsuarioService } from './usuario.service';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/usuarios/me`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('gets the current profile', () => {
    service.getMiPerfil().subscribe();
    const request = http.expectOne(url);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 1 });
  });

  it('updates the current profile', () => {
    const dto: ActualizarPerfilRequest = {
      nombre: 'Luna',
      barrioId: 1,
      disponibleRescate: true,
    };
    service.actualizarPerfil(dto).subscribe();
    const request = http.expectOne(url);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(dto);
    request.flush({
      id: 1,
      nombre: 'Luna',
      correo: 'luna@test.com',
      rol: 'CIUDADANO_VOLUNTARIO',
      verificado: true,
      barrioId: 1,
      disponibleRescate: true,
    });
  });
});
