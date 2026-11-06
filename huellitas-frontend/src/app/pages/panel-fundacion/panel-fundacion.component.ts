import { Component, signal, inject, OnInit, computed } from '@angular/core';
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
import { pageContent, pageTotal } from '../../core/utils/page.utils';
import { getServerErrorMessage } from '../../core/utils/request.utils';
import { environment } from '../../../environments/environment';
import { forkJoin } from 'rxjs';
import { CasoMiniaturaComponent } from '../../shared/components/caso-miniatura/caso-miniatura.component';

interface UsuarioPerfilDTO {
  id: number;
  nombre: string;
  organizacionNombre?: string | null;
  correo: string;
  telefono?: string;
  rol: string;
  verificado: boolean;
  departamentoId?: number;
  municipioId?: number;
  barrioId?: number;
  estadoVerificacionOrganizacion?: string | null;
  aceptaCustodia?: boolean | null;
  capacidadAlbergue?: number | null;
}

@Component({
  selector: 'app-panel-fundacion',
  imports: [CommonModule, FormsModule, RouterLink, CasoMiniaturaComponent],
  templateUrl: './panel-fundacion.component.html',
  styleUrl: './panel-fundacion.component.css',
})
export class PanelFundacionComponent implements OnInit {
  private casoService = inject(CasoService);
  private httpClient = inject(HttpClient);
  private router = inject(Router);
  authService = inject(AuthService);

  fundacionStats = signal({ enCustodia: 0, casosSinCustodia: 0 });
  fundacionAlertas = signal<PanelCaso[]>([]);
  fundacionAnimales = signal<PanelCaso[]>([]);

  cargando = signal(false);
  errorMensaje = signal('');
  custodiaEnCursoId = signal<number | null>(null);
  liberandoId = signal<number | null>(null);
  estadoVerificacion = signal('PENDIENTE');
  nombreOrganizacion = signal('');
  aceptaCustodia = signal(false);
  capacidadAlbergue = signal(0);
  puedeAsumirCustodia = computed(
    () =>
      this.estadoVerificacion() === 'VERIFICADA' &&
      this.aceptaCustodia() &&
      this.fundacionStats().enCustodia < this.capacidadAlbergue(),
  );
  cuposDisponibles = computed(() =>
    Math.max(0, this.capacidadAlbergue() - this.fundacionStats().enCustodia),
  );
  motivoLiberacion = '';

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    this.fundacionStats.set({ enCustodia: 0, casosSinCustodia: 0 });
    this.fundacionAlertas.set([]);
    this.fundacionAnimales.set([]);

    this.httpClient.get<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me`).subscribe({
      next: (usuario) => {
        this.nombreOrganizacion.set(usuario.organizacionNombre?.trim() || usuario.nombre);
        this.estadoVerificacion.set(usuario.estadoVerificacionOrganizacion ?? 'PENDIENTE');
        this.aceptaCustodia.set(usuario.aceptaCustodia === true);
        this.capacidadAlbergue.set(usuario.capacidadAlbergue ?? 0);
        if (usuario.municipioId) {
          this.cargarCasos(usuario.municipioId);
        } else {
          this.errorMensaje.set('No se pudo determinar tu municipio');
          this.cargando.set(false);
        }
      },
      error: (error: unknown) => {
        console.error('Error al cargar el perfil de la fundación:', error);
        this.errorMensaje.set('Error al cargar tu información');
        this.cargando.set(false);
      },
    });
  }

  private cargarCasos(municipioId: number): void {
    forkJoin({
      enCustodia: this.casoService.listarCasosEnCustodia({ page: 0, size: 50 }),
      sinCustodia: this.casoService.listarCasosSinCustodia(municipioId, { page: 0, size: 50 }),
    }).subscribe({
      next: ({ enCustodia, sinCustodia }) => {
        this.fundacionAnimales.set(pageContent<CasoListItem>(enCustodia));
        this.fundacionAlertas.set(pageContent<CasoListItem>(sinCustodia));
        this.fundacionStats.set({
          enCustodia: pageTotal(enCustodia),
          casosSinCustodia: pageTotal(sinCustodia),
        });
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar los casos de la fundación:', error);
        this.errorMensaje.set('No fue posible cargar los casos de la fundación.');
        this.cargando.set(false);
      },
    });
  }

  asumirCustodia(casoId: number): void {
    if (this.custodiaEnCursoId() !== null) {
      return;
    }
    this.errorMensaje.set('');
    this.custodiaEnCursoId.set(casoId);
    this.casoService.gestionarCustodia(casoId, { accion: 'ASUMIR' }).subscribe({
      next: () => {
        this.custodiaEnCursoId.set(null);
        this.cargarDatos();
      },
      error: (error: unknown) => {
        this.custodiaEnCursoId.set(null);
        this.errorMensaje.set(getServerErrorMessage(error) ?? 'Error al asumir custodia');
      },
    });
  }

  iniciarLiberacion(casoId: number): void {
    this.liberandoId.set(casoId);
    this.motivoLiberacion = '';
  }

  cancelarLiberacion(): void {
    this.liberandoId.set(null);
    this.motivoLiberacion = '';
  }

  liberarCustodia(casoId: number): void {
    const motivo = this.motivoLiberacion.trim();
    if (!motivo || this.custodiaEnCursoId() !== null) {
      return;
    }
    this.errorMensaje.set('');
    this.custodiaEnCursoId.set(casoId);
    this.casoService.gestionarCustodia(casoId, { accion: 'LIBERAR', motivo }).subscribe({
      next: () => {
        this.custodiaEnCursoId.set(null);
        this.cancelarLiberacion();
        this.cargarDatos();
      },
      error: (error: unknown) => {
        this.custodiaEnCursoId.set(null);
        this.errorMensaje.set(getServerErrorMessage(error) ?? 'Error al liberar custodia');
      },
    });
  }

  irAlCaso(casoId: number): void {
    void this.router.navigate(['/caso-detalle', casoId]);
  }

  abrirBitacora(casoId: number): void {
    void this.router.navigate(['/caso-detalle', casoId], {
      queryParams: { tab: 'bitacora' },
    });
  }

  getBadgeClass = getPanelBadgeClass;
  getBadgeLabel = getPanelBadgeLabel;
  scrollToPanel = scrollToDashboardPanel;
}
