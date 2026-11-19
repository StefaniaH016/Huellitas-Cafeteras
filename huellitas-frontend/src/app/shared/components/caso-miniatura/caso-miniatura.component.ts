import { Component, computed, input, signal } from '@angular/core';

@Component({
  selector: 'app-caso-miniatura',
  template: `
    @if (imagen() && !fotoRota()) {
      <img [src]="imagen()" [alt]="alt()" (error)="fotoRota.set(true)" />
    } @else {
      <span aria-hidden="true">🐾</span>
    }
  `,
  host: {
    class: 'list-thumb',
  },
})
export class CasoMiniaturaComponent {
  readonly foto = input<string | null | undefined>(null);
  readonly fotoPrincipal = input<string | null | undefined>(null);
  readonly alt = input('Foto principal del caso');
  readonly fotoRota = signal(false);
  readonly imagen = computed(() => this.fotoPrincipal() || this.foto());
}
