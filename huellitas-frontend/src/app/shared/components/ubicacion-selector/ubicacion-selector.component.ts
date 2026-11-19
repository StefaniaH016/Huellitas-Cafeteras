import {
  Component,
  ChangeDetectorRef,
  Input,
  Output,
  EventEmitter,
  inject,
  OnInit,
  OnChanges,
  SimpleChanges,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import {
  UbicacionService,
  Departamento,
  Municipio,
  Barrio,
} from '../../../core/services/ubicacion.service';

@Component({
  selector: 'app-ubicacion-selector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ubicacion-selector.component.html',
  styleUrls: ['./ubicacion-selector.component.css'],
})
export class UbicacionSelectorComponent implements OnInit, OnChanges {
  private ubicacionService = inject(UbicacionService);
  private changeDetectorRef = inject(ChangeDetectorRef);

  @Input() deptoId: number | null = null;
  @Input() munId: number | null = null;
  @Input() barrioId: number | null = null;
  /** Prefijo de los id de los campos, para usar el selector más de una vez en la misma pantalla. */
  @Input() idPrefix = 'zona';
  @Input() leyenda = 'Zona';
  @Input() stacked = false;

  @Output() deptoChange = new EventEmitter<number | null>();
  @Output() munChange = new EventEmitter<number | null>();
  @Output() barrioChange = new EventEmitter<number | null>();

  selectedDeptoId = signal<number | null>(null);
  selectedMunId = signal<number | null>(null);
  selectedBarrioId = signal<number | null>(null);

  departamentos: Departamento[] = [];
  municipios: Municipio[] = [];
  barrios: Barrio[] = [];

  cargandoDepartamentos = false;
  cargandoMunicipios = false;
  cargandoBarrios = false;
  errorDepartamentos: string | null = null;
  errorMunicipios: string | null = null;
  errorBarrios: string | null = null;

  private readonly solicitudes = { departamentos: 0, municipios: 0, barrios: 0 };

  get loading(): boolean {
    return this.cargandoDepartamentos || this.cargandoMunicipios || this.cargandoBarrios;
  }

  ngOnInit(): void {
    this.loadDepartamentos();
    this.selectedDeptoId.set(this.deptoId);
    this.selectedMunId.set(this.munId);
    this.selectedBarrioId.set(this.barrioId);

    if (this.deptoId) {
      this.loadMunicipios(this.deptoId);
    }

    if (this.munId) {
      this.loadBarrios(this.munId);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['deptoId'] && !changes['deptoId'].firstChange) {
      const deptoId = changes['deptoId'].currentValue as number | null;
      this.selectedDeptoId.set(deptoId);
      this.municipios = [];
      this.barrios = [];
      this.invalidarSolicitud('municipios');
      this.invalidarSolicitud('barrios');
      if (deptoId) {
        this.loadMunicipios(deptoId);
      } else {
        this.selectedMunId.set(null);
        this.selectedBarrioId.set(null);
      }
    }
    if (changes['munId'] && !changes['munId'].firstChange) {
      const munId = changes['munId'].currentValue as number | null;
      this.selectedMunId.set(munId);
      if (munId) {
        this.loadBarrios(munId);
      } else {
        this.barrios = [];
        this.invalidarSolicitud('barrios');
      }
    }
    if (changes['barrioId'] && !changes['barrioId'].firstChange) {
      this.selectedBarrioId.set(changes['barrioId'].currentValue);
    }
  }

  loadDepartamentos(): void {
    this.cargar(this.ubicacionService.getDepartamentos(), 'departamentos', (data) => {
      this.departamentos = data;
    });
  }

  loadMunicipios(deptoId: number): void {
    this.cargar(this.ubicacionService.getMunicipios(deptoId), 'municipios', (data) => {
      this.municipios = data;
    });
  }

  loadBarrios(munId: number): void {
    this.cargar(this.ubicacionService.getBarrios(munId), 'barrios', (data) => {
      this.barrios = data;
    });
  }

  private cargar<T>(
    fuente: Observable<T[]>,
    nivel: 'departamentos' | 'municipios' | 'barrios',
    asignar: (data: T[]) => void,
  ): void {
    const solicitud = ++this.solicitudes[nivel];
    this.setCargando(nivel, true);
    this.setError(nivel, null);
    fuente.subscribe({
      next: (data) => {
        if (solicitud !== this.solicitudes[nivel]) {
          return;
        }
        asignar(data);
        this.setCargando(nivel, false);
        this.changeDetectorRef.markForCheck();
      },
      error: (err: unknown) => {
        if (solicitud !== this.solicitudes[nivel]) {
          return;
        }
        console.error(`Error loading ${nivel}:`, err);
        this.setError(nivel, `Error al cargar ${nivel}`);
        this.setCargando(nivel, false);
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  private invalidarSolicitud(nivel: 'municipios' | 'barrios'): void {
    this.solicitudes[nivel]++;
    this.setCargando(nivel, false);
    this.setError(nivel, null);
  }

  private setCargando(nivel: 'departamentos' | 'municipios' | 'barrios', cargando: boolean): void {
    if (nivel === 'departamentos') {
      this.cargandoDepartamentos = cargando;
    } else if (nivel === 'municipios') {
      this.cargandoMunicipios = cargando;
    } else {
      this.cargandoBarrios = cargando;
    }
  }

  private setError(nivel: 'departamentos' | 'municipios' | 'barrios', error: string | null): void {
    if (nivel === 'departamentos') {
      this.errorDepartamentos = error;
    } else if (nivel === 'municipios') {
      this.errorMunicipios = error;
    } else {
      this.errorBarrios = error;
    }
  }

  onDeptoChange(deptoId: string): void {
    const id = deptoId ? Number(deptoId) : null;
    this.selectedDeptoId.set(id);
    this.deptoChange.emit(id);

    this.municipios = [];
    this.barrios = [];
    this.invalidarSolicitud('municipios');
    this.invalidarSolicitud('barrios');
    this.selectedMunId.set(null);
    this.selectedBarrioId.set(null);
    this.munChange.emit(null);
    this.barrioChange.emit(null);

    if (id !== null) {
      this.loadMunicipios(id);
    }
  }

  onMunChange(munId: string): void {
    const id = munId ? Number(munId) : null;
    this.selectedMunId.set(id);
    this.munChange.emit(id);

    this.barrios = [];
    this.invalidarSolicitud('barrios');
    this.selectedBarrioId.set(null);
    this.barrioChange.emit(null);

    if (id !== null) {
      this.loadBarrios(id);
    }
  }

  onBarrioChange(barrioId: string): void {
    const id = barrioId ? Number(barrioId) : null;
    this.selectedBarrioId.set(id);
    this.barrioChange.emit(id);
  }

  reintentarMunicipios(): void {
    const departamentoId = this.selectedDeptoId();
    if (departamentoId !== null) {
      this.loadMunicipios(departamentoId);
    }
  }

  reintentarBarrios(): void {
    const municipioId = this.selectedMunId();
    if (municipioId !== null) {
      this.loadBarrios(municipioId);
    }
  }
}
