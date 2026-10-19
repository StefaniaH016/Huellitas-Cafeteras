import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
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

  private fallbackStr(val?: string, defaultVal = ''): string {
    if (val) {
      return val;
    }
    return defaultVal;
  }

  private extractHomeFoto(c: RawHomeCaso): string {
    if (c.fotoPrincipal) {
      return c.fotoPrincipal;
    }
    if (c.foto) {
      return c.foto;
    }
    if (c.fotografias && c.fotografias.length > 0) {
      return c.fotografias[0]?.url || '';
    }
    return '';
  }

  private mapRawHomeCaso(c: RawHomeCaso): Caso {
    const urg = c.urgencia ? c.urgencia.toUpperCase() : 'MEDIA';
    const tip = this.fallbackStr(c.tipoCaso, this.fallbackStr(c.tipo));
    const est = this.fallbackStr(c.estado, 'REGISTRADO').toUpperCase();
    const titulo = this.fallbackStr(c.titulo, this.fallbackStr(c.descripcion));

    return {
      id: c.id,
      titulo,
      descripcion: this.fallbackStr(c.descripcion),
      urgencia: urg,
      tipoCaso: tip.toUpperCase(),
      estado: est,
      municipio: this.fallbackStr(c.municipio),
      barrio: this.fallbackStr(c.barrio),
      foto: this.extractHomeFoto(c),
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
    switch (urgencia?.toUpperCase()) {
      case 'CRITICA':
        return 'badge-emergencia';
      case 'ALTA':
        return 'badge-alta';
      case 'MEDIA':
        return 'badge-atencion';
      case 'BAJA':
        return 'badge-media';
      default:
        return 'badge-atencion';
    }
  }

  getBadgeLabel(urgencia: string): string {
    switch (urgencia?.toUpperCase()) {
      case 'CRITICA':
        return 'Emergencia';
      case 'ALTA':
        return 'Alta';
      case 'MEDIA':
        return 'Media';
      case 'BAJA':
        return 'Baja';
      default:
        return urgencia || 'Media';
    }
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
