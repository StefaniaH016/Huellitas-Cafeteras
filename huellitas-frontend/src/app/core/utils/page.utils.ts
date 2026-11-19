/** Respuesta paginada de Spring Data (Page<T>), como la devuelven /casos y /notificaciones. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** Parámetros de paginación que entiende el backend (Pageable). */
export interface PageRequest {
  page?: number;
  size?: number;
}

/** Contenido de una página; tolera una lista plana o una respuesta vacía. */
export function pageContent<T>(response: Page<T> | T[] | null | undefined): T[] {
  if (!response) {
    return [];
  }
  return Array.isArray(response) ? response : (response.content ?? []);
}

/** Total de elementos de una página; para una lista plana es su longitud. */
export function pageTotal<T>(response: Page<T> | T[] | null | undefined): number {
  if (!response) {
    return 0;
  }
  return Array.isArray(response) ? response.length : (response.totalElements ?? 0);
}

/** Quita los filtros vacíos para no mandarlos como parámetros de la URL. */
export function compactParams(
  params: Record<string, string | number | boolean | null | undefined>,
): Record<string, string | number | boolean> {
  const result: Record<string, string | number | boolean> = {};
  for (const [key, value] of Object.entries(params)) {
    if (value !== null && value !== undefined && value !== '') {
      result[key] = value;
    }
  }
  return result;
}
