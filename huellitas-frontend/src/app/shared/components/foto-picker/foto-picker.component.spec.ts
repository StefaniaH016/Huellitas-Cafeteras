import { TestBed } from '@angular/core/testing';
import { FotoPickerComponent } from './foto-picker.component';

function crear(): FotoPickerComponent {
  const fixture = TestBed.createComponent(FotoPickerComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

beforeEach(() => {
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:x');
  vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
});

afterEach(() => vi.restoreAllMocks());

it('adds valid photos, removes them and clears the selection', () => {
  const component = crear();
  const cambios: File[][] = [];
  component.archivosChange.subscribe((lista) => cambios.push(lista));
  const foto = new File(['x'], 'a.jpg', { type: 'image/jpeg' });

  component.onDrop({
    preventDefault: vi.fn().mockReturnValue(undefined),
    dataTransfer: { files: [foto] },
  } as unknown as DragEvent);
  expect(component.archivos()).toEqual([foto]);

  component.quitar(0);
  expect(component.archivos()).toEqual([]);

  component.onDrop({
    preventDefault: vi.fn().mockReturnValue(undefined),
    dataTransfer: { files: [foto] },
  } as unknown as DragEvent);
  component.limpiar();
  expect(cambios.at(-1)).toEqual([]);
});

it('rejects files that are not images', () => {
  const component = crear();
  const pdf = new File(['x'], 'a.pdf', { type: 'application/pdf' });

  component.onDrop({
    preventDefault: vi.fn().mockReturnValue(undefined),
    dataTransfer: { files: [pdf] },
  } as unknown as DragEvent);

  expect(component.archivos()).toEqual([]);
  expect(component.error()).not.toBe('');
});
