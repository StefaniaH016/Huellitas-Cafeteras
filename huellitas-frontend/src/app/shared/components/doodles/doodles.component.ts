import { Component } from '@angular/core';

/** Huellas y círculos pastel semitransparentes del fondo (mismo fondo en todas las pantallas). */
@Component({
  selector: 'app-doodles',
  templateUrl: './doodles.component.html',
  styleUrl: './doodles.component.css',
  host: { 'aria-hidden': 'true' },
})
export class DoodlesComponent {
  readonly huellas = [1, 2, 3, 4, 5, 6];
  readonly circulos = ['mint', 'sky', 'amber', 'coral', 'pine', 'mint'];
}
