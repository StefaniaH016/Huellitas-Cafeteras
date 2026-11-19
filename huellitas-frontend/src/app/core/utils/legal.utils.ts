/** Datos comunes de la Política de Tratamiento de Datos y de los Términos y Condiciones. */
export const CORREO_SOPORTE = 'soporte@huellitascafeteras.co';
export const LEGAL_VERSION = '1.0';
export const LEGAL_VIGENTE_DESDE = '28 de octubre de 2026';

/** Entrada del índice de un documento legal (id del <section> y título). */
export interface LegalSeccion {
  id: string;
  titulo: string;
}
