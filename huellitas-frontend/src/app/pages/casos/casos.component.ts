import { Component, OnInit, inject, signal } from '@angular/core';
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
  total = signal(0);
  page = signal(0);
  pageSize = 10;

  urgencias = signal<string[]>(['CRITICA', 'ALTA', 'MEDIA', 'BAJA']);
  tipos = signal<string[]>(['HERIDO', 'VULNERABLE', 'EXTRAVIADO']);
  estados = signal<string[]>(['REGISTRADO', 'EN_ATENCION']);
  deptoId = signal<number | null>(null);
  munId = signal<number | null>(null);
  barrioId = signal<number | null>(null);

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

    if (this.urgencias().length === 1) {
      params['urgencia'] = this.urgencias()[0];
    }
    if (this.tipos().length === 1) {
      params['tipoCaso'] = this.tipos()[0];
    }
    if (this.estados().length === 1) {
      params['estado'] = this.estados()[0];
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

    if (this.urgencias().length > 0) {
      filtered = filtered.filter((c) => this.urgencias().includes(c.urgencia));
    }

    if (this.tipos().length > 0) {
      filtered = filtered.filter((c) => this.tipos().includes(c.tipo));
    }

    if (this.estados().length > 0) {
      filtered = filtered.filter((c) => this.estados().includes(c.estado));
    }

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
    const current = this.urgencias();
    if (current.includes(urgencia)) {
      this.urgencias.set(current.filter((u) => u !== urgencia));
    } else {
      this.urgencias.set([...current, urgencia]);
    }
  }

  toggleTipo(tipo: string): void {
    const current = this.tipos();
    if (current.includes(tipo)) {
      this.tipos.set(current.filter((t) => t !== tipo));
    } else {
      this.tipos.set([...current, tipo]);
    }
  }

  toggleEstado(estado: string): void {
    const current = this.estados();
    if (current.includes(estado)) {
      this.estados.set(current.filter((e) => e !== estado));
    } else {
      this.estados.set([...current, estado]);
    }
  }

  onAplicarFiltros(): void {
    this.page.set(0);
    this.loadCasos();
  }

  onVerCaso(id: string | number): void {
    void this.router.navigate(['/caso-detalle', id]);
  }

  onDeptoChange(deptoId: number | null): void {
    this.deptoId.set(deptoId);
  }

  onMunChange(munId: number | null): void {
    this.munId.set(munId);
  }

  onBarrioChange(barrioId: number | null): void {
    this.barrioId.set(barrioId);
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
