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
  page?: number;
  size?: number;
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

  // Feed data (for Ciudadano)
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

    // Load location and feed data for ciudadano, fundacion, and veterinaria
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

  // Ubicación methods
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
    const filtros: FiltrosCaso = {};
    if (this.filtros.departamentoId) {
      filtros.departamentoId = this.filtros.departamentoId;
    }
    if (this.filtros.municipioId) {
      filtros.municipioId = this.filtros.municipioId;
    }
    if (this.filtros.barrioId) {
      filtros.barrioId = this.filtros.barrioId;
    }
    if (this.filtros.urgencia) {
      filtros.urgencia = this.filtros.urgencia;
    }
    if (this.filtros.tipoAnimal) {
      filtros.tipoAnimal = this.filtros.tipoAnimal;
    }
    filtros.page = 0;
    filtros.size = 10;
    return filtros;
  }

  private extractCasosArray(response: unknown): Caso[] {
    if (!response || typeof response !== 'object') {
      return [];
    }

    const obj = response as Record<string, unknown>;
    const casosArray: unknown = obj['casos'] || obj['content'] || response;

    // Validate that we have an array
    if (!Array.isArray(casosArray)) {
      return [];
    }

    return casosArray as Caso[];
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
    const urgenciaLower = (urgencia || '').toLowerCase();
    if (urgenciaLower === 'critica' || urgenciaLower === 'emergencia') {
      return 'badge-emergencia';
    }
    if (urgenciaLower === 'alta' || urgenciaLower === 'extraviado') {
      return 'badge-alta';
    }
    return 'badge-atencion';
  }

  getBadgeLabel(urgencia: string): string {
    const urgenciaLower = (urgencia || '').toLowerCase();
    if (urgenciaLower === 'critica' || urgenciaLower === 'emergencia') {
      return 'Emergencia';
    }
    if (urgenciaLower === 'alta' || urgenciaLower === 'extraviado') {
      return 'Extraviado';
    }
    return 'En atención';
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
