import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { BitacoraService } from '../../core/services/bitacora.service';
import { CasoService } from '../../core/services/caso.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { PanelVeterinariaComponent } from './panel-veterinaria.component';

const pacientes = [
  {
    id: 8,
    casoId: 4,
    nombre: 'Luna',
    especie: 'PERRO',
    raza: 'Criollo',
    sexo: 'HEMBRA',
    descripcion: 'Paciente en recuperación',
    urgencia: 'ALTA',
    estado: 'EN_ATENCION',
    municipio: 'Armenia',
    barrio: 'Centro',
    foto: null,
    fechaApertura: '2026-11-04T10:00:00',
    altaMedica: false,
    fechaAlta: null,
    proximoControl: null,
  },
  {
    id: 9,
    casoId: 5,
    nombre: 'Max',
    especie: 'PERRO',
    raza: null,
    sexo: null,
    descripcion: 'Alta registrada',
    urgencia: 'MEDIA',
    estado: 'ALTA_REGISTRADA',
    municipio: 'Armenia',
    barrio: null,
    foto: null,
    fechaApertura: '2026-11-03T10:00:00',
    altaMedica: true,
    fechaAlta: '2026-11-04T10:00:00',
    proximoControl: null,
  },
];

let bitacoraService: { listarMisPacientes: ReturnType<typeof vi.fn> };
let casoService: { listarCasos: ReturnType<typeof vi.fn> };
let usuarioService: { getMiPerfil: ReturnType<typeof vi.fn> };

beforeEach(() => {
  bitacoraService = { listarMisPacientes: vi.fn().mockReturnValue(of(pacientes)) };
  casoService = {
    listarCasos: vi.fn().mockReturnValue(of({ content: [], totalElements: 0 })),
  };
  usuarioService = {
    getMiPerfil: vi.fn().mockReturnValue(
      of({
        id: 2,
        nombre: 'Dra. Ana',
        municipioId: 31,
        organizacionNombre: 'Clínica Huellitas',
      }),
    ),
  };
  TestBed.configureTestingModule({
    imports: [PanelVeterinariaComponent],
    providers: [
      provideRouter([]),
      { provide: BitacoraService, useValue: bitacoraService },
      { provide: CasoService, useValue: casoService },
      { provide: UsuarioService, useValue: usuarioService },
      { provide: AuthService, useValue: { getNombre: vi.fn().mockReturnValue('Vet Norte') } },
    ],
  });
});

it('loads only active patients from the medical-log endpoint', () => {
  const fixture = TestBed.createComponent(PanelVeterinariaComponent);
  fixture.detectChanges();

  const component = fixture.componentInstance;
  expect(bitacoraService.listarMisPacientes).toHaveBeenCalledOnce();
  expect(component.veterinariaPacientes().map((paciente) => paciente.casoId)).toEqual([4]);
  expect(component.veterinariaStats().pacientesActivos).toBe(1);
  expect(component.nombreOrganizacion()).toBe('Clínica Huellitas');
  expect(casoService.listarCasos).toHaveBeenCalledWith({
    municipioId: 31,
    estado: 'REGISTRADO',
    page: 0,
    size: 10,
  });
  expect(fixture.nativeElement.textContent).toContain('Mi panel · Clínica Huellitas');
  expect(fixture.nativeElement.textContent).toContain('Luna');
  expect(fixture.nativeElement.textContent).toContain('Caso #4');
  expect(fixture.nativeElement.textContent).toContain('Max');
  expect(component.cargando()).toBe(false);
});

it('shows registered cases as requests for veterinary attention', () => {
  vi.mocked(casoService.listarCasos).mockReturnValue(
    of({
      content: [
        {
          id: 14,
          descripcion: 'Gato herido',
          estado: 'REGISTRADO',
          urgencia: 'ALTA',
          municipio: 'Armenia',
          barrio: 'Centro',
        },
      ],
      totalElements: 1,
    }),
  );
  const fixture = TestBed.createComponent(PanelVeterinariaComponent);
  fixture.detectChanges();

  expect(fixture.componentInstance.veterinariaSolicitudes().map((caso) => caso.id)).toEqual([14]);
  expect(fixture.nativeElement.textContent).toContain('Casos sin atención médica');
  expect(fixture.nativeElement.textContent).toContain('Gato herido');
});

it('navigates to the case ID, not the bitácora ID', async () => {
  const component = TestBed.createComponent(PanelVeterinariaComponent).componentInstance;
  const router = TestBed.inject(Router);
  const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

  component.abrirBitacora(4);

  expect(navigate).toHaveBeenCalledWith(['/caso-detalle', 4], {
    queryParams: { tab: 'bitacora', formulario: 'entrada' },
  });
});

it('delegates badge helpers', () => {
  const component = TestBed.createComponent(PanelVeterinariaComponent).componentInstance;

  expect(component.getBadgeClass('ALTA')).toBe('badge-alta');
  expect(component.getBadgeLabel('CRITICA')).toBe('Emergencia');
});
