import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import {
  fallbackCaseString,
  getUrgencyBadgeClass,
  getUrgencyBadgeLabel,
  normalizeCasoDisplay,
} from '../../core/utils/caso-display.utils';
import { normalizeRole } from '../../core/utils/role.utils';

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

  casosEnCustodia = computed(() => {
    const nombre = this.authService.getNombre();
    if (!nombre) {
      return [];
    }
    return this.casos().filter((c) => c.estado === 'EN_ATENCION' && c.fundacion === nombre);
  });

  emergenciasZona = computed(() =>
    this.casos().filter((c) => c.urgencia === 'CRITICA' || c.urgencia === 'ALTA'),
  );

  esperandoAtencionMedica = computed(() =>
    this.casos().filter(
      (c) => c.estado === 'EN_ATENCION' && (c.tipoCaso === 'HERIDO' || c.urgencia === 'CRITICA'),
    ),
  );

  // Mis reportes (Ciudadano)
  misReportes = signal<Caso[]>([]);
  cargandoMisReportes = signal(false);

  misReportesActivos = computed(() => this.misReportes().filter((c) => c.estado !== 'RESUELTO'));

  misReportesResueltos = computed(() => this.misReportes().filter((c) => c.estado === 'RESUELTO'));

  ngOnInit(): void {
    const rawRole = this.authService.getRol();
    const normalized = normalizeRole(rawRole);
    this.rol.set(normalized);

    const role = this.rol();
    if (
      !role ||
      role === 'CIUDADANO_VOLUNTARIO' ||
      role === 'FUNDACION_REFUGIO' ||
      role === 'VETERINARIA'
    ) {
      this.cargarDepartamentos();
      this.cargarFeed();
    }

    if (this.isCiudadano()) {
      this.cargarMisReportes();
    }
  }

  cargarMisReportes(): void {
    this.cargandoMisReportes.set(true);
    this.casoService.getMisReportes(0).subscribe({
      next: (resp: unknown): void => {
        const list = this.extractCasosArray(resp);
        this.misReportes.set(list);
        this.cargandoMisReportes.set(false);
      },
      error: (err: unknown): void => {
        console.error('Error cargando mis reportes:', err);
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
    const casosArray: unknown = obj['casos'] || obj['content'] || response;

    if (!Array.isArray(casosArray)) {
      return [];
    }

    return (casosArray as RawHomeCaso[]).map((c) => this.mapRawHomeCaso(c));
  }

  cargarFeed(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    const filtros = this.buildFilterParams();
    this.casoService.listarCasos(filtros).subscribe({
      next: (response: unknown) => {
        const casosArray = this.extractCasosArray(response);
        this.casos.set(casosArray);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar feed de casos:', error);
        this.errorMensaje.set('Error al cargar los casos. Intente más tarde.');
        this.casos.set([]);
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
    switch (estado?.toUpperCase()) {
      case 'REGISTRADO':
        return 'Registrado';
      case 'EN_ATENCION':
        return 'En atención';
      case 'RESUELTO':
        return 'Resuelto';
      case 'ARCHIVADO':
        return 'Archivado';
      default:
        return estado || 'Registrado';
    }
  }

  getTipoLabel(tipo?: string): string {
    switch (tipo?.toUpperCase()) {
      case 'EXTRAVIADO':
        return 'Extraviado';
      case 'VULNERABLE':
        return 'Vulnerable';
      case 'HERIDO':
        return 'Herido';
      case 'EN_ADOPCION':
        return 'En adopción';
      default:
        return tipo || '';
    }
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
