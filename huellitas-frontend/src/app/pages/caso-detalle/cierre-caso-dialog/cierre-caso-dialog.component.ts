import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ResultadoCierre } from '../../../core/services/caso.service';

@Component({
  selector: 'app-cierre-caso-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './cierre-caso-dialog.component.html',
  styleUrl: './cierre-caso-dialog.component.css',
})
export class CierreCasoDialogComponent implements AfterViewInit {
  @ViewChild('dialog') private dialog!: ElementRef<HTMLElement>;

  @Input() puedeConfirmarReencuentro = false;
  @Input() esCasoExtraviado = false;
  @Input() requiereResponsable = false;
  @Input() accionEnCurso = false;
  @Input() error = '';
  @Input() casoId: string | number = '';
  @Input() tieneAltaMedica = false;
  @Input() veterinariaNombre = '';
  @Input() fechaAlta = '';
  @Input() tieneCustodia = false;
  @Input() fundacionNombre = '';

  @Input() motivoCierre: ResultadoCierre = 'RECUPERADO';
  @Output() motivoCierreChange = new EventEmitter<ResultadoCierre>();
  @Input() entregadoA = '';
  @Output() entregadoAChange = new EventEmitter<string>();
  @Input() observacionCierre = '';
  @Output() observacionCierreChange = new EventEmitter<string>();

  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();

  seleccionarResultado(resultado: ResultadoCierre): void {
    this.motivoCierreChange.emit(resultado);
  }

  ngAfterViewInit(): void {
    this.dialog.nativeElement.focus();
  }

  @HostListener('document:keydown.escape')
  cerrarConEscape(): void {
    this.cancelarDialogo();
  }

  @HostListener('document:click', ['$event'])
  cerrarAlHacerClickFuera(event: MouseEvent): void {
    if (!this.dialog.nativeElement.contains(event.target as Node)) {
      this.cancelarDialogo();
    }
  }

  cancelarDialogo(): void {
    if (!this.accionEnCurso) {
      this.cancelar.emit();
    }
  }
}
