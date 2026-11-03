import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CasoListItem, CasoService } from '../../../core/services/caso.service';
import { FEED_PANEL } from '../../../core/utils/panel.utils';
import { pageContent } from '../../../core/utils/page.utils';
import { EstadoCarga } from '../../../core/utils/request.utils';
import { CasoRowComponent } from '../caso-row/caso-row.component';
import { DashFiguresComponent, Figura } from '../dash-figures/dash-figures.component';
import { PageHeroComponent } from '../page-hero/page-hero.component';

/** Una lista del panel: título, casos, texto vacío y el botón de cada fila. */
export interface ListaPanel {
  titulo: string;
  casos: CasoListItem[];
  vacio: string;
  accion: string;
  /** Texto cuando la lista depende de un endpoint que el backend aún no publica. */
  pendiente?: string;
}

/** Lo que cambia entre el panel de la fundación y el de la veterinaria. */
export interface PanelConfig {
  titulo: string;
  resumen: string;
  cta: { texto: string; ruta: string };
  figuras: (casos: CasoListItem[]) => Figura[];
  listas: (casos: CasoListItem[]) => ListaPanel[];
}

/**
 * Espacio de trabajo de la fundación y la veterinaria: carga los casos de la zona y arma
 * el saludo, las cifras y las listas con la configuración de cada rol.
 */
@Component({
  selector: 'app-panel-trabajo',
  imports: [RouterLink, CasoRowComponent, DashFiguresComponent, PageHeroComponent],
  templateUrl: './panel-trabajo.component.html',
})
export class PanelTrabajoComponent implements OnInit {
  private casoService = inject(CasoService);

  readonly config = input.required<PanelConfig>();

  readonly carga = new EstadoCarga();
  readonly casos = signal<CasoListItem[]>([]);
  readonly figuras = computed(() => this.config().figuras(this.casos()));
  readonly listas = computed(() => this.config().listas(this.casos()));

  ngOnInit(): void {
    this.carga.ejecutar(this.casoService.listarCasos(FEED_PANEL), (page) =>
      this.casos.set(pageContent(page)),
    );
  }
}
