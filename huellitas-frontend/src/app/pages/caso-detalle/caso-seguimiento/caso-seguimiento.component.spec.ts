import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AporteService } from '../../../core/services/aporte.service';
import { AuthService } from '../../../core/services/auth.service';
import { CasoDetalle, CasoService } from '../../../core/services/caso.service';
import { CasoSeguimientoComponent } from './caso-seguimiento.component';

const caso: CasoDetalle = {
  id: 3,
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

let aporteService: { crear: ReturnType<typeof vi.fn> };
let casoService: { subirImagen: ReturnType<typeof vi.fn> };

function crear(): CasoSeguimientoComponent {
  TestBed.configureTestingModule({
    imports: [CasoSeguimientoComponent],
    providers: [
      { provide: AporteService, useValue: aporteService },
      { provide: CasoService, useValue: casoService },
      { provide: AuthService, useValue: { tieneRol: vi.fn().mockReturnValue(false) } },
    ],
  });
  const fixture = TestBed.createComponent(CasoSeguimientoComponent);
  fixture.componentRef.setInput('caso', caso);
  fixture.detectChanges();
  return fixture.componentInstance;
}

beforeEach(() => {
  aporteService = { crear: vi.fn().mockReturnValue(of({})) };
  casoService = {
    subirImagen: vi.fn().mockReturnValue(of({ url: 'foto.jpg', publicId: 'p' })),
  };
});

it('merges contributions and state changes, newest first', () => {
  const component = crear();

  expect(component.actividad().map((a) => a.clave)).toEqual(['a1', 'h1']);
  expect(component.puedeAportar()).toBe(true);
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
    ubicacionTexto: 'Parque Sucre',
    fotografias: ['foto.jpg'],
  });
  expect(component.publicado()).toBe(true);
  expect(actualizado).toHaveBeenCalled();
});
