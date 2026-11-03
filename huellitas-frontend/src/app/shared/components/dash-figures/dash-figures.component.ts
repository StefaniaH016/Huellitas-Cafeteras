import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Una cifra del resumen: número, texto, a dónde lleva y, si aplica, una barra de cupo. */
export interface Figura {
  valor: number | string;
  texto: string;
  ruta?: string;
  queryParams?: Record<string, string>;
  urgente?: boolean;
  maximo?: number;
}

/** Franja de cifras del inicio y de los paneles. */
@Component({
  selector: 'app-dash-figures',
  imports: [RouterLink],
  templateUrl: './dash-figures.component.html',
  host: { class: 'dash-figures', role: 'region' },
})
export class DashFiguresComponent {
  readonly figuras = input.required<Figura[]>();
}
