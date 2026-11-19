import { Provider } from '@angular/core';
import { of } from 'rxjs';
import { UbicacionService } from '../services/ubicacion.service';

/** UbicacionService sin datos, para las pruebas de pantallas que usan el selector de zona. */
export const UBICACION_VACIA: Provider = {
  provide: UbicacionService,
  useValue: {
    getDepartamentos: () => of([]),
    getMunicipios: () => of([]),
    getBarrios: () => of([]),
  } satisfies Pick<UbicacionService, 'getDepartamentos' | 'getMunicipios' | 'getBarrios'>,
};
