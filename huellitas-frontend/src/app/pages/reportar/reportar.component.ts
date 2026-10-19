import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import { UbicacionSelectorComponent } from '../../shared/components/ubicacion-selector/ubicacion-selector.component';

interface AnimalReportePayload {
  especie: string;
  nombre?: string;
  raza?: string;
  color?: string;
  sexo?: string;
  edadAproximada?: string;
  senasParticulares?: string;
}

interface CrearReporteDTO {
  tipoCaso: string;
  urgencia: string;
  descripcion: string;
  barrioId: number | null;
  animal: AnimalReportePayload;
  direccionReferencia?: string;
}

interface CrearReporteResponse {
  id?: string | number;
}

@Component({
  selector: 'app-reportar',
  standalone: true,
  imports: [CommonModule, FormsModule, UbicacionSelectorComponent, RouterLink],
  templateUrl: './reportar.component.html',
  styleUrls: ['./reportar.component.css'],
})
export class ReportarComponent {
  private casoService = inject(CasoService);
  private ubicacionService = inject(UbicacionService);
  private router = inject(Router);

  currentStep = signal(1);
  enviando = signal(false);
  createdCasoId = signal('');
  submitError = signal('');

  tipoCasoLabels: Record<string, string> = {
    HERIDO: 'Herido',
    VULNERABLE: 'Vulnerable',
    EXTRAVIADO: 'Extraviado',
  };

  urgenciaLabels: Record<string, string> = {
    CRITICA: 'Emergencia · riesgo inmediato',
    ALTA: 'Alta · necesita atención pronto',
    MEDIA: 'Media',
    BAJA: 'Baja · puede esperar',
  };

  especieLabels: Record<string, string> = {
    PERRO: 'Perro',
    GATO: 'Gato',
    OTRO: 'Otro animal',
  };

  deptoNombre = signal('');
  munNombre = signal('');
  barrioNombre = signal('');

  formData = {
    tipoCaso: '',
    urgencia: '',
    especie: '',
    descripcion: '',
    nombre: '',
    raza: '',
    color: '',
    sexo: '',
    edad: '',
    senas: '',
    direccion: '',
    deptoId: null as number | null,
    munId: null as number | null,
    barrioId: null as number | null,
  };

  errors: Record<string, string> = {};

  buildDTO(): Record<string, unknown> {
    return {
      tipoCaso: this.formData.tipoCaso,
      urgencia: this.formData.urgencia,
      especie: this.formData.especie,
      descripcion: this.formData.descripcion,
      nombre: this.formData.nombre,
      raza: this.formData.raza,
      color: this.formData.color,
      sexo: this.formData.sexo,
      edad: this.formData.edad,
      senas: this.formData.senas,
      direccion: this.formData.direccion,
      barrioId: this.formData.barrioId,
    };
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  cancelar(): void {
    this.goHome();
  }

  getLabel(value: string, labels: Record<string, string>): string {
    return labels[value] || value;
  }

  stepOneNext(): void {
    this.errors = {};

    if (!this.formData.tipoCaso) {
      this.errors['tipoCaso'] = 'Selecciona qué le pasó al animal';
    }
    if (!this.formData.urgencia) {
      this.errors['urgencia'] = 'Selecciona el nivel de urgencia';
    }
    if (!this.formData.especie) {
      this.errors['especie'] = 'Selecciona el tipo de animal';
    }
    if (!this.formData.descripcion) {
      this.errors['descripcion'] = 'Describe lo que observaste';
    }

    if (Object.keys(this.errors).length === 0) {
      this.currentStep.set(2);
    }
  }

  stepTwoNext(): void {
    this.errors = {};

    if (!this.formData.deptoId) {
      this.errors['departamento'] = 'Selecciona el departamento';
    }
    if (!this.formData.munId) {
      this.errors['municipio'] = 'Selecciona el municipio';
    }
    if (!this.formData.barrioId) {
      this.errors['barrio'] = 'Selecciona el barrio';
    }

    if (Object.keys(this.errors).length === 0) {
      this.currentStep.set(3);
    }
  }

  private buildAnimalPayload(): AnimalReportePayload {
    const animal: AnimalReportePayload = {
      especie: this.formData.especie,
    };

    if (this.formData.nombre?.trim()) {
      animal.nombre = this.formData.nombre;
    }
    if (this.formData.raza?.trim()) {
      animal.raza = this.formData.raza;
    }
    if (this.formData.color?.trim()) {
      animal.color = this.formData.color;
    }
    if (this.formData.sexo) {
      animal.sexo = this.formData.sexo;
    }
    if (this.formData.edad?.trim()) {
      animal.edadAproximada = this.formData.edad;
    }
    if (this.formData.senas?.trim()) {
      animal.senasParticulares = this.formData.senas;
    }

    return animal;
  }

  private buildReporteDTO(animalPayload: AnimalReportePayload): CrearReporteDTO {
    const dto: CrearReporteDTO = {
      tipoCaso: this.formData.tipoCaso,
      urgencia: this.formData.urgencia,
      descripcion: this.formData.descripcion,
      barrioId: this.formData.barrioId,
      animal: animalPayload,
    };

    if (this.formData.direccion?.trim()) {
      dto.direccionReferencia = this.formData.direccion;
    }

    return dto;
  }

  onPublicar(): void {
    this.submitError.set('');
    this.enviando.set(true);

    const animal = this.buildAnimalPayload();
    const dto = this.buildReporteDTO(animal);

    this.casoService.crearReporte(dto).subscribe({
      next: (response: unknown): void => {
        const idGenerado = (response as CrearReporteResponse).id?.toString() || '#NUEVO';
        this.createdCasoId.set(idGenerado);
        this.currentStep.set(4);
        this.enviando.set(false);
      },
      error: (err: { error?: unknown }): void => {
        console.error('Error creating report:', err);
        console.error('Error response:', err.error);
        this.submitError.set('Error al publicar el reporte. Intenta de nuevo.');
        this.enviando.set(false);
      },
    });
  }

  onDeptoChange(deptoId: number | null): void {
    this.formData.deptoId = deptoId;
    if (deptoId) {
      this.ubicacionService.getDepartamentos().subscribe((deptos): void => {
        const depto = deptos.find((d) => d.id === deptoId);
        this.deptoNombre.set(depto?.nombre || '');
      });
    } else {
      this.deptoNombre.set('');
    }
  }

  onMunChange(munId: number | null): void {
    this.formData.munId = munId;
    if (munId && this.formData.deptoId) {
      this.ubicacionService.getMunicipios(this.formData.deptoId).subscribe((muns): void => {
        const mun = muns.find((m) => m.id === munId);
        this.munNombre.set(mun?.nombre || '');
      });
    } else {
      this.munNombre.set('');
    }
  }

  onBarrioChange(barrioId: number | null): void {
    this.formData.barrioId = barrioId;
    if (barrioId && this.formData.munId) {
      this.ubicacionService.getBarrios(this.formData.munId).subscribe((barrios): void => {
        const barrio = barrios.find((b) => b.id === barrioId);
        this.barrioNombre.set(barrio?.nombre || '');
      });
    } else {
      this.barrioNombre.set('');
    }
  }
}
