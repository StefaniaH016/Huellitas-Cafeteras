import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CasoService } from '../../core/services/caso.service';
import { UbicacionService } from '../../core/services/ubicacion.service';
import { ReportarComponent } from './reportar.component';

let casoService: Pick<
  CasoService,
  | 'actualizarReporte'
  | 'crearReporte'
  | 'eliminarCaso'
  | 'eliminarFoto'
  | 'getDetalle'
  | 'subirImagen'
>;
let ubicacionService: Pick<UbicacionService, 'getBarrios' | 'getDepartamentos' | 'getMunicipios'>;
let router: Router;

beforeEach(() => {
  casoService = {
    actualizarReporte: vi.fn().mockReturnValue(of({ id: 21 })),
    crearReporte: vi.fn().mockReturnValue(of({ id: 21 })),
    eliminarCaso: vi.fn().mockReturnValue(of(undefined)),
    eliminarFoto: vi.fn().mockReturnValue(of(undefined)),
    getDetalle: vi.fn().mockReturnValue(of({})),
    subirImagen: vi.fn().mockReturnValue(of({ url: 'https://example.test/photo.jpg' })),
  };
  ubicacionService = {
    getBarrios: vi.fn().mockReturnValue(of([{ id: 3, nombre: 'Centro' }])),
    getDepartamentos: vi.fn().mockReturnValue(of([{ id: 1, nombre: 'Quindío' }])),
    getMunicipios: vi.fn().mockReturnValue(of([{ id: 2, nombre: 'Armenia' }])),
  };
  TestBed.configureTestingModule({
    imports: [ReportarComponent],
    providers: [
      provideRouter([]),
      { provide: CasoService, useValue: casoService },
      { provide: UbicacionService, useValue: ubicacionService },
    ],
  });
  router = TestBed.inject(Router);
  vi.spyOn(router, 'navigate').mockResolvedValue(true);
});

afterEach(() => {
  vi.restoreAllMocks();
});

function createComponent(): ReportarComponent {
  const fixture = TestBed.createComponent(ReportarComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

function completeForm(component: ReportarComponent): void {
  component.formData = {
    tipoCaso: 'HERIDO',
    urgencia: 'ALTA',
    especie: 'PERRO',
    descripcion: 'Necesita atención',
    nombre: 'Luna',
    raza: 'Criolla',
    color: 'Blanca',
    sexo: 'HEMBRA',
    edad: '2 años',
    senas: 'Mancha en la pata',
    direccion: 'Cerca del parque',
    deptoId: 1,
    munId: 2,
    barrioId: 3,
  };
}

it('renders the initial reporting step', () => {
  const fixture = TestBed.createComponent(ReportarComponent);
  fixture.detectChanges();

  expect(fixture.componentInstance.currentStep()).toBe(1);
  expect(fixture.nativeElement.textContent).toContain('Qué pasó');
});

it('validates required fields in step one and advances when complete', () => {
  const component = createComponent();
  component.stepOneNext();

  expect(component.errors).toEqual({
    tipoCaso: 'Selecciona qué le pasó al animal',
    urgencia: 'Selecciona el nivel de urgencia',
    especie: 'Selecciona el tipo de animal',
    descripcion: 'Describe lo que observaste',
  });
  expect(component.currentStep()).toBe(1);

  component.formData.tipoCaso = 'HERIDO';
  component.formData.urgencia = 'ALTA';
  component.formData.especie = 'PERRO';
  component.formData.descripcion = 'Necesita atención';
  component.stepOneNext();

  expect(component.errors).toEqual({});
  expect(component.currentStep()).toBe(2);
});

it('validates location fields in step two and advances when complete', () => {
  const component = createComponent();
  component.stepTwoNext();

  expect(component.errors).toEqual({
    departamento: 'Selecciona el departamento',
    municipio: 'Selecciona el municipio',
    barrio: 'Selecciona el barrio',
    fotos: 'La foto es obligatoria. Agrega al menos una fotografía del animal.',
  });

  component.formData.deptoId = 1;
  component.formData.munId = 2;
  component.formData.barrioId = 3;
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:photo');
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [new File(['photo'], 'animal.jpg', { type: 'image/jpeg' })] },
  } as unknown as DragEvent);
  component.stepTwoNext();

  expect(component.currentStep()).toBe(3);
});

it('builds report values, omitting blank optional fields', () => {
  const component = createComponent();
  completeForm(component);

  expect(component.buildDTO()).toEqual({
    tipoCaso: 'HERIDO',
    urgencia: 'ALTA',
    especie: 'PERRO',
    descripcion: 'Necesita atención',
    nombre: 'Luna',
    raza: 'Criolla',
    color: 'Blanca',
    sexo: 'HEMBRA',
    edad: '2 años',
    senas: 'Mancha en la pata',
    direccion: 'Cerca del parque',
    barrioId: 3,
  });
  expect(component.getLabel('HERIDO', component.tipoCasoLabels)).toBe('Herido');
  expect(component.getLabel('OTRO', component.tipoCasoLabels)).toBe('OTRO');
});

it('accepts valid image files and rejects unsupported, oversized, and excess files', () => {
  const component = createComponent();
  const createObjectUrl = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:preview');
  const validFile = new File(['image'], 'animal.png', { type: 'image/png' });
  component.onFilesSelected({
    target: { files: [validFile], value: 'selected' },
  } as unknown as Event);

  expect(component.fotos()).toEqual([validFile]);
  expect(component.previewUrls()).toEqual(['blob:preview']);
  expect(createObjectUrl).toHaveBeenCalledWith(validFile);

  component.removeFoto(0);
  const unsupportedFile = new File(['text'], 'notes.txt', { type: 'text/plain' });
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [unsupportedFile] },
  } as unknown as DragEvent);
  expect(component.fotoError()).toContain('JPG, PNG o WebP');

  const largeFile = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'large.jpg', {
    type: 'image/jpeg',
  });
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [largeFile] },
  } as unknown as DragEvent);
  expect(component.fotoError()).toContain('5 MB');

  const sixFiles = Array.from(
    { length: 6 },
    (_, index) => new File(['image'], `animal-${index}.webp`, { type: 'image/webp' }),
  );
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: sixFiles },
  } as unknown as DragEvent);
  expect(component.fotoError()).toContain('máximo de 5');
  expect(component.fotos()).toEqual([]);
});

it('revokes preview URLs when removing photos and destroying the component', () => {
  const component = createComponent();
  const revokeObjectUrl = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
  vi.spyOn(URL, 'createObjectURL')
    .mockReturnValueOnce('blob:first')
    .mockReturnValueOnce('blob:second');
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: {
      files: [
        new File(['first'], 'first.jpg', { type: 'image/jpeg' }),
        new File(['second'], 'second.jpg', { type: 'image/jpeg' }),
      ],
    },
  } as unknown as DragEvent);

  component.removeFoto(0);
  expect(revokeObjectUrl).toHaveBeenCalledWith('blob:first');
  expect(component.fotos()).toHaveLength(1);

  component.ngOnDestroy();
  expect(revokeObjectUrl).toHaveBeenCalledWith('blob:second');
});

it('prevents default drag behavior', () => {
  const component = createComponent();
  const preventDefault = vi.fn();

  component.onDragOver({ preventDefault } as unknown as DragEvent);

  expect(preventDefault).toHaveBeenCalledOnce();
});

it('requires at least one photo before advancing or creating a report', () => {
  const component = createComponent();
  completeForm(component);

  component.stepTwoNext();
  expect(component.currentStep()).toBe(2);
  expect(component.errors['fotos']).toContain('La foto es obligatoria');

  component.onPublicar();

  expect(casoService.crearReporte).not.toHaveBeenCalled();
  expect(component.currentStep()).toBe(2);
  expect(component.enviando()).toBe(false);
});

it('creates a report and uploads selected photos sequentially', () => {
  const component = createComponent();
  completeForm(component);
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:photo');
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [new File(['photo'], 'animal.jpg', { type: 'image/jpeg' })] },
  } as unknown as DragEvent);

  component.onPublicar();

  expect(casoService.crearReporte).toHaveBeenCalledWith(
    expect.objectContaining({
      tipoCaso: 'HERIDO',
      urgencia: 'ALTA',
      barrioId: 3,
      direccionReferencia: 'Cerca del parque',
      animal: expect.objectContaining({ especie: 'PERRO', nombre: 'Luna' }),
    }),
  );
  expect(casoService.subirImagen).toHaveBeenCalledWith(component.fotos()[0], 21);
  expect(component.currentStep()).toBe(4);
  expect(component.createdCasoId()).toBe('21');
});

it('finishes the report and reports an error when report creation fails', () => {
  const component = createComponent();
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.crearReporte).mockReturnValue(throwError(() => new Error('create')));
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:photo');
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [new File(['photo'], 'animal.jpg', { type: 'image/jpeg' })] },
  } as unknown as DragEvent);

  component.onPublicar();

  expect(component.submitError()).toContain('Error al publicar');
  expect(component.enviando()).toBe(false);
  expect(error).toHaveBeenCalled();
});

it('updates a report without requiring a new photo', () => {
  const component = createComponent();
  completeForm(component);
  component.editando.set(true);
  component.casoId.set(21);
  component.casoCargado.set(true);
  component.existingPhotos.set([{ id: 5, url: 'https://example.test/photo.jpg' }]);

  component.onPublicar();

  expect(casoService.actualizarReporte).toHaveBeenCalledWith(
    21,
    expect.objectContaining({ descripcion: 'Necesita atención', barrioId: 3 }),
  );
  expect(casoService.crearReporte).not.toHaveBeenCalled();
  expect(component.currentStep()).toBe(4);
});

it('deletes a registered photo but keeps at least one photo in the case', () => {
  const component = createComponent();
  component.editando.set(true);
  component.casoId.set(21);
  component.existingPhotos.set([
    { id: 5, url: 'https://example.test/first.jpg' },
    { id: 6, url: 'https://example.test/second.jpg' },
  ]);

  component.eliminarFotoExistente(component.existingPhotos()[0]);

  expect(casoService.eliminarFoto).toHaveBeenCalledWith(21, 5);
  expect(component.existingPhotos()).toEqual([{ id: 6, url: 'https://example.test/second.jpg' }]);
  component.eliminarFotoExistente(component.existingPhotos()[0]);
  expect(casoService.eliminarFoto).toHaveBeenCalledTimes(1);
});

it('reports when the backend has not deployed the photo removal endpoint', () => {
  const component = createComponent();
  component.editando.set(true);
  component.casoId.set(21);
  component.existingPhotos.set([
    { id: 5, url: 'https://example.test/first.jpg' },
    { id: 6, url: 'https://example.test/second.jpg' },
  ]);
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(casoService.eliminarFoto).mockReturnValue(throwError(() => ({ status: 405 })));

  component.eliminarFotoExistente(component.existingPhotos()[0]);

  expect(component.fotoError()).toContain('Debe desplegarse la última versión del backend');
});

it('deletes the newly created case if no photo could be saved', () => {
  const component = createComponent();
  completeForm(component);
  const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:photo');
  vi.mocked(casoService.subirImagen).mockReturnValue(throwError(() => new Error('upload')));
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [new File(['photo'], 'animal.jpg', { type: 'image/jpeg' })] },
  } as unknown as DragEvent);

  component.onPublicar();

  expect(casoService.eliminarCaso).toHaveBeenCalledWith(21);
  expect(component.currentStep()).toBe(3);
  expect(component.submitError()).toContain('el caso no se publicó');
  expect(component.enviando()).toBe(false);
  expect(error).toHaveBeenCalled();
});

it('keeps the case pending and retries image upload if cleanup fails', () => {
  const component = createComponent();
  completeForm(component);
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:photo');
  vi.mocked(casoService.subirImagen).mockReturnValue(throwError(() => new Error('upload')));
  vi.mocked(casoService.eliminarCaso).mockReturnValue(throwError(() => new Error('delete')));
  component.onFileDrop({
    preventDefault: vi.fn(),
    dataTransfer: { files: [new File(['photo'], 'animal.jpg', { type: 'image/jpeg' })] },
  } as unknown as DragEvent);

  component.onPublicar();
  expect(component.pendingPhotoCaseId()).toBe(21);
  vi.mocked(casoService.subirImagen).mockReturnValue(of({ url: 'https://example.test/photo.jpg' }));

  component.onPublicar();

  expect(casoService.crearReporte).toHaveBeenCalledOnce();
  expect(component.pendingPhotoCaseId()).toBeNull();
  expect(component.currentStep()).toBe(4);
});

it('resolves location names and clears them when a selection is removed', () => {
  const component = createComponent();

  component.onDeptoChange(1);
  component.onMunChange(2);
  component.onBarrioChange(3);

  expect(component.deptoNombre()).toBe('Quindío');
  expect(component.munNombre()).toBe('Armenia');
  expect(component.barrioNombre()).toBe('Centro');

  component.onDeptoChange(null);
  component.onMunChange(null);
  component.onBarrioChange(null);
  expect(component.deptoNombre()).toBe('');
  expect(component.munNombre()).toBe('');
  expect(component.barrioNombre()).toBe('');
});

it('navigates home for cancel and go-home actions', () => {
  const component = createComponent();

  component.cancelar();

  expect(router.navigate).toHaveBeenCalledWith(['/home']);
});
