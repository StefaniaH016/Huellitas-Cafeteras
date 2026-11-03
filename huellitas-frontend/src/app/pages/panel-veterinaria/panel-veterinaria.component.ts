import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CasoService, CasoListItem } from '../../core/services/caso.service';
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
  private router = inject(Router);
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

    // Cargar casos activos en atención
    this.casoService.listarCasos({ estado: 'EN_ATENCION' }).subscribe({
      next: (response) => {
        const casos: CasoListItem[] = Array.isArray(response.content)
          ? response.content
          : Array.isArray(response)
            ? response
            : [];

        // Los pacientes activos son casos en atención
        this.veterinariaPacientes.set(casos);
        this.veterinariaStats.update((stats) => ({
          ...stats,
          pacientesActivos: casos.length,
        }));
        this.cargando.set(false);
      },
      error: () => {
        this.errorMensaje.set('Error al cargar pacientes activos');
        this.cargando.set(false);
        this.limpiarErrorDespuesDe3Segundos();
      },
    });
  }

  abrirBitacora(casoId: number): void {
    void this.router.navigate(['/caso-detalle', casoId], {
      queryParams: { tab: 'bitacora' },
    });
  }

  irAlCaso(casoId: number): void {
    void this.router.navigate(['/caso-detalle', casoId]);
  }

  private limpiarErrorDespuesDe3Segundos(): void {
    setTimeout(() => {
      this.errorMensaje.set('');
    }, 3000);
  }

  getBadgeClass = getPanelBadgeClass;
  getBadgeLabel = getPanelBadgeLabel;
  scrollToPanel = scrollToDashboardPanel;
}
