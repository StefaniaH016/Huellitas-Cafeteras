import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import type { CasoListItem } from '../../../core/services/caso.service';
import { formatFecha, lugarTexto } from '../../../core/utils/caso-display.utils';
import { CasoBadgesComponent } from '../caso-badges/caso-badges.component';

/**
 * Fila de un caso en una lista (feed, Mis reportes, paneles). El título lleva al detalle
 * y los botones se proyectan con <ng-content>.
 */
@Component({
  selector: 'app-caso-row',
  imports: [RouterLink, CasoBadgesComponent],
  templateUrl: './caso-row.component.html',
  host: { class: 'list-row' },
})
export class CasoRowComponent {
  readonly caso = input.required<CasoListItem>();
  /** Texto bajo el título; por defecto, lugar y fecha del reporte. */
  readonly meta = input<string>();
  readonly mostrarTipo = input(false);
  readonly mostrarEstado = input(false);

  protected readonly fotoRota = signal(false);

  protected readonly metaTexto = computed(() => {
    const caso = this.caso();
    return this.meta() ?? lugarTexto(caso.municipio, caso.barrio, formatFecha(caso.fechaCreacion));
  });
}
