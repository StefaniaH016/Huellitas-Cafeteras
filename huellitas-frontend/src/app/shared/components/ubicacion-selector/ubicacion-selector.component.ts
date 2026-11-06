import {
  Component,
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

  loading = false;
  error: string | null = null;

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
      if (deptoId) {
        this.loadMunicipios(deptoId);
      } else {
        this.municipios = [];
        this.barrios = [];
      }
    }
    if (changes['munId'] && !changes['munId'].firstChange) {
      const munId = changes['munId'].currentValue as number | null;
      this.selectedMunId.set(munId);
      if (munId) {
        this.loadBarrios(munId);
      } else {
        this.barrios = [];
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

  private cargar<T>(fuente: Observable<T[]>, nivel: string, asignar: (data: T[]) => void): void {
    this.loading = true;
    this.error = null;
    fuente.subscribe({
      next: (data) => {
        asignar(data);
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error(`Error loading ${nivel}:`, err);
        this.error = `Error al cargar ${nivel}`;
        this.loading = false;
      },
    });
  }

  onDeptoChange(deptoId: string): void {
    const id = deptoId ? Number(deptoId) : null;
    this.selectedDeptoId.set(id);
    this.deptoChange.emit(id);

    this.municipios = [];
    this.barrios = [];
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
}
