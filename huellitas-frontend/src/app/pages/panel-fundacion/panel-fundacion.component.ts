import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import {
  getPanelBadgeClass,
  getPanelBadgeLabel,
  PanelCaso,
  scrollToDashboardPanel,
} from '../../core/utils/panel.utils';

@Component({
  selector: 'app-panel-fundacion',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './panel-fundacion.component.html',
  styleUrl: './panel-fundacion.component.css',
})
export class PanelFundacionComponent implements OnInit {
  private casoService = inject(CasoService);
  authService = inject(AuthService);

  // Stats
  fundacionStats = signal({ enCustodia: 0, esperandoAtencion: 0, emergenciasZona: 0 });
  fundacionAlertas = signal<PanelCaso[]>([]);
  fundacionAnimales = signal<PanelCaso[]>([]);

  cargando = signal(false);
  errorMensaje = signal('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');
    // TODO: Load data from backend (phase 2)
    // For now, show empty state
    this.fundacionStats.set({ enCustodia: 0, esperandoAtencion: 0, emergenciasZona: 0 });
    this.fundacionAlertas.set([]);
    this.fundacionAnimales.set([]);
    this.cargando.set(false);
  }

  getBadgeClass = getPanelBadgeClass;
  getBadgeLabel = getPanelBadgeLabel;
  scrollToPanel = scrollToDashboardPanel;
}
