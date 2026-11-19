import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CasoRowComponent } from './caso-row.component';

it('links to the case and shows place and badges', () => {
  TestBed.configureTestingModule({ imports: [CasoRowComponent], providers: [provideRouter([])] });
  const fixture = TestBed.createComponent(CasoRowComponent);
  fixture.componentRef.setInput('caso', {
    id: 9,
    tipoCaso: 'EN_ADOPCION',
    estado: 'REGISTRADO',
    urgencia: 'BAJA',
    descripcion: 'Gatita',
    municipio: 'Armenia',
    barrio: 'Centro',
  });
  fixture.componentRef.setInput('mostrarTipo', true);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;

  expect(el.querySelector('a')?.getAttribute('href')).toBe('/caso-detalle/9');
  expect(el.textContent).toContain('Armenia · Centro');
  expect(el.textContent).toContain('En adopción');
});
