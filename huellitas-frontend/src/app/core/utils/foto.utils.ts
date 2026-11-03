/** Reglas de las fotos de un caso o de un aporte (RNF: hasta 5 fotos JPG, PNG o WebP de 5 MB). */
export const FOTO_TIPOS = ['image/jpeg', 'image/png', 'image/webp'];
export const FOTO_MAX_BYTES = 5 * 1024 * 1024;
export const FOTO_MAX_CANTIDAD = 5;
export const FOTO_ACCEPT = FOTO_TIPOS.join(',');
export const FOTO_AYUDA = 'JPG, PNG o WebP · hasta 5 fotos de máximo 5 MB';

/** Mensaje de error de un archivo, o null si se puede subir. */
export function validarFoto(file: File): string | null {
  if (!FOTO_TIPOS.includes(file.type)) {
    return 'Solo se admiten formatos JPG, PNG o WebP.';
  }
  if (file.size > FOTO_MAX_BYTES) {
    return 'Cada imagen debe pesar máximo 5 MB.';
  }
  return null;
}

/** Valida un lote nuevo contra las fotos que ya hay; devuelve el primer error o null. */
export function validarLoteFotos(nuevas: File[], actuales: number): string | null {
  if (actuales + nuevas.length > FOTO_MAX_CANTIDAD) {
    return `Puedes subir un máximo de ${FOTO_MAX_CANTIDAD} fotos.`;
  }
  for (const file of nuevas) {
    const error = validarFoto(file);
    if (error) {
      return error;
    }
  }
  return null;
}
