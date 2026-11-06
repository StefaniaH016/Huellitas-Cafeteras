import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AporteService } from '../../../core/services/aporte.service';
import { AuthService } from '../../../core/services/auth.service';
import { CasoDetalle, CasoService } from '../../../core/services/caso.service';
import { CasoSeguimientoComponent } from './caso-seguimiento.component';

const caso: CasoDetalle = {
  id: 3,
  autorId: 7,
  tipoCaso: 'EXTRAVIADO',
  estado: 'REGISTRADO',
  urgencia: 'MEDIA',
  descripcion: 'Luna',
  municipio: 'Armenia',
  barrio: 'Centro',
  aportes: [
    {
      id: 1,
      tipo: 'AVISTAMIENTO',
      comentario: 'La vi',
      fecha: '2026-10-02T10:00:00Z',
      autorNombre: 'Carlos',
      zona: 'Centro · Armenia',
      ubicacionTexto: 'Parque Sucre, junto a la fuente',
      fotografias: ['aporte.jpg'],
    },
  ],
  historial: [
    {
      id: 1,
      estadoAnterior: 'REGISTRADO',
      estadoNuevo: 'REGISTRADO',
      fecha: '2026-10-01T10:00:00Z',
    },
  ],
};

let aporteService: {
  crear: ReturnType<typeof vi.fn>;
  ocultarAporte: ReturnType<typeof vi.fn>;
};
let casoService: { subirImagen: ReturnType<typeof vi.fn> };
let rolActual: string;

function crearFixture(
  options: {
    rol?: string;
    usuarioId?: number | null;
    organizacionId?: number | null;
    detalle?: CasoDetalle;
  } = {},
): ComponentFixture<CasoSeguimientoComponent> {
  rolActual = options.rol ?? 'CIUDADANO_VOLUNTARIO';
  TestBed.configureTestingModule({
    imports: [CasoSeguimientoComponent],
    providers: [
      { provide: AporteService, useValue: aporteService },
      { provide: CasoService, useValue: casoService },
      {
        provide: AuthService,
        useValue: { tieneRol: (rol: string): boolean => rolActual === rol },
      },
    ],
  });
  const fixture = TestBed.createComponent(CasoSeguimientoComponent);
  fixture.componentRef.setInput('caso', options.detalle ?? caso);
  fixture.componentRef.setInput('usuarioId', options.usuarioId ?? 7);
  fixture.componentRef.setInput('organizacionId', options.organizacionId ?? null);
  fixture.detectChanges();
  return fixture;
}

function crear(
  options: {
    rol?: string;
    usuarioId?: number | null;
    organizacionId?: number | null;
    detalle?: CasoDetalle;
  } = {},
): CasoSeguimientoComponent {
  return crearFixture(options).componentInstance;
}

beforeEach(() => {
  aporteService = {
    crear: vi.fn().mockReturnValue(of({})),
    ocultarAporte: vi.fn().mockReturnValue(of({ casoId: 1, decision: 'OCULTADO' })),
  };
  rolActual = 'CIUDADANO_VOLUNTARIO';
  casoService = {
    subirImagen: vi.fn().mockReturnValue(of({ url: 'foto.jpg', publicId: 'p' })),
  };
});

it('merges contributions and state changes, newest first', () => {
  const component = crear();

  expect(component.actividad().map((a) => a.clave)).toEqual(['a1', 'h1']);
  expect(component.actividad()[0].fotografias).toEqual(['aporte.jpg']);
  expect(component.actividad()[0].zona).toBe('Centro · Armenia');
  expect(component.actividad()[0].referencia).toBe('Parque Sucre, junto a la fuente');
  expect(component.puedeAportar()).toBe(true);
});

it('renders contribution photos in the follow-up activity', () => {
  const fixture = crearFixture();
  const component = fixture.componentInstance;
  const image = fixture.nativeElement.querySelector('.timeline-photos img') as HTMLImageElement;
  const location = fixture.nativeElement.querySelector('.timeline-location') as HTMLElement;
  expect(component.actividad()[0].fotografias).toEqual(['aporte.jpg']);
  expect(image.getAttribute('src')).toBe('aporte.jpg');
  expect(location.textContent).toContain('Zona: Centro · Armenia');
  expect(location.textContent).toContain('Punto de referencia: Parque Sucre, junto a la fuente');
});

it('shows medical attention activity once with its status transition', () => {
  const component = crear({
    detalle: {
      ...caso,
      aportes: [
        {
          id: 2,
          tipo: 'ACTUALIZACION',
          comentario: 'Atención médica iniciada por Clínica Huellitas',
          fecha: '2026-10-02T10:00:00Z',
          autorNombre: 'Clínica Huellitas',
        },
      ],
      historial: [
        {
          id: 2,
          estadoAnterior: 'REGISTRADO',
          estadoNuevo: 'EN_ATENCION',
          observacion: 'Atención médica iniciada por Clínica Huellitas',
          fecha: '2026-10-02T10:00:00Z',
        },
      ],
    },
  });
  expect(component.actividad().map((item) => item.clave)).toEqual(['a2']);
  expect(component.actividad()[0].cambio).toBe('Registrado → En atención');
});

it('lets administrators hide community contributions with a reason', () => {
  const fixture = crearFixture({ rol: 'ADMINISTRADOR' });
  const component = fixture.componentInstance;
  const actualizado = vi.fn();
  component.actualizado.subscribe(actualizado);

  expect(fixture.nativeElement.textContent).toContain('Ocultar aporte');
  component.abrirModeracionAporte(1);
  component.motivoOcultacion.set('Contenido inapropiado');
  component.ocultarAporte(1);

  expect(aporteService.ocultarAporte).toHaveBeenCalledWith(1, 'Contenido inapropiado');
  expect(actualizado).toHaveBeenCalledOnce();
  expect(component.moderacionAporte.exito()).toBe('Aporte ocultado correctamente.');
});

it('keeps Actions and Responsibles in the parent case layout', () => {
  rolActual = 'ADMINISTRADOR';
  TestBed.configureTestingModule({
    imports: [CasoSeguimientoComponent],
    providers: [
      { provide: AporteService, useValue: aporteService },
      { provide: CasoService, useValue: casoService },
      {
        provide: AuthService,
        useValue: { tieneRol: (rol: string): boolean => rolActual === rol },
      },
    ],
  });
  const fixture = TestBed.createComponent(CasoSeguimientoComponent);
  fixture.componentRef.setInput('caso', caso);
  fixture.detectChanges();

  const text = fixture.nativeElement.textContent as string;
  expect(text).not.toContain('Acciones');
  expect(text).not.toContain('Responsables');
});

it('allows a citizen outside the case to add a contribution', () => {
  const notReporter = crear({ usuarioId: 8 });
  expect(notReporter.puedeAportar()).toBe(true);
});

it('does not allow the reporter to update a case with a custodian', () => {
  const custodialCase = { ...caso, fundacionCustodioId: 42 };
  const reporterWithCustody = crear({ detalle: custodialCase });
  expect(reporterWithCustody.puedeAportar()).toBe(false);
});

it('allows a citizen outside the case to add a contribution when it has a custodian', () => {
  const custodialCase = { ...caso, fundacionCustodioId: 42 };
  const fixture = crearFixture({ usuarioId: 8, detalle: custodialCase });

  expect(fixture.componentInstance.puedeAportar()).toBe(true);
  expect(fixture.nativeElement.querySelector('#form-aporte')).not.toBeNull();
});

it('allows the foundation that holds custody to add follow-up', () => {
  const custodialCase = { ...caso, fundacionCustodioId: 42 };
  const foundation = crear({
    rol: 'FUNDACION_REFUGIO',
    organizacionId: 42,
    detalle: custodialCase,
  });
  expect(foundation.puedeAportar()).toBe(true);
});

it('does not allow follow-up on resolved or archived cases', () => {
  const fixture = crearFixture();
  const component = fixture.componentInstance;

  fixture.componentRef.setInput('caso', { ...caso, estado: 'RESUELTO' });
  expect(component.puedeAportar()).toBe(false);

  fixture.componentRef.setInput('caso', { ...caso, estado: 'ARCHIVADO' });
  expect(component.puedeAportar()).toBe(false);
});

it('validates the text and the sighting place', () => {
  const component = crear();
  component.descripcion = 'corto';
  expect(component.errorFormulario()).toContain('10 caracteres');

  component.descripcion = 'La vi en el parque principal';
  component.tipo.set('AVISTAMIENTO');
  expect(component.errorFormulario()).toContain('dónde');
});

it('uploads the photos and publishes the contribution', () => {
  const component = crear();
  const actualizado = vi.fn();
  component.actualizado.subscribe(actualizado);
  component.tipo.set('AVISTAMIENTO');
  component.descripcion = 'La vi en el parque principal';
  component.ubicacionTexto = 'Parque Sucre';
  component.fotos.set([new File([''], 'foto.jpg')]);

  component.publicar();

  expect(aporteService.crear).toHaveBeenCalledWith(3, {
    descripcion: 'La vi en el parque principal',
    barrioId: undefined,
    ubicacionTexto: 'Parque Sucre',
    fotografias: ['foto.jpg'],
  });
  expect(component.publicado()).toBe(true);
  expect(component.envio.cargando()).toBe(false);
  expect(actualizado).toHaveBeenCalled();
});
