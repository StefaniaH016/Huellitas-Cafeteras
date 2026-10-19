import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
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
}

interface RespuestaCasos {
  content?: Caso[];
  totalElements?: number;
}

interface FiltrosCasos {
  page: number;
  size: number;
  urgencias?: string;
  tipos?: string;
  estados?: string;
  deptoId?: number;
  munId?: number;
  barrioId?: number;
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

  urgencias = signal<string[]>(['CRITICA', 'ALTA']);
  tipos = signal<string[]>(['HERIDO', 'VULNERABLE']);
  estados = signal<string[]>(['REGISTRADO', 'EN_ATENCION']);
  deptoId = signal<number | null>(null);
  munId = signal<number | null>(null);
  barrioId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadCasos();
  }

  loadCasos(): void {
    this.cargando.set(true);
    const filtros = this.buildFilterParams();

    this.casoService.listarCasos(filtros).subscribe({
      next: (response: RespuestaCasos | Caso[]): void => {
        const content = Array.isArray(response) ? response : response.content || [];
        const totalElements = Array.isArray(response)
          ? response.length
          : response.totalElements || content.length;

        const casosData = content.map((caso: Caso): Caso => ({
          id: caso.id,
          titulo: caso.titulo || '',
          descripcion: caso.descripcion || '',
          urgencia: caso.urgencia || 'MEDIA',
          tipo: caso.tipo || 'HERIDO',
          estado: caso.estado || 'REGISTRADO',
          municipio: caso.municipio || '',
          barrio: caso.barrio || '',
          fechaReporte: caso.fechaReporte || new Date().toLocaleDateString('es-CO'),
        }));

        this.casos.set(casosData);
        this.total.set(totalElements);
        this.cargando.set(false);
      },
      error: (err: unknown): void => {
        console.error('Error loading casos:', err);
        this.cargando.set(false);
      },
    });
  }

  buildFilterParams(): FiltrosCasos {
    const params: FiltrosCasos = {
      page: this.page(),
      size: this.pageSize,
    };

    if (this.urgencias().length > 0) {
      params.urgencias = this.urgencias().join(',');
    }
    if (this.tipos().length > 0) {
      params.tipos = this.tipos().join(',');
    }
    if (this.estados().length > 0) {
      params.estados = this.estados().join(',');
    }

    const currentDepto = this.deptoId();
    if (currentDepto !== null) {
      params.deptoId = currentDepto;
    }

    const currentMun = this.munId();
    if (currentMun !== null) {
      params.munId = currentMun;
    }

    const currentBarrio = this.barrioId();
    if (currentBarrio !== null) {
      params.barrioId = currentBarrio;
    }

    return params;
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
    this.router.navigate(['/caso-detalle', id]);
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
    switch (urgencia) {
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
    switch (urgencia) {
      case 'CRITICA':
        return 'Emergencia';
      case 'ALTA':
        return 'Alta';
      case 'MEDIA':
        return 'Media';
      case 'BAJA':
        return 'Baja';
      default:
        return urgencia;
    }
  }
}
