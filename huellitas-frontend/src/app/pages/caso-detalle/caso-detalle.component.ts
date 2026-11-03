import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CasoService, CasoDetalle } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import { CasoSeguimientoComponent } from './caso-seguimiento/caso-seguimiento.component';
import { CasoBitacoraComponent } from './caso-bitacora/caso-bitacora.component';

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
  reportante?: string;
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
  reportanteNombre?: string;
  fundacionCustodioNombre?: string;
  veterinariaNombreResponsable?: string;
}

@Component({
  selector: 'app-caso-detalle',
  standalone: true,
  imports: [CommonModule, CasoSeguimientoComponent, CasoBitacoraComponent],
  templateUrl: './caso-detalle.component.html',
  styleUrls: ['./caso-detalle.component.css'],
})
export class CasoDetalleComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private casoService = inject(CasoService);
  private authService = inject(AuthService);
  private router = inject(Router);

  caso = signal<Caso | null>(null);
  cargando = signal(true);
  activeTab = signal<'informacion' | 'seguimiento' | 'bitacora'>('informacion');
  asumiendo = signal(false);

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

  esFundacion = computed(() => this.authService.tieneRol('FUNDACION_REFUGIO'));

  casoDet = computed(() => {
    const c = this.caso();
    if (!c) {
      return null;
    }
    return {
      id: Number(c.id),
      titulo: c.titulo,
      descripcion: c.descripcion,
      urgencia: c.urgencia,
      estado: c.estado || '',
      tipo: c.tipo,
      tipoCaso: c.tipo,
      municipio: c.municipio,
      barrio: c.barrio,
      aportes: [],
      historial: [],
      fotografias: c.fotos,
      foto: c.foto,
      resumenMedico: undefined,
      fundacionCustodioNombre: c.fundacion,
      veterinariaNombreResponsable: c.veterinaria,
      reportanteNombre: c.reportante,
    } as CasoDetalle;
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

  private buildCasoFromResponse(
    response: CasoResponse,
    fallbackId: string | number,
    animalInfo: { especie: string; tamanio: string; edad: string; color: string },
    ubicacion: { mun: string; bar: string },
    fotos: string[],
  ): Caso {
    const desc = response.descripcion ? response.descripcion : '';
    const titulo = desc ? desc : response.titulo ? response.titulo : '';
    const idVal = response.id !== undefined && response.id !== null ? response.id : fallbackId;
    const fotoPrincipal = fotos.length > 0 ? fotos[0] : '';

    return this.createCasoObject(
      idVal,
      titulo,
      desc,
      response.urgencia,
      response.tipoCaso,
      response.estado,
      response.fechaCreacion,
      animalInfo,
      ubicacion,
      fotoPrincipal,
      fotos,
      response.fundacionCustodioNombre,
      response.veterinariaNombreResponsable,
      response.reportanteNombre,
    );
  }

  private createCasoObject(
    id: string | number,
    titulo: string,
    descripcion: string,
    urgencia: string | undefined,
    tipoCaso: string | undefined,
    estado: string | undefined,
    fechaCreacion: string | undefined,
    animalInfo: { especie: string; tamanio: string; edad: string; color: string },
    ubicacion: { mun: string; bar: string },
    fotoPrincipal: string,
    fotos: string[],
    fundacion: string | undefined,
    veterinaria: string | undefined,
    reportante: string | undefined,
  ): Caso {
    return {
      id,
      titulo,
      descripcion,
      urgencia: urgencia ? urgencia : 'MEDIA',
      tipo: tipoCaso,
      estado,
      fechaReporte: this.formatFecha(fechaCreacion),
      especie: animalInfo.especie,
      tamanio: animalInfo.tamanio,
      edad: animalInfo.edad,
      color: animalInfo.color,
      municipio: ubicacion.mun,
      barrio: ubicacion.bar,
      foto: fotoPrincipal,
      fotos,
      fundacion: fundacion ? fundacion : '',
      veterinaria: veterinaria ? veterinaria : '',
      reportante: reportante ? reportante : '',
    };
  }

  private mapResponseToCaso(response: CasoResponse, fallbackId: string | number): Caso {
    const ai = this.extractAnimalInfo(response.animal);
    const u = this.formatUbicacion(response.ubicacion);
    const fotos = this.extractFotos(response);
    return this.buildCasoFromResponse(response, fallbackId, ai, u, fotos);
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
    void this.router.navigate(['/casos']);
  }

  selectTab(tab: 'informacion' | 'seguimiento' | 'bitacora'): void {
    this.activeTab.set(tab);
  }

  asumirCustodia(): void {
    if (!this.caso()?.id) {
      console.error('No case ID available');
      return;
    }

    this.asumiendo.set(true);
    this.casoService.asumirCustodia(Number(this.caso()?.id)).subscribe({
      next: () => {
        this.asumiendo.set(false);
        console.warn('Custodia asumida exitosamente');
        // Reload case details
        if (this.caso()?.id) {
          this.loadDetalle(this.caso()!.id);
        }
      },
      error: (err: unknown) => {
        this.asumiendo.set(false);
        console.error('Error al asumir custodia:', err);
      },
    });
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
