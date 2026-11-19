import { Component, input } from '@angular/core';

/** Indicador de pasos de un asistente (registro y reportar). */
@Component({
  selector: 'app-steps',
  templateUrl: './steps.component.html',
})
export class StepsComponent {
  readonly pasos = input.required<string[]>();
  /** Paso actual, empezando en 1. */
  readonly actual = input.required<number>();
  readonly etiqueta = input('Pasos');
}
