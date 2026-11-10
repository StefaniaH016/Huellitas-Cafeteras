import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
import {
  fallbackCaseString,
  getUrgencyBadgeClass,
  getUrgencyBadgeLabel,
  normalizeCasoDisplay,
} from '../../core/utils/caso-display.utils';
import { UbicacionSelectorComponent } from '../../shared/components/ubicacion-selector/ubicacion-selector.component';

interface Caso {
  id: string | number;
  titulo?: string;
  descripcion: string;
  urgencia: string;
  tipo: string;
  estado: string;
  municipio: string;
  barrio: string;
  fechaReporte: string;
  foto?: string;
}

interface RespuestaCasos {
  content?: Caso[];
  totalElements?: number;
}

interface RawCasoItem {
  id: string | number;
  titulo?: string;
  descripcion?: string;
  urgencia?: string;
  tipoCaso?: string;
  tipo?: string;
  estado?: string;
  municipio?: string;
  barrio?: string;
  fechaCreacion?: string;
  fechaReporte?: string;
  fotoPrincipal?: string;
  foto?: string;
}

@Component({
  selector: 'app-casos',
  standalone: true,
  imports: [CommonModule, FormsModule, UbicacionSelectorComponent],
  templateUrl: './casos.component.html',
  styleUrls: ['./casos.component.css'],
})
export class CasosComponent implements OnInit {
  private casoService = inject(CasoService);
  private router = inject(Router);

  casos = signal<Caso[]>([]);
  cargando = signal(false);
  error = signal('');
  total = signal(0);
  page = signal(0);
  pageSize = 10;
  mostrarFiltros = signal(false);

  filtroDeptoId = signal<number | null>(null);
  filtroMunId = signal<number | null>(null);
  filtroBarrioId = signal<number | null>(null);
  deptoId = signal<number | null>(null);
  munId = signal<number | null>(null);
  barrioId = signal<number | null>(null);

  urgencias = signal<string[]>(['CRITICA', 'ALTA', 'MEDIA', 'BAJA']);
  tipos = signal<string[]>(['HERIDO', 'VULNERABLE', 'EXTRAVIADO', 'EN_ADOPCION']);
  estados = signal<string[]>(['REGISTRADO', 'EN_ATENCION']);
  urgenciasSeleccionadas = signal([...this.urgencias()]);
  tiposSeleccionados = signal([...this.tipos()]);
  estadosSeleccionados = signal([...this.estados()]);
  fotosRota = signal(new Set<string | number>());

  casosFiltrados = computed(() => {
    const todos = this.casos();
    return this.filterCasos(todos);
  });

  paginaActual = computed(() => this.page() + 1);
  hayProxima = computed(() => this.casosFiltrados().length >= this.pageSize);

  ngOnInit(): void {
    this.loadCasos();
  }

  buildFilterParams(): Record<string, string | number> {
    const params: Record<string, string | number> = {
      page: this.page(),
      size: 50,
    };

    const currentDepto = this.deptoId();
    if (currentDepto !== null) {
      params['departamentoId'] = currentDepto;
    }

    const currentMun = this.munId();
    if (currentMun !== null) {
      params['municipioId'] = currentMun;
    }

    const currentBarrio = this.barrioId();
    if (currentBarrio !== null) {
      params['barrioId'] = currentBarrio;
    }

    // Enviar filtros solo si no está todo seleccionado
    if (this.urgencias().length < 4) {
      params['urgencias'] = this.urgencias().join(',');
    }
    if (this.tipos().length < 4) {
      params['tiposCaso'] = this.tipos().join(',');
    }
    if (this.estados().length < 2) {
      // Si hay menos de 2 estados (no están todos)
      params['estados'] = this.estados().join(',');
    }

    return params;
  }

  private formatCasoFecha(fechaCreacion?: string, fechaReporte?: string): string {
    if (fechaCreacion) {
      return new Date(fechaCreacion).toLocaleDateString('es-CO');
    }
    return fechaReporte || new Date().toLocaleDateString('es-CO');
  }

  private extractTipo(caso: RawCasoItem): string {
    if (caso.tipoCaso) {
      return caso.tipoCaso.toUpperCase();
    }
    if (caso.tipo) {
      return caso.tipo.toUpperCase();
    }
    return 'VULNERABLE';
  }

  private mapRawCaso(caso: RawCasoItem): Caso {
    const display = normalizeCasoDisplay(caso);
    return {
      ...display,
      tipo: this.extractTipo(caso),
      fechaReporte: this.formatCasoFecha(caso.fechaCreacion, caso.fechaReporte),
      foto: fallbackCaseString(caso.fotoPrincipal, fallbackCaseString(caso.foto)),
    };
  }

  private filterCasos(allCasos: Caso[]): Caso[] {
    let filtered = allCasos;

    filtered = filtered.filter((c) => this.urgencias().includes(c.urgencia));
    filtered = filtered.filter((c) => this.tipos().includes(c.tipo));
    filtered = filtered.filter((c) => this.estados().includes(c.estado));

    return filtered;
  }

  loadCasos(): void {
    this.cargando.set(true);
    const filtros = this.buildFilterParams();

    this.casoService.listarCasos(filtros).subscribe({
      next: (response: RespuestaCasos | Caso[]): void => {
        const rawContent = Array.isArray(response) ? response : response.content || [];
        const allCasos = (rawContent as unknown as RawCasoItem[]).map((c) => this.mapRawCaso(c));
        const filtered = this.filterCasos(allCasos);

        this.casos.set(filtered);
        this.total.set(filtered.length);
        this.cargando.set(false);
      },
      error: (err: unknown): void => {
        console.error('Error loading casos:', err);
        this.cargando.set(false);
      },
    });
  }

  toggleUrgencia(urgencia: string): void {
    const current = this.urgenciasSeleccionadas();
    if (current.includes(urgencia)) {
      this.urgenciasSeleccionadas.set(current.filter((u) => u !== urgencia));
    } else {
      this.urgenciasSeleccionadas.set([...current, urgencia]);
    }
  }

  toggleTipo(tipo: string): void {
    const current = this.tiposSeleccionados();
    if (current.includes(tipo)) {
      this.tiposSeleccionados.set(current.filter((t) => t !== tipo));
    } else {
      this.tiposSeleccionados.set([...current, tipo]);
    }
  }

  toggleEstado(estado: string): void {
    const current = this.estadosSeleccionados();
    if (current.includes(estado)) {
      this.estadosSeleccionados.set(current.filter((e) => e !== estado));
    } else {
      this.estadosSeleccionados.set([...current, estado]);
    }
  }

  onAplicarFiltros(): void {
    this.aplicarFiltros();
  }

  onVerCaso(id: string | number): void {
    void this.router.navigate(['/caso-detalle', id]);
  }

  toggleFiltros(): void {
    this.mostrarFiltros.update((v) => !v);
  }

  aplicarFiltros(): void {
    this.urgencias.set([...this.urgenciasSeleccionadas()]);
    this.tipos.set([...this.tiposSeleccionados()]);
    this.estados.set([...this.estadosSeleccionados()]);
    this.deptoId.set(this.filtroDeptoId());
    this.munId.set(this.filtroMunId());
    this.barrioId.set(this.filtroBarrioId());
    this.page.set(0);
    this.loadCasos();
  }

  verCaso(id: string | number): void {
    this.onVerCaso(id);
  }

  paginaAnterior(): void {
    this.prevPage();
  }

  proximaPagina(): void {
    this.nextPage();
  }

  onDeptoChange(deptoId: number | null): void {
    this.filtroDeptoId.set(deptoId);
    this.filtroMunId.set(null);
    this.filtroBarrioId.set(null);
  }

  onMunChange(munId: number | null): void {
    this.filtroMunId.set(munId);
    this.filtroBarrioId.set(null);
  }

  onBarrioChange(barrioId: number | null): void {
    this.filtroBarrioId.set(barrioId);
  }

  onCasePhotoError(id: string | number): void {
    this.fotosRota.update((ids) => new Set(ids).add(id));
  }

  prevPage(): void {
    if (this.page() > 0) {
      this.page.update((p) => p - 1);
      this.loadCasos();
    }
  }

  nextPage(): void {
    if (this.casos().length >= this.pageSize) {
      this.page.update((p) => p + 1);
      this.loadCasos();
    }
  }

  getBadgeClass(urgencia: string): string {
    return getUrgencyBadgeClass(urgencia);
  }

  getBadgeLabel(urgencia: string): string {
    return getUrgencyBadgeLabel(urgencia);
  }
}
