import { Component, ViewEncapsulation, inject, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LEGAL_VERSION, LEGAL_VIGENTE_DESDE, LegalSeccion } from '../../../core/utils/legal.utils';

/**
 * Plantilla de los documentos legales: encabezado, índice, secciones proyectadas y pie.
 * Sin encapsulación porque las secciones vienen del componente que la usa; todas las reglas
 * van bajo .policy.
 */
@Component({
  selector: 'app-legal-page',
  imports: [RouterLink],
  templateUrl: './legal-page.component.html',
  styleUrl: './legal-page.component.css',
  encapsulation: ViewEncapsulation.None,
})
export class LegalPageComponent {
  private router = inject(Router);

  readonly titulo = input.required<string>();
  readonly secciones = input.required<LegalSeccion[]>();
  /** Enlace del pie al otro documento legal. */
  readonly otroDocumento = input.required<{ ruta: string; texto: string }>();

  readonly version = LEGAL_VERSION;
  readonly vigenteDesde = LEGAL_VIGENTE_DESDE;

  /** Si se abrió en otra pestaña desde el registro, la cierra; si no, vuelve atrás. */
  volver(): void {
    if (window.history.length > 1) {
      window.history.back();
      return;
    }
    window.close();
    setTimeout(() => void this.router.navigate(['/']), 150);
  }

  irA(id: string, event: Event): void {
    event.preventDefault();
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}
