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
    this.loading = true;
    this.error = null;
    this.ubicacionService.getDepartamentos().subscribe({
      next: (data) => {
        this.departamentos = data;
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Error loading departamentos:', err);
        this.error = 'Error al cargar departamentos';
        this.loading = false;
      },
    });
  }

  loadMunicipios(deptoId: number): void {
    this.loading = true;
    this.error = null;
    this.ubicacionService.getMunicipios(deptoId).subscribe({
      next: (data) => {
        this.municipios = data;
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Error loading municipios:', err);
        this.error = 'Error al cargar municipios';
        this.loading = false;
      },
    });
  }

  loadBarrios(munId: number): void {
    this.loading = true;
    this.error = null;
    this.ubicacionService.getBarrios(munId).subscribe({
      next: (data) => {
        this.barrios = data;
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Error loading barrios:', err);
        this.error = 'Error al cargar barrios';
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
