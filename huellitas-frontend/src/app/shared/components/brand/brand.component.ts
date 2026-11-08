import { Component, input } from '@angular/core';

export type BrandSize = 'sm' | 'md' | 'lg';

/** Logo y nombre de Huellitas Cafeteras (header, login y encabezado del registro). */
@Component({
  selector: 'app-brand',
  templateUrl: './brand.component.html',
  styleUrl: './brand.component.css',
  host: { '[class]': '"brand brand-" + size()' },
})
export class BrandComponent {
  readonly size = input<BrandSize>('md');
}
