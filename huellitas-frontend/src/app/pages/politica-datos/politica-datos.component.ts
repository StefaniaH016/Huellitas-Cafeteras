import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

interface Seccion {
  id: string;
  titulo: string;
}

/**
 * Política de Tratamiento de Datos Personales (Ley 1581 de 2012, Decreto 1074 de 2015).
 * Página pública: se enlaza desde el registro (casilla de autorización) y desde el footer.
 */
@Component({
  selector: 'app-politica-datos',
  imports: [RouterLink],
  templateUrl: './politica-datos.component.html',
  styleUrl: './politica-datos.component.css',
})
export class PoliticaDatosComponent {
  readonly version = '1.0';
  readonly vigenteDesde = '28 de octubre de 2026';
  readonly correoContacto = 'soporte@huellitascafeteras.co';

  readonly secciones: Seccion[] = [
    { id: 'responsable', titulo: 'Responsable y marco legal' },
    { id: 'datos', titulo: 'Datos que recogemos y para qué' },
    { id: 'autorizacion', titulo: 'Autorización' },
    { id: 'acceso', titulo: 'Quién ve qué' },
    { id: 'encargados', titulo: 'Proveedores y transferencia' },
    { id: 'derechos', titulo: 'Tus derechos' },
    { id: 'procedimiento', titulo: 'Consultas y reclamos' },
    { id: 'conservacion', titulo: 'Seguridad y conservación' },
    { id: 'vigencia', titulo: 'Vigencia y cambios' },
  ];

  private router = inject(Router);

  /** Si se abrió en otra pestaña desde el registro, la cierra; si no, vuelve atrás. */
  volver(): void {
    if (window.history.length > 1) {
      window.history.back();
      return;
    }
    window.close();
    setTimeout(() => this.router.navigate(['/']), 150);
  }

  irA(id: string, event: Event): void {
    event.preventDefault();
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}
