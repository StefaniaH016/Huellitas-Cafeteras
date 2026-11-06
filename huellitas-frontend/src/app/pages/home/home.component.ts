import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { EMPTY, expand, forkJoin, reduce } from 'rxjs';
import {
  ESTADO_LABELS,
  TIPO_LABELS,
  fallbackCaseString,
  getUrgencyBadgeClass,
  getUrgencyBadgeLabel,
  labelOf,
  normalizeCasoDisplay,
} from '../../core/utils/caso-display.utils';
import { normalizeRole } from '../../core/utils/role.utils';
import {
  VerificacionPendiente,
  VerificacionService,
} from '../../core/services/verificacion.service';
import { CasoReportado, ModeracionService } from '../../core/services/moderacion.service';

interface Ubicacion {
  id: number;
  nombre: string;
}

interface Caso {
  id: number;
  titulo?: string;
  descripcion: string;
  urgencia: string;
  tipoCaso?: string;
  estado?: string;
  municipio: string;
  barrio: string;
  foto?: string;
  fundacion?: string;
}

interface FiltrosCaso {
  departamentoId?: string | number;
  municipioId?: string | number;
  barrioId?: string | number;
  urgencia?: string;
  tipoAnimal?: string;
  especie?: string;
  page?: number;
  size?: number;
}

interface RawHomeCaso {
  id: number;
  titulo?: string;
  descripcion?: string;
  urgencia?: string;
  tipoCaso?: string;
  tipo?: string;
  estado?: string;
  municipio?: string;
  barrio?: string;
  fotoPrincipal?: string;
  foto?: string;
  fotografias?: { url?: string }[];
  fundacion?: string;
  fundacionCustodio?: string;
}

@Component({
  selector: 'app-home',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private casoService = inject(CasoService);
  authService = inject(AuthService);
  private ubicacionService = inject(UbicacionService);
  private notificacionService = inject(NotificacionService);
  private verificacionService = inject(VerificacionService);
  private moderacionService = inject(ModeracionService);

  // Ubicación signals
  departamentos = signal<Ubicacion[]>([]);
  municipios = signal<Ubicacion[]>([]);
  barrios = signal<Ubicacion[]>([]);

  // Filtros para búsqueda
  filtros: FiltrosCaso = {
    departamentoId: '',
    municipioId: '',
    barrioId: '',
    urgencia: '',
    tipoAnimal: '',
  };

  // Feed data
  casos = signal<Caso[]>([]);
  cargando = signal(false);
  errorMensaje = signal('');

  // Computed for role checks
  rol = signal<string | null>(null);

  isCiudadano = computed(() => {
    const r = this.rol();
    return !r || r === 'CIUDADANO_VOLUNTARIO';
  });

  isFundacion = computed(() => this.rol() === 'FUNDACION_REFUGIO');
  isVeterinaria = computed(() => this.rol() === 'VETERINARIA');
  isAdmin = computed(() => this.rol() === 'ADMINISTRADOR');
  resumenAdmin = signal({ cuentasPendientes: 0, casosReportados: 0 });
  cuentasPendientesAdmin = signal<VerificacionPendiente[]>([]);
  casosReportadosAdmin = signal<CasoReportado[]>([]);
  cargandoResumenAdmin = signal(false);
  errorResumenAdmin = signal('');
  totalCasosFeed = signal(0);

  // Mis reportes (Ciudadano)
  misReportes = signal<Caso[]>([]);
  cargandoMisReportes = signal(false);
  errorMisReportes = signal('');
  readonly notificacionesSinLeer = this.notificacionService.sinLeer;

  misReportesActivos = computed(() =>
    this.misReportes().filter((c) => c.estado === 'REGISTRADO' || c.estado === 'EN_ATENCION'),
  );

  misReportesResueltos = computed(() => this.misReportes().filter((c) => c.estado === 'RESUELTO'));

  // Primer caso activo (Ciudadano)
  primerCasoActivo = computed(() => this.misReportesActivos()[0] || null);

  ngOnInit(): void {
    const rawRole = this.authService.getRol();
    const normalized = normalizeRole(rawRole);
    this.rol.set(normalized);

    const role = this.rol();
    if (
      !role ||
      role === 'CIUDADANO_VOLUNTARIO' ||
      role === 'FUNDACION_REFUGIO' ||
      role === 'VETERINARIA' ||
      role === 'ADMINISTRADOR'
    ) {
      this.cargarDepartamentos();
      this.cargarFeed();
    }

    if (this.isCiudadano()) {
      this.cargarMisReportes();
    }
    if (this.isAdmin()) {
      this.cargarResumenAdmin();
    }
  }

  cargarResumenAdmin(): void {
    this.cargandoResumenAdmin.set(true);
    this.errorResumenAdmin.set('');
    forkJoin({
      cuentas: this.verificacionService.getPendientes(),
      casos: this.moderacionService.getCasosReportados(),
    }).subscribe({
      next: ({ cuentas, casos }) => {
        this.resumenAdmin.set({
          cuentasPendientes: cuentas.length,
          casosReportados: casos.length,
        });
        this.cuentasPendientesAdmin.set(cuentas.slice(0, 3));
        this.casosReportadosAdmin.set(casos.slice(0, 3));
        this.cargandoResumenAdmin.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar el resumen administrativo:', error);
        this.errorResumenAdmin.set('No fue posible cargar el resumen. Intenta nuevamente.');
        this.cargandoResumenAdmin.set(false);
      },
    });
  }

  cargarMisReportes(): void {
    this.cargandoMisReportes.set(true);
    this.errorMisReportes.set('');
    this.casoService
      .getMisReportes(0, 50)
      .pipe(
        expand((response: unknown, index: number) => {
          if (!response || typeof response !== 'object' || Array.isArray(response)) {
            return EMPTY;
          }
          const page = response as Record<string, unknown>;
          const currentPage = Number(page['number'] ?? index);
          const totalPages = Number(page['totalPages'] ?? 1);
          return currentPage + 1 < totalPages
            ? this.casoService.getMisReportes(currentPage + 1, 50)
            : EMPTY;
        }),
        reduce(
          (all: Caso[], response: unknown) => [...all, ...this.extractCasosArray(response)],
          [],
        ),
      )
      .subscribe({
        next: (reports) => {
          this.misReportes.set(reports);
          this.cargandoMisReportes.set(false);
        },
        error: (err: unknown) => {
          console.error('Error cargando mis reportes:', err);
          this.errorMisReportes.set('No fue posible cargar tus reportes. Intenta nuevamente.');
          this.misReportes.set([]);
          this.cargandoMisReportes.set(false);
        },
      });
  }

  cargarDepartamentos(): void {
    this.ubicacionService.getDepartamentos().subscribe({
      next: (deps: Ubicacion[]) => {
        this.departamentos.set(deps || []);
      },
      error: (err: unknown) => {
        console.error('Error cargando departamentos:', err);
      },
    });
  }

  onDepartamentoChange(): void {
    if (!this.filtros.departamentoId) {
      this.municipios.set([]);
      this.barrios.set([]);
      this.filtros.municipioId = '';
      this.filtros.barrioId = '';
      return;
    }
    this.ubicacionService.getMunicipios(Number(this.filtros.departamentoId)).subscribe({
      next: (muns: Ubicacion[]) => {
        this.municipios.set(muns || []);
        this.barrios.set([]);
        this.filtros.municipioId = '';
        this.filtros.barrioId = '';
      },
      error: (err: unknown) => {
        console.error('Error cargando municipios:', err);
      },
    });
  }

  onMunicipioChange(): void {
    if (!this.filtros.municipioId) {
      this.barrios.set([]);
      return;
    }
    this.ubicacionService.getBarrios(Number(this.filtros.municipioId)).subscribe({
      next: (bars: Ubicacion[]) => {
        this.barrios.set(bars || []);
        this.filtros.barrioId = '';
      },
      error: (err: unknown) => {
        console.error('Error cargando barrios:', err);
      },
    });
  }

  onSearch(): void {
    this.cargarFeed();
  }

  private buildFilterParams(): FiltrosCaso {
    const p: FiltrosCaso = {};
    if (this.filtros.departamentoId) {
      p.departamentoId = this.filtros.departamentoId;
    }
    if (this.filtros.municipioId) {
      p.municipioId = this.filtros.municipioId;
    }
    if (this.filtros.barrioId) {
      p.barrioId = this.filtros.barrioId;
    }
    if (this.filtros.urgencia) {
      p.urgencia = this.filtros.urgencia;
    }
    if (this.filtros.tipoAnimal) {
      p.especie = this.filtros.tipoAnimal;
    }
    p.page = 0;
    p.size = 10;
    return p;
  }

  private extractHomeFoto(c: RawHomeCaso): string {
    return fallbackCaseString(c.fotoPrincipal, fallbackCaseString(c.foto, c.fotografias?.[0]?.url));
  }

  private mapRawHomeCaso(c: RawHomeCaso): Caso {
    const display = normalizeCasoDisplay(c);
    return {
      ...display,
      id: c.id,
      tipoCaso: fallbackCaseString(c.tipoCaso, fallbackCaseString(c.tipo)).toUpperCase(),
      foto: this.extractHomeFoto(c),
      fundacion: fallbackCaseString(c.fundacion, fallbackCaseString(c.fundacionCustodio)),
    };
  }

  private extractCasosArray(response: unknown): Caso[] {
    if (!response || typeof response !== 'object') {
      return [];
    }

    const obj = response as Record<string, unknown>;
    const rawCases = obj['casos'] ?? obj['content'];
    const casosArray = Array.isArray(rawCases) ? (rawCases as RawHomeCaso[]) : [];
    return casosArray.map((c) => this.mapRawHomeCaso(c));
  }

  cargarFeed(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    const filtros = this.buildFilterParams();
    this.casoService.listarCasos(filtros).subscribe({
      next: (response: unknown) => {
        const casosArray = this.extractCasosArray(response);
        this.casos.set(casosArray);
        const total =
          response && typeof response === 'object' && !Array.isArray(response)
            ? Number((response as Record<string, unknown>)['totalElements'])
            : Number.NaN;
        this.totalCasosFeed.set(Number.isFinite(total) ? total : casosArray.length);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar feed de casos:', error);
        this.errorMensaje.set('Error al cargar los casos. Intente más tarde.');
        this.casos.set([]);
        this.totalCasosFeed.set(0);
        this.cargando.set(false);
      },
    });
  }

  getBadgeClass(urgencia: string): string {
    return getUrgencyBadgeClass(urgencia);
  }

  getBadgeLabel(urgencia: string): string {
    return getUrgencyBadgeLabel(urgencia);
  }

  getEstadoLabel(estado?: string): string {
    return labelOf(ESTADO_LABELS, estado || 'REGISTRADO');
  }

  getTipoLabel(tipo?: string): string {
    return labelOf(TIPO_LABELS, tipo || '');
  }

  getPanelLink(): string {
    const normalizedRole = this.rol();
    if (normalizedRole === 'FUNDACION_REFUGIO') {
      return '/fundacion';
    }
    if (normalizedRole === 'VETERINARIA') {
      return '/veterinaria';
    }
    if (normalizedRole === 'ADMINISTRADOR') {
      return '/admin';
    }
    return '/';
  }
}
