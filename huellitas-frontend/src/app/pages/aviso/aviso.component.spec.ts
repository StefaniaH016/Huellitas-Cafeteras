import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { AvisoComponent } from './aviso.component';

it('shows the message from the route data', () => {
  const data = { icono: '🚫', titulo: 'Acceso denegado', mensaje: 'Sin permiso.' };
  TestBed.configureTestingModule({
    imports: [AvisoComponent],
    providers: [provideRouter([]), { provide: ActivatedRoute, useValue: { snapshot: { data } } }],
  });
  const fixture = TestBed.createComponent(AvisoComponent);
  fixture.detectChanges();

  expect((fixture.nativeElement as HTMLElement).textContent).toContain('Acceso denegado');
});
