import { Component, OnDestroy, OnInit, WritableSignal, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, from, concatMap, tap, toArray } from 'rxjs';
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

interface CasoEdicionResponse {
  id: number;
  estado: string;
  tipoCaso: string;
  urgencia: string;
  descripcion: string;
  animal?: AnimalReportePayload;
  ubicacion?: {
    departamentoId?: number;
    municipioId?: number;
    barrioId?: number;
    direccionReferencia?: string;
    departamento?: string;
    municipio?: string;
  };
  fotografias?: { url?: string }[];
}

interface ReporteFormData {
  tipoCaso: string;
  urgencia: string;
  especie: string;
  descripcion: string;
  nombre: string;
  raza: string;
  color: string;
  sexo: string;
  edad: string;
  senas: string;
  direccion: string;
  deptoId: number | null;
  munId: number | null;
  barrioId: number | null;
}

@Component({
  selector: 'app-reportar',
  standalone: true,
  imports: [CommonModule, FormsModule, UbicacionSelectorComponent, RouterLink],
  templateUrl: './reportar.component.html',
  styleUrls: ['./reportar.component.css'],
})
export class ReportarComponent implements OnInit, OnDestroy {
  private casoService = inject(CasoService);
  private ubicacionService = inject(UbicacionService);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  currentStep = signal(1);
  enviando = signal(false);
  cargandoCaso = signal(false);
  casoCargado = signal(false);
  editando = signal(false);
  casoId = signal<number | null>(null);
  existingPhotoUrls = signal<string[]>([]);
  createdCasoId = signal('');
  submitError = signal('');
  submitWarning = signal('');
  pendingPhotoCaseId = signal<number | null>(null);
  fotos = signal<File[]>([]);
  previewUrls = signal<string[]>([]);
  fotoError = signal('');

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

  formData: ReporteFormData = {
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

  ngOnInit(): void {
    const id = Number(this.activatedRoute.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      return;
    }

    this.editando.set(true);
    this.casoId.set(id);
    this.cargandoCaso.set(true);
    this.casoService.getDetalle(id).subscribe({
      next: (response: unknown): void => this.cargarDatosCaso(response),
      error: (err: unknown): void => this.errorAlCargarCaso(err),
    });
  }

  private cargarDatosCaso(response: unknown): void {
    const caso = response as CasoEdicionResponse;
    if (caso.estado !== 'REGISTRADO') {
      this.submitError.set('Solo puedes editar casos que estén en estado REGISTRADO.');
      this.cargandoCaso.set(false);
      return;
    }

    this.formData = this.obtenerDatosFormulario(caso);
    this.deptoNombre.set(caso.ubicacion?.departamento ?? '');
    this.munNombre.set(caso.ubicacion?.municipio ?? '');
    this.existingPhotoUrls.set(
      (caso.fotografias ?? [])
        .map((foto) => foto.url?.trim())
        .filter((url): url is string => Boolean(url)),
    );
    this.casoCargado.set(true);
    this.cargandoCaso.set(false);
  }

  private obtenerDatosFormulario(caso: CasoEdicionResponse): ReporteFormData {
    return {
      tipoCaso: caso.tipoCaso,
      urgencia: caso.urgencia,
      descripcion: caso.descripcion,
      ...this.obtenerDatosAnimal(caso.animal),
      ...this.obtenerDatosUbicacion(caso.ubicacion),
    };
  }

  private obtenerDatosAnimal(
    animal: CasoEdicionResponse['animal'],
  ): Pick<ReporteFormData, 'especie' | 'nombre' | 'raza' | 'color' | 'sexo' | 'edad' | 'senas'> {
    const datos = animal ?? { especie: '' };
    return {
      especie: datos.especie ?? '',
      nombre: datos.nombre ?? '',
      raza: datos.raza ?? '',
      color: datos.color ?? '',
      sexo: datos.sexo ?? '',
      edad: datos.edadAproximada ?? '',
      senas: datos.senasParticulares ?? '',
    };
  }

  private obtenerDatosUbicacion(
    ubicacion: CasoEdicionResponse['ubicacion'],
  ): Pick<ReporteFormData, 'direccion' | 'deptoId' | 'munId' | 'barrioId'> {
    const datos = ubicacion ?? {};
    return {
      direccion: datos.direccionReferencia ?? '',
      deptoId: datos.departamentoId ?? null,
      munId: datos.municipioId ?? null,
      barrioId: datos.barrioId ?? null,
    };
  }

  private errorAlCargarCaso(error: unknown): void {
    console.error('Error cargando el caso para editar:', error);
    this.submitError.set('No fue posible cargar el caso para editar.');
    this.cargandoCaso.set(false);
  }

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
    void this.router.navigate(['/home']);
  }

  cancelar(): void {
    this.goHome();
  }

  getLabel(value: string, labels: Record<string, string>): string {
    return labels[value] || value;
  }

  stepOneNext(): void {
    this.validateStep(2, [
      {
        field: 'tipoCaso',
        valid: Boolean(this.formData.tipoCaso),
        message: 'Selecciona qué le pasó al animal',
      },
      {
        field: 'urgencia',
        valid: Boolean(this.formData.urgencia),
        message: 'Selecciona el nivel de urgencia',
      },
      {
        field: 'especie',
        valid: Boolean(this.formData.especie),
        message: 'Selecciona el tipo de animal',
      },
      {
        field: 'descripcion',
        valid: Boolean(this.formData.descripcion),
        message: 'Describe lo que observaste',
      },
    ]);
  }

  stepTwoNext(): void {
    this.validateStep(3, [
      {
        field: 'departamento',
        valid: Boolean(this.formData.deptoId),
        message: 'Selecciona el departamento',
      },
      {
        field: 'municipio',
        valid: Boolean(this.formData.munId),
        message: 'Selecciona el municipio',
      },
      {
        field: 'barrio',
        valid: Boolean(this.formData.barrioId),
        message: 'Selecciona el barrio',
      },
    ]);

    if (!this.editando() && this.fotos().length === 0) {
      this.errors['fotos'] = 'La foto es obligatoria. Agrega al menos una fotografía del animal.';
    }

    if (Object.keys(this.errors).length > 0) {
      this.currentStep.set(2);
    }
  }

  private validateStep(
    nextStep: number,
    validations: { field: string; valid: boolean; message: string }[],
  ): void {
    this.errors = {};

    for (const { field, valid, message } of validations) {
      if (!valid) {
        this.errors[field] = message;
      }
    }

    if (Object.keys(this.errors).length === 0) {
      this.currentStep.set(nextStep);
    }
  }

  private cleanField(val?: string): string | undefined {
    if (!val) {
      return undefined;
    }
    const trimmed = val.trim();
    return trimmed ? trimmed : undefined;
  }

  private buildAnimalPayload(): AnimalReportePayload {
    const animal: AnimalReportePayload = {
      especie: this.formData.especie,
    };
    animal.nombre = this.cleanField(this.formData.nombre);
    animal.raza = this.cleanField(this.formData.raza);
    animal.color = this.cleanField(this.formData.color);
    animal.sexo = this.formData.sexo || undefined;
    animal.edadAproximada = this.cleanField(this.formData.edad);
    animal.senasParticulares = this.cleanField(this.formData.senas);
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

    const dir = this.cleanField(this.formData.direccion);
    if (dir) {
      dto.direccionReferencia = dir;
    }

    return dto;
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
      input.value = '';
    }
  }

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    if (event.dataTransfer?.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  private validateFile(file: File): string | null {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      return 'Solo se admiten formatos JPG, PNG o WebP.';
    }
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      return 'Cada imagen debe pesar máximo 5 MB.';
    }
    return null;
  }

  private addFiles(files: File[]): void {
    this.fotoError.set('');
    delete this.errors['fotos'];
    const currentCount = this.fotos().length;
    if (currentCount + files.length > 5) {
      this.fotoError.set('Puedes subir un máximo de 5 fotos.');
      return;
    }

    const newFiles: File[] = [];
    const newUrls: string[] = [];

    for (const file of files) {
      const error = this.validateFile(file);
      if (error) {
        this.fotoError.set(error);
        return;
      }
      newFiles.push(file);
      newUrls.push(URL.createObjectURL(file));
    }

    this.fotos.update((prev) => [...prev, ...newFiles]);
    this.previewUrls.update((prev) => [...prev, ...newUrls]);
  }

  removeFoto(index: number): void {
    const urls = this.previewUrls();
    if (urls[index]) {
      URL.revokeObjectURL(urls[index]);
    }
    this.fotos.update((prev) => prev.filter((_, i) => i !== index));
    this.previewUrls.update((prev) => prev.filter((_, i) => i !== index));
  }

  ngOnDestroy(): void {
    for (const url of this.previewUrls()) {
      URL.revokeObjectURL(url);
    }
  }

  onPublicar(): void {
    this.submitError.set('');
    this.submitWarning.set('');
    if (this.reintentarCargaPendiente()) {
      return;
    }
    if (!this.validarPublicacion()) {
      return;
    }

    this.enviando.set(true);
    const dto = this.buildReporteDTO(this.buildAnimalPayload());
    if (this.editando()) {
      this.actualizarReporte(dto);
    } else {
      this.crearReporte(dto);
    }
  }

  private reintentarCargaPendiente(): boolean {
    const pendingCaseId = this.pendingPhotoCaseId();
    if (pendingCaseId === null) {
      return false;
    }

    if (this.fotos().length === 0) {
      this.currentStep.set(2);
      this.submitError.set('Agrega al menos una foto para poder reintentar la publicación.');
      return true;
    }
    this.enviando.set(true);
    this.handleUploadImages(pendingCaseId.toString());
    return true;
  }

  private validarPublicacion(): boolean {
    if (!this.editando() && this.fotos().length === 0) {
      this.currentStep.set(2);
      this.errors['fotos'] = 'La foto es obligatoria. Agrega al menos una fotografía del animal.';
      return false;
    }
    return !this.editando() || (this.casoCargado() && !this.cargandoCaso());
  }

  private actualizarReporte(dto: CrearReporteDTO): void {
    const id = this.casoId();
    if (id === null) {
      this.submitError.set('No se pudo identificar el caso que deseas editar.');
      this.enviando.set(false);
      return;
    }

    this.casoService.actualizarReporte(id, dto).subscribe({
      next: (): void => this.handleUploadImages(id.toString()),
      error: (err: unknown): void => {
        console.error('Error actualizando el caso:', err);
        this.submitError.set('No fue posible guardar los cambios. Intenta de nuevo.');
        this.enviando.set(false);
      },
    });
  }

  private crearReporte(dto: CrearReporteDTO): void {
    this.casoService.crearReporte(dto).subscribe({
      next: (response: unknown): void => {
        const idGenerado = (response as CrearReporteResponse).id?.toString() || '#NUEVO';
        this.handleUploadImages(idGenerado);
      },
      error: (err: { error?: unknown }): void => {
        console.error('Error creating report:', err);
        this.submitError.set('Error al publicar el reporte. Intenta de nuevo.');
        this.enviando.set(false);
      },
    });
  }

  private handleUploadImages(idGenerado: string): void {
    const files = this.fotos();
    const casoIdNum = Number(idGenerado);

    if (files.length === 0) {
      this.finishPublicacion(idGenerado);
      return;
    }
    if (!Number.isInteger(casoIdNum) || casoIdNum <= 0) {
      this.submitError.set(
        'El caso se registró, pero no se pudo identificar para guardar las fotos.',
      );
      this.enviando.set(false);
      return;
    }

    let uploadedCount = 0;
    from(files)
      .pipe(
        concatMap((file) =>
          this.casoService.subirImagen(file, casoIdNum).pipe(tap(() => uploadedCount++)),
        ),
        toArray(),
      )
      .subscribe({
        next: (): void => {
          this.pendingPhotoCaseId.set(null);
          this.finishPublicacion(idGenerado);
        },
        error: (uploadErr: unknown): void => {
          this.handleUploadFailure(idGenerado, casoIdNum, uploadedCount, uploadErr);
        },
      });
  }

  private handleUploadFailure(
    idGenerado: string,
    casoIdNum: number,
    uploadedCount: number,
    error: unknown,
  ): void {
    console.error('Error al subir imágenes:', error);
    if (uploadedCount > 0 || this.editando()) {
      this.submitWarning.set(
        uploadedCount > 0
          ? 'Algunas fotos no pudieron guardarse. El caso conserva las fotos que sí se cargaron.'
          : 'Los cambios se guardaron, pero no se pudo agregar la nueva foto. Las fotos anteriores se conservan.',
      );
      this.pendingPhotoCaseId.set(null);
      this.finishPublicacion(idGenerado);
      return;
    }

    this.eliminarCasoSinFoto(casoIdNum);
  }

  private eliminarCasoSinFoto(casoId: number): void {
    this.casoService.eliminarCaso(casoId).subscribe({
      next: (): void => {
        this.pendingPhotoCaseId.set(null);
        this.currentStep.set(3);
        this.submitError.set(
          'No se pudo registrar la foto y el caso no se publicó. Intenta cargarla de nuevo.',
        );
        this.enviando.set(false);
      },
      error: (error: unknown): void => {
        console.error('Error eliminando el caso sin foto:', error);
        this.pendingPhotoCaseId.set(casoId);
        this.submitError.set(
          'No se pudo guardar la foto ni eliminar el caso. Intenta publicar de nuevo para reintentar la carga.',
        );
        this.enviando.set(false);
      },
    });
  }

  private finishPublicacion(idGenerado: string): void {
    this.createdCasoId.set(idGenerado);
    this.currentStep.set(4);
    this.enviando.set(false);
  }

  onDeptoChange(deptoId: number | null): void {
    this.formData.deptoId = deptoId;
    this.updateLocationName(deptoId, this.deptoNombre, () =>
      this.ubicacionService.getDepartamentos(),
    );
  }

  onMunChange(munId: number | null): void {
    this.formData.munId = munId;
    this.updateLocationName(munId, this.munNombre, () => {
      const departamentoId = this.formData.deptoId;
      return departamentoId ? this.ubicacionService.getMunicipios(departamentoId) : null;
    });
  }

  onBarrioChange(barrioId: number | null): void {
    this.formData.barrioId = barrioId;
    this.updateLocationName(barrioId, this.barrioNombre, () => {
      const municipioId = this.formData.munId;
      return municipioId ? this.ubicacionService.getBarrios(municipioId) : null;
    });
  }

  private updateLocationName<T extends { id: number; nombre: string }>(
    selectedId: number | null,
    name: WritableSignal<string>,
    loadLocations: () => Observable<T[]> | null,
  ): void {
    const locations = selectedId ? loadLocations() : null;

    if (!locations) {
      name.set('');
      return;
    }

    locations.subscribe((items): void => {
      const selectedLocation = items.find((item) => item.id === selectedId);
      name.set(selectedLocation?.nombre || '');
    });
  }
}
