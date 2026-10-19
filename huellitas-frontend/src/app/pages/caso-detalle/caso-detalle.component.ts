import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CasoService } from '../../core/services/caso.service';

interface Caso {
  id: string | number;
  titulo?: string;
  descripcion?: string;
  urgencia: string;
  fechaReporte: string;
  especie?: string;
  tamanio?: string;
  edad?: string;
  color?: string;
  municipio?: string;
  barrio?: string;
  fundacion?: string;
  veterinaria?: string;
}

interface CasoResponse {
  id?: string | number;
  titulo?: string;
  descripcion?: string;
  urgencia?: string;
  fechaReporte?: string;
  especie?: string;
  tamanio?: string;
  edad?: string;
  color?: string;
  municipio?: string;
  barrio?: string;
  fundacion?: string;
  veterinaria?: string;
}

@Component({
  selector: 'app-caso-detalle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './caso-detalle.component.html',
  styleUrls: ['./caso-detalle.component.css'],
})
export class CasoDetalleComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private casoService = inject(CasoService);
  private router = inject(Router);

  caso = signal<Caso | null>(null);
  cargando = signal(true);
  activeTab = signal<'informacion' | 'seguimiento' | 'bitacora'>('informacion');

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.loadDetalle(id);
      }
    });
  }

  private mapResponseToCaso(response: CasoResponse, fallbackId: string | number): Caso {
    return {
      id: response.id ?? fallbackId,
      titulo: response.titulo ?? '',
      descripcion: response.descripcion ?? '',
      urgencia: response.urgencia ?? 'MEDIA',
      fechaReporte: response.fechaReporte ?? new Date().toLocaleDateString('es-CO'),
      especie: response.especie ?? 'Animal',
      tamanio: response.tamanio ?? 'N/A',
      edad: response.edad ?? 'N/A',
      color: response.color ?? 'N/A',
      municipio: response.municipio ?? 'N/A',
      barrio: response.barrio ?? 'N/A',
      fundacion: response.fundacion ?? '',
      veterinaria: response.veterinaria ?? '',
    };
  }

  loadDetalle(id: string | number): void {
    this.cargando.set(true);
    this.casoService.getDetalle(Number(id)).subscribe({
      next: (response: CasoResponse): void => {
        this.caso.set(this.mapResponseToCaso(response, id));
        this.cargando.set(false);
      },
      error: (err: unknown): void => {
        console.error('Error loading case detail:', err);
        this.cargando.set(false);
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/casos']);
  }

  selectTab(tab: 'informacion' | 'seguimiento' | 'bitacora'): void {
    this.activeTab.set(tab);
  }

  getBadgeClass(urgencia?: string): string {
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

  getBadgeLabel(urgencia?: string): string {
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
        return urgencia || 'Media';
    }
  }
}
