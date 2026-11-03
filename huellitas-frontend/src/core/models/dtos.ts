/**
 * DTOs - Interfaces de Transferencia de Datos
 *
 * Archivo que contiene todas las interfaces TypeScript que representan
 * los DTOs Java del backend para aportes, bitácora y notificaciones.
 */

// ============================================================================
// APORTES
// ============================================================================

/**
 * DTO para crear un nuevo aporte
 * Se utiliza cuando el usuario reporta información adicional sobre un caso
 */
export interface CrearAporteDTO {
  /** Descripción del aporte o información adicional */
  descripcion: string;

  /** ID del departamento donde se reporta el aporte */
  deptoId?: number;

  /** ID del municipio donde se reporta el aporte */
  munId?: number;

  /** ID del barrio donde se reporta el aporte */
  barrioId?: number;

  /** Ubicación en texto libre del aporte (referencia opcional) */
  ubicacionTexto?: string;

  /** Lista de URLs de fotografías asociadas al aporte */
  fotografias: string[];
}

/**
 * DTO de respuesta para un aporte creado o recuperado
 * Contiene la información completa del aporte con datos adicionales del servidor
 */
export interface AporteResponseDTO {
  /** ID único del aporte */
  id: number;

  /** ID del caso al que pertenece el aporte */
  casoId: number;

  /** ID del usuario que realizó el aporte */
  autorId: number;

  /** Descripción del aporte */
  descripcion: string;

  /** Ubicación en texto del aporte */
  ubicacionTexto: string;

  /** Lista de URLs de fotografías */
  fotografias: string[];

  /** Fecha de creación del aporte (formato ISO 8601) */
  fecha: string;
}

/**
 * DTO para ocultar un aporte (moderación)
 * Se utiliza cuando el administrador oculta un aporte por motivos de moderación
 */
export interface OcultarAporteDTO {
  /** Motivo por el que se oculta el aporte */
  motivo: string;
}

/**
 * DTO para gestionar custodia de un caso
 * Se utiliza cuando una fundación/refugio asume o cede la custodia de un caso
 */
export interface CustodiaDTO {
  /** Indica si se asume (true) o se cede (false) la custodia */
  asumir: boolean;
}

// ============================================================================
// BITÁCORA MÉDICA
// ============================================================================

/**
 * DTO para abrir/crear una nueva bitácora médica
 * Se utiliza cuando se inicia el seguimiento médico de un caso
 */
export interface AbrirBitacoraDTO {
  /** Observación inicial del veterinario al abrir la bitácora */
  observacionInicial: string;
}

/**
 * DTO para registrar la evolución del paciente
 * Captura información médica durante el seguimiento del caso
 */
export interface EvolucionDTO {
  /** Diagnóstico del paciente (requerido) */
  diagnostico: string;

  /** Procedimientos realizados */
  procedimientos?: string;

  /** Tratamiento prescrito */
  tratamiento?: string;

  /** Medicamentos administrados */
  medicamentos?: string;

  /** Observaciones adicionales del veterinario */
  observaciones?: string;
}

/**
 * DTO para visualizar los detalles de una evolución registrada
 * Respuesta del servidor con información completamente formada
 */
export interface EvolucionDetalleDTO {
  /** ID único de la evolución */
  id: number;

  /** Fecha de registro de la evolución (formato ISO 8601) */
  fecha: string;

  /** Diagnóstico registrado */
  diagnostico: string;

  /** Tratamiento registrado */
  tratamiento: string;

  /** Observaciones registradas */
  observaciones: string;
}

/**
 * DTO para registrar el alta médica de un paciente
 * Marca el cierre del seguimiento médico con observaciones finales
 */
export interface AltaMedicaDTO {
  /** Condición del alta médica (requerido) - ej: "curado", "estable", "derivado" */
  condicionAlta: string;

  /** Indicaciones post-alta para el cuidador */
  indicaciones?: string;
}

/**
 * DTO de respuesta cuando se registra un alta médica
 * Contiene confirmación del alta y el nuevo estado de la bitácora
 */
export interface AltaMedicaResponseDTO {
  /** ID del caso al que pertenece el alta */
  casoId: number;

  /** Fecha en que se registró el alta (formato ISO 8601) */
  fechaAlta: string;

  /** Estado actual de la bitácora después del alta (ej: "cerrada") */
  estadoBitacora: string;
}

/**
 * DTO de respuesta para una bitácora médica creada o recuperada
 * Información resumida de la bitácora sin detalles completos
 */
export interface BitacoraResponseDTO {
  /** ID único de la bitácora */
  id: number;

  /** ID del caso asociado a la bitácora */
  casoId: number;

  /** ID del veterinario responsable de la bitácora */
  veterinariaId: number;

  /** Fecha de apertura de la bitácora (formato ISO 8601) */
  fechaApertura: string;

  /** Estado actual de la bitácora (ej: "abierta", "cerrada") */
  estado: string;
}

/**
 * DTO de respuesta para un registro médico en la bitácora
 * Representa una entrada o evento en la bitácora
 */
export interface RegistroMedicoResponseDTO {
  /** ID único del registro */
  id: number;

  /** ID de la bitácora a la que pertenece el registro */
  bitacoraId: number;

  /** Fecha del registro (formato ISO 8601) */
  fecha: string;
}

/**
 * DTO completo de bitácora con todos sus detalles
 * Se utiliza para visualizar la bitácora completa de un caso
 */
export interface BitacoraDetalleDTO {
  /** ID único de la bitácora */
  id: number;

  /** ID del caso asociado */
  casoId: number;

  /** Estado actual de la bitácora */
  estado: string;

  /** Array de evoluciones registradas en la bitácora */
  evoluciones: EvolucionDetalleDTO[];

  /** Información del alta médica (si existe) */
  alta?: object;
}

// ============================================================================
// NOTIFICACIONES
// ============================================================================

/**
 * DTO para representar una notificación del sistema
 * Contiene información de alertas y mensajes para el usuario
 */
export interface NotificacionDTO {
  /** ID único de la notificación */
  id: number;

  /** Tipo de notificación (ej: "info", "alerta", "recordatorio") */
  tipo: string;

  /** Mensaje de la notificación */
  mensaje: string;

  /** Indica si la notificación ha sido leída por el usuario */
  leida: boolean;

  /** Fecha de creación de la notificación (formato ISO 8601) */
  fecha: string;

  /** ID del caso asociado a la notificación (si aplica) */
  casoId: number;
}
