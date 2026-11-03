import { Component, input } from '@angular/core';

/** Saludo y resumen de arriba de cada pantalla; los botones se proyectan. */
@Component({
  selector: 'app-page-hero',
  templateUrl: './page-hero.component.html',
  host: { class: 'dash-hello' },
})
export class PageHeroComponent {
  readonly titulo = input.required<string>();
  readonly resumen = input<string>();
}
