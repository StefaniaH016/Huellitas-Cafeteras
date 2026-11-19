import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

interface Seccion {
  id: string;
  titulo: string;
}

/**
 * Términos y Condiciones de Uso de Huellitas Cafeteras.
 * Página pública: se enlaza desde el registro (casilla de aceptación) y desde el footer.
 */
@Component({
  selector: 'app-terminos',
  imports: [RouterLink],
  templateUrl: './terminos.component.html',
  styleUrl: './terminos.component.css',
})
export class TerminosComponent {
  private router = inject(Router);

  readonly version = '1.0';
  readonly vigenteDesde = '28 de octubre de 2026';
  readonly correoContacto = 'soporte@huellitascafeteras.co';

  readonly secciones: Seccion[] = [
    { id: 'objeto', titulo: 'Objeto y aceptación' },
    { id: 'cuentas', titulo: 'Cuentas y roles' },
    { id: 'reglas', titulo: 'Reglas de uso' },
    { id: 'casos', titulo: 'Casos, custodia y cierre' },
    { id: 'responsabilidad', titulo: 'Límites de responsabilidad' },
    { id: 'disponibilidad', titulo: 'Disponibilidad y propiedad intelectual' },
    { id: 'moderacion', titulo: 'Moderación y cancelación' },
    { id: 'cambios', titulo: 'Cambios, ley aplicable y contacto' },
  ];

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
