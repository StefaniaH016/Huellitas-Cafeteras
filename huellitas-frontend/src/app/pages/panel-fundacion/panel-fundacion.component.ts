import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CasoService, CasoListItem } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import {
  getPanelBadgeClass,
  getPanelBadgeLabel,
  PanelCaso,
  scrollToDashboardPanel,
} from '../../core/utils/panel.utils';
import { CustodiaDTO } from '../../../core/models/dtos';
import { environment } from '../../../environments/environment';
import {
  PanelTrabajoComponent,
  PanelConfig,
  ListaPanel,
} from '../../shared/components/panel-trabajo/panel-trabajo.component';
import { Figura } from '../../shared/components/dash-figures/dash-figures.component';

interface UsuarioPerfilDTO {
  id: number;
  nombre: string;
  correo: string;
  telefono?: string;
  rol: string;
  verificado: boolean;
  departamentoId?: number;
  municipioId?: number;
  barrioId?: number;
}

@Component({
  selector: 'app-panel-fundacion',
  imports: [CommonModule, FormsModule, RouterLink, PanelTrabajoComponent],
  templateUrl: './panel-fundacion.component.html',
  styleUrl: './panel-fundacion.component.css',
})
export class PanelFundacionComponent implements OnInit {
  private casoService = inject(CasoService);
  private httpClient = inject(HttpClient);
  private router = inject(Router);
  authService = inject(AuthService);

  // Panel config
  panelConfig = computed<PanelConfig>(() => {
    const nombre = this.authService.getNombre();
    return {
      titulo: `Mi panel · ${nombre}`,
      resumen:
        'Hay emergencias en tu municipio. Coordina rescates y gestiona los animales en custodia.',
      cta: { texto: 'Ver casos de mi zona', ruta: '/casos' },
      figuras: this.buildFiguras.bind(this),
      listas: this.buildListas.bind(this),
    };
  });

  // Stats for legacy template
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

    // Reset UI state
    this.fundacionStats.set({ enCustodia: 0, esperandoAtencion: 0, emergenciasZona: 0 });
    this.fundacionAlertas.set([]);
    this.fundacionAnimales.set([]);

    // Obtener municipioId del usuario autenticado
    this.httpClient.get<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me`).subscribe({
      next: (usuario) => {
        if (usuario.municipioId) {
          this.cargarCasos(usuario.municipioId);
        } else {
          this.errorMensaje.set('No se pudo determinar tu municipio');
          this.cargando.set(false);
          this.limpiarErrorDespuesDe3Segundos();
        }
      },
      error: () => {
        this.errorMensaje.set('Error al cargar tu información');
        this.cargando.set(false);
        this.limpiarErrorDespuesDe3Segundos();
      },
    });
  }

  private cargarCasos(municipioId: number): void {
    // Cargar casos en custodia
    this.casoService.listarCasosEnCustodia().subscribe({
      next: (response) => {
        const casos = Array.isArray(response.content)
          ? response.content
          : Array.isArray(response)
            ? response
            : [];
        this.fundacionAnimales.set(casos);
        this.fundacionStats.update((stats) => ({
          ...stats,
          enCustodia: casos.length,
        }));
      },
      error: () => {
        this.errorMensaje.set('Error al cargar casos en custodia');
        this.limpiarErrorDespuesDe3Segundos();
      },
    });

    // Cargar casos sin custodia
    this.casoService.listarCasosSinCustodia(municipioId).subscribe({
      next: (response) => {
        const casos = Array.isArray(response.content)
          ? response.content
          : Array.isArray(response)
            ? response
            : [];
        this.fundacionAlertas.set(casos);
        this.fundacionStats.update((stats) => ({
          ...stats,
          emergenciasZona: casos.length,
        }));
        this.cargando.set(false);
      },
      error: () => {
        this.errorMensaje.set('Error al cargar casos sin custodia');
        this.cargando.set(false);
        this.limpiarErrorDespuesDe3Segundos();
      },
    });
  }

  private buildFiguras(casos: CasoListItem[]): Figura[] {
    // Count critical injured cases in the zone
    const criticosHeridos = casos.filter(
      (c) => c.tipoCaso === 'HERIDO' && c.estado === 'REGISTRADO' && c.urgencia === 'CRITICA',
    ).length;

    // Count cases waiting for attention
    const esperandoAtencion = casos.filter((c) => c.estado === 'EN_ATENCION').length;

    return [
      { valor: '—', texto: 'Resumen' },
      { valor: criticosHeridos, texto: 'Animales críticos' },
      { valor: esperandoAtencion, texto: 'En atención' },
    ];
  }

  private buildListas(casos: CasoListItem[]): ListaPanel[] {
    // Critical injured cases
    const criticosHeridos = casos.filter(
      (c) => c.tipoCaso === 'HERIDO' && c.estado === 'REGISTRADO' && c.urgencia === 'CRITICA',
    );

    // Cases awaiting attention
    const enAtencion = casos.filter((c) => c.estado === 'EN_ATENCION');

    return [
      {
        titulo: 'Emergencias sin custodia',
        casos: criticosHeridos,
        vacio: 'No hay emergencias críticas',
        accion: 'Asumir custodia',
      },
      {
        titulo: 'Esperando atención',
        casos: enAtencion,
        vacio: 'Cargar desde el backend',
        accion: 'Ver detalles',
        pendiente: 'El backend no publica este endpoint aún',
      },
    ];
  }

  asumirCustodia(casoId: number): void {
    this.casoService.gestionarCustodia(casoId, { asumir: true } as CustodiaDTO).subscribe({
      next: () => {
        this.cargarDatos();
      },
      error: () => {
        this.errorMensaje.set('Error al asumir custodia');
        this.limpiarErrorDespuesDe3Segundos();
      },
    });
  }

  liberarCustodia(casoId: number): void {
    this.casoService.gestionarCustodia(casoId, { asumir: false } as CustodiaDTO).subscribe({
      next: () => {
        this.cargarDatos();
      },
      error: () => {
        this.errorMensaje.set('Error al liberar custodia');
        this.limpiarErrorDespuesDe3Segundos();
      },
    });
  }

  irAlCaso(casoId: number): void {
    void this.router.navigate([`/casos/detalle/${casoId}`]);
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
