import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

/** Datos de la ruta que muestra esta página (acceso denegado, 404, sección en desarrollo). */
export interface AvisoData {
  icono: string;
  titulo: string;
  mensaje: string;
}

/** Página de aviso genérica: el contenido sale de route.data. */
@Component({
  selector: 'app-aviso',
  imports: [RouterLink],
  templateUrl: './aviso.component.html',
  styleUrl: './aviso.component.css',
})
export class AvisoComponent {
  readonly aviso = inject(ActivatedRoute).snapshot.data as AvisoData;
}
