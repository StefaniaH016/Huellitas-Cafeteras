import { Component, OnDestroy, input, output, signal } from '@angular/core';
import {
  FOTO_ACCEPT,
  FOTO_AYUDA,
  FOTO_MAX_CANTIDAD,
  validarLoteFotos,
} from '../../../core/utils/foto.utils';

/** Selector de fotos con arrastrar y soltar, vista previa y las reglas de tamaño y formato. */
@Component({
  selector: 'app-foto-picker',
  templateUrl: './foto-picker.component.html',
  styleUrl: './foto-picker.component.css',
})
export class FotoPickerComponent implements OnDestroy {
  readonly campoId = input('fotos');
  readonly archivosChange = output<File[]>();

  readonly accept = FOTO_ACCEPT;
  readonly ayuda = FOTO_AYUDA;
  readonly maximo = FOTO_MAX_CANTIDAD;

  readonly archivos = signal<File[]>([]);
  readonly previews = signal<string[]>([]);
  readonly error = signal('');

  onFilesSelected(event: Event): void {
    const campo = event.target as HTMLInputElement;
    if (campo.files) {
      this.agregar(Array.from(campo.files));
      campo.value = '';
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    if (event.dataTransfer?.files) {
      this.agregar(Array.from(event.dataTransfer.files));
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  quitar(index: number): void {
    URL.revokeObjectURL(this.previews()[index]);
    this.archivos.update((lista) => lista.filter((_, i) => i !== index));
    this.previews.update((lista) => lista.filter((_, i) => i !== index));
    this.archivosChange.emit(this.archivos());
  }

  /** Vacía la selección (después de publicar). */
  limpiar(): void {
    this.previews().forEach((url) => URL.revokeObjectURL(url));
    this.archivos.set([]);
    this.previews.set([]);
    this.archivosChange.emit([]);
  }

  ngOnDestroy(): void {
    this.previews().forEach((url) => URL.revokeObjectURL(url));
  }

  private agregar(nuevas: File[]): void {
    const error = validarLoteFotos(nuevas, this.archivos().length);
    this.error.set(error ?? '');
    if (error) {
      return;
    }
    this.archivos.update((lista) => [...lista, ...nuevas]);
    this.previews.update((lista) => [...lista, ...nuevas.map((f) => URL.createObjectURL(f))]);
    this.archivosChange.emit(this.archivos());
  }
}
