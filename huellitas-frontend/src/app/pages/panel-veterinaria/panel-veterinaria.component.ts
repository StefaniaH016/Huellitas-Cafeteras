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
  selector: 'app-panel-veterinaria',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './panel-veterinaria.component.html',
  styleUrl: './panel-veterinaria.component.css',
})
export class PanelVeterinariaComponent implements OnInit {
  private casoService = inject(CasoService);
  authService = inject(AuthService);

  // Stats
  veterinariaStats = signal({ solicitudes: 0, pacientesActivos: 0 });
  veterinariaSolicitudes = signal<PanelCaso[]>([]);
  veterinariaPacientes = signal<PanelCaso[]>([]);

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
    this.veterinariaStats.set({ solicitudes: 0, pacientesActivos: 0 });
    this.veterinariaSolicitudes.set([]);
    this.veterinariaPacientes.set([]);
    this.cargando.set(false);
  }

  getBadgeClass = getPanelBadgeClass;
  getBadgeLabel = getPanelBadgeLabel;
  scrollToPanel = scrollToDashboardPanel;
}
