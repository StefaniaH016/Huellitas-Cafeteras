import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CasoService } from '../../core/services/caso.service';

interface Caso {
  id: string | number;
  titulo?: string;
  descripcion?: string;
  urgencia: string;
  tipo?: string;
  estado?: string;
  fechaReporte: string;
  especie?: string;
  tamanio?: string;
  edad?: string;
  color?: string;
  municipio?: string;
  barrio?: string;
  foto?: string;
  fotos: string[];
  fundacion?: string;
  veterinaria?: string;
}

interface AnimalResponse {
  id?: number;
  nombre?: string;
  especie?: string;
  raza?: string;
  color?: string;
  sexo?: string;
  edadAproximada?: string;
  senasParticulares?: string;
}

interface UbicacionResponse {
  departamentoId?: number;
  departamento?: string;
  municipioId?: number;
  municipio?: string;
  barrioId?: number;
  barrio?: string;
  direccionReferencia?: string;
}

interface FotoResponse {
  id?: number;
  url?: string;
  principal?: boolean;
}

interface CasoResponse {
  id?: string | number;
  tipoCaso?: string;
  estado?: string;
  urgencia?: string;
  descripcion?: string;
  animal?: AnimalResponse;
  ubicacion?: UbicacionResponse;
  fotografias?: FotoResponse[];
  fechaCreacion?: string;
  titulo?: string;
  especie?: string;
  tamanio?: string;
  edad?: string;
  color?: string;
  municipio?: string;
  barrio?: string;
  fechaReporte?: string;
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

  fotos = signal<string[]>([]);
  selectedFotoIndex = signal<number>(0);
  fotoActual = computed(() => {
    const list = this.fotos();
    const idx = this.selectedFotoIndex();
    if (idx >= 0 && idx < list.length) {
      return list[idx];
    }
    return this.caso()?.foto || '';
  });

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.loadDetalle(id);
      }
    });
  }

  private formatEspecie(especie?: string): string {
    const esp = (especie || '').toUpperCase();
    const map: Record<string, string> = {
      PERRO: 'Perro',
      CANINO: 'Perro',
      GATO: 'Gato',
      FELINO: 'Gato',
      OTRO: 'Otro',
    };
    if (map[esp]) {
      return map[esp];
    }
    if (especie) {
      return especie.charAt(0).toUpperCase() + especie.slice(1).toLowerCase();
    }
    return 'Animal';
  }

  private formatColorYsenas(color?: string, senas?: string): string {
    const c = color?.trim() || '';
    const s = senas?.trim() || '';
    if (c && s) {
      return `${c} · ${s}`;
    }
    return c || s || 'N/A';
  }

  private formatUbicacion(ubicacion?: UbicacionResponse): { mun: string; bar: string } {
    const mun = ubicacion?.municipio || 'N/A';
    const bar = ubicacion?.barrio || 'N/A';
    const dir = ubicacion?.direccionReferencia ? ` (${ubicacion.direccionReferencia})` : '';
    return { mun, bar: `${bar}${dir}` };
  }

  private formatFecha(fechaCreacion?: string): string {
    if (!fechaCreacion) {
      return new Date().toLocaleDateString('es-CO');
    }
    try {
      return new Date(fechaCreacion).toLocaleDateString('es-CO');
    } catch {
      return fechaCreacion;
    }
  }

  private extractFotos(response: CasoResponse): string[] {
    const fotos = response.fotografias ?? [];

    // Ordenar y extraer URLs válidas en un solo flujo declarativo
    const urls = [...fotos]
      .sort((a, b) => Number(!!b.principal) - Number(!!a.principal))
      .map((f) => f?.url?.trim())
      .filter((url): url is string => Boolean(url));

    if (urls.length > 0) {
      return urls;
    }

    // Fallback si no hay fotos en el arreglo
    const respRecord = response as Record<string, unknown>;
    const single = respRecord['fotoPrincipal'] ?? respRecord['foto'];

    return typeof single === 'string' && single.trim() ? [single.trim()] : [];
  }

  private extractAnimalInfo(a?: AnimalResponse): {
    especie: string;
    tamanio: string;
    edad: string;
    color: string;
  } {
    if (!a) {
      return { especie: 'N/A', tamanio: 'Mediano', edad: 'N/A', color: 'N/A' };
    }
    const tamanio = a.raza ? a.raza : 'Mediano';
    const edad = a.edadAproximada ? a.edadAproximada : 'N/A';
    return {
      especie: this.formatEspecie(a.especie),
      tamanio,
      edad,
      color: this.formatColorYsenas(a.color, a.senasParticulares),
    };
  }

  private mapResponseToCaso(response: CasoResponse, fallbackId: string | number): Caso {
    const ai = this.extractAnimalInfo(response.animal);
    const u = this.formatUbicacion(response.ubicacion);
    const desc = response.descripcion ? response.descripcion : '';
    const titulo = desc ? desc : response.titulo ? response.titulo : '';
    const idVal = response.id !== undefined && response.id !== null ? response.id : fallbackId;
    const fotos = this.extractFotos(response);
    const fotoPrincipal = fotos.length > 0 ? fotos[0] : '';

    return {
      id: idVal,
      titulo,
      descripcion: desc,
      urgencia: response.urgencia ? response.urgencia : 'MEDIA',
      tipo: response.tipoCaso,
      estado: response.estado,
      fechaReporte: this.formatFecha(response.fechaCreacion),
      especie: ai.especie,
      tamanio: ai.tamanio,
      edad: ai.edad,
      color: ai.color,
      municipio: u.mun,
      barrio: u.bar,
      foto: fotoPrincipal,
      fotos,
      fundacion: response.fundacion ? response.fundacion : '',
      veterinaria: response.veterinaria ? response.veterinaria : '',
    };
  }

  selectFoto(index: number): void {
    if (index >= 0 && index < this.fotos().length) {
      this.selectedFotoIndex.set(index);
    }
  }

  prevFoto(): void {
    const total = this.fotos().length;
    if (total <= 1) {
      return;
    }
    const current = this.selectedFotoIndex();
    this.selectedFotoIndex.set((current - 1 + total) % total);
  }

  nextFoto(): void {
    const total = this.fotos().length;
    if (total <= 1) {
      return;
    }
    const current = this.selectedFotoIndex();
    this.selectedFotoIndex.set((current + 1) % total);
  }

  onFotoError(): void {
    const current = this.selectedFotoIndex();
    const list = this.fotos();
    if (list.length > 0) {
      const updated = list.filter((_, idx) => idx !== current);
      this.fotos.set(updated);
      this.selectedFotoIndex.set(0);
      const c = this.caso();
      if (c) {
        this.caso.set({ ...c, foto: updated[0] || '', fotos: updated });
      }
    }
  }

  loadDetalle(id: string | number): void {
    this.cargando.set(true);
    this.casoService.getDetalle(Number(id)).subscribe({
      next: (response: CasoResponse): void => {
        const mapped = this.mapResponseToCaso(response, id);
        this.caso.set(mapped);
        this.fotos.set(mapped.fotos);
        this.selectedFotoIndex.set(0);
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

  getBadgeLabel(urgencia?: string): string {
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
}
