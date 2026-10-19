/**
 * Role Normalization Utility
 *
 * Handles mapping between backend role enum names and legacy short forms.
 * The backend DataLoader inserts roles using full enum names:
 * - ADMINISTRADOR, FUNDACION_REFUGIO, CIUDADANO_VOLUNTARIO, VETERINARIA
 *
 * However, legacy data may contain short forms:
 * - ADMIN (legacy for ADMINISTRADOR)
 * - FUNDACION (legacy for FUNDACION_REFUGIO)
 * - CIUDADANO (legacy for CIUDADANO_VOLUNTARIO)
 *
 * This function normalizes both forms to the full enum name.
 */

export type NormalizedRole =
  'ADMINISTRADOR' | 'FUNDACION_REFUGIO' | 'VETERINARIA' | 'CIUDADANO_VOLUNTARIO' | null;

/**
 * Normalize a raw role string to the full enum name.
 * Handles both full forms (backend) and short forms (legacy data).
 *
 * @param rawRole - The raw role string from token or storage
 * @returns Normalized full-form role, or null if unrecognized
 */
export function normalizeRole(rawRole: string | null | undefined): NormalizedRole {
  if (!rawRole) {
    return null;
  }

  const upper = rawRole.toUpperCase().trim();

  // Try exact match first (handles all full forms)
  const validFullForms = [
    'ADMINISTRADOR',
    'FUNDACION_REFUGIO',
    'VETERINARIA',
    'CIUDADANO_VOLUNTARIO',
  ];
  if (validFullForms.includes(upper)) {
    return upper as NormalizedRole;
  }

  // Try short form fallback (legacy data compatibility)
  const shortToFull: Record<string, NormalizedRole> = {
    ADMIN: 'ADMINISTRADOR',
    FUNDACION: 'FUNDACION_REFUGIO',
    CIUDADANO: 'CIUDADANO_VOLUNTARIO',
    VETERINARIA: 'VETERINARIA', // no mapping needed, but included for completeness
  };

  const normalized = shortToFull[upper];
  if (normalized) {
    return normalized;
  }

  // Not recognized
  console.warn(`[RoleUtils] Unrecognized role: "${rawRole}"`);
  return null;
}

/**
 * Check if a role is one of the panel roles (non-ciudadano).
 * Used for quick conditional checks in templates.
 */
export function isPanelRole(role: NormalizedRole): boolean {
  return role !== null && role !== 'CIUDADANO_VOLUNTARIO';
}
