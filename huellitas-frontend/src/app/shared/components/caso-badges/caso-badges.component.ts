import { Component, input } from '@angular/core';
import type { EstadoCaso, TipoCaso, Urgencia } from '../../../core/services/caso.service';
import {
  ESTADO_LABELS,
  TIPO_LABELS,
  URGENCIA_LABELS,
  estadoBadge,
  labelOf,
  urgenciaBadge,
} from '../../../core/utils/caso-display.utils';

/** Etiquetas de urgencia, tipo y estado de un caso. Se muestran solo las que llegan. */
@Component({
  selector: 'app-caso-badges',
  templateUrl: './caso-badges.component.html',
  host: { class: 'badge-row' },
})
export class CasoBadgesComponent {
  readonly urgencia = input<Urgencia | null>();
  readonly tipo = input<TipoCaso | null>();
  readonly estado = input<EstadoCaso | null>();

  protected readonly urgenciaBadge = urgenciaBadge;
  protected readonly estadoBadge = estadoBadge;

  protected urgenciaLabel(value: Urgencia): string {
    return labelOf(URGENCIA_LABELS, value);
  }

  protected tipoLabel(value: TipoCaso): string {
    return labelOf(TIPO_LABELS, value);
  }

  protected estadoLabel(value: EstadoCaso): string {
    return labelOf(ESTADO_LABELS, value);
  }
}
