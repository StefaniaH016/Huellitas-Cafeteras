import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CasoListItem, CasoService } from '../../../core/services/caso.service';
import { FEED_PANEL } from '../../../core/utils/panel.utils';
import { PanelFundacionComponent } from '../../../pages/panel-fundacion/panel-fundacion.component';
import { PanelVeterinariaComponent } from '../../../pages/panel-veterinaria/panel-veterinaria.component';
import { PanelTrabajoComponent } from './panel-trabajo.component';

const casos: CasoListItem[] = [
  {
    id: 1,
    tipoCaso: 'HERIDO',
    estado: 'REGISTRADO',
    urgencia: 'CRITICA',
    descripcion: 'a',
    municipio: 'Armenia',
    barrio: 'Centro',
  },
  {
    id: 2,
    tipoCaso: 'VULNERABLE',
    estado: 'EN_ATENCION',
    urgencia: 'MEDIA',
    descripcion: 'b',
    municipio: 'Armenia',
    barrio: 'Centro',
  },
  {
    id: 3,
    tipoCaso: 'EXTRAVIADO',
    estado: 'RESUELTO',
    urgencia: 'BAJA',
    descripcion: 'c',
    municipio: 'Armenia',
    barrio: 'Centro',
  },
];
let listarCasos: ReturnType<typeof vi.fn>;

function panel(
  tipo: typeof PanelFundacionComponent | typeof PanelVeterinariaComponent,
): PanelTrabajoComponent {
  TestBed.configureTestingModule({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    imports: [tipo as any],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: { listarCasos } },
      { provide: AuthService, useValue: { getNombre: vi.fn().mockReturnValue('Patitas') } },
    ],
  });
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const fixture = TestBed.createComponent(tipo as any);
  fixture.detectChanges();
  return fixture.debugElement.children[0].componentInstance as unknown as PanelTrabajoComponent;
}

beforeEach(() => {
  listarCasos = vi.fn().mockReturnValue(of({ content: casos, totalElements: 3 }));
});

it.skip('builds the foundation panel from the zone feed', () => {
  const trabajo = panel(PanelFundacionComponent);

  expect(listarCasos).toHaveBeenCalledWith(FEED_PANEL);
  expect(trabajo.config().titulo).toBe('Mi panel · Patitas');
  expect(trabajo.figuras().map((f) => f.valor)).toEqual(['—', 1, 1]);
  expect(trabajo.listas()[0].casos.map((c) => c.id)).toEqual([1]);
  expect(trabajo.listas()[1].pendiente).toContain('backend');
});

it.skip('builds the veterinary panel with open injured animals', () => {
  const trabajo = panel(PanelVeterinariaComponent);

  expect(trabajo.figuras()[0].valor).toBe(1);
  expect(trabajo.listas()[0].casos.map((c) => c.id)).toEqual([1]);
});
