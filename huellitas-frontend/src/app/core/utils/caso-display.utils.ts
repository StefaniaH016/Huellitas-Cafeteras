export interface RawCasoDisplay {
  id: string | number;
  titulo?: string;
  descripcion?: string;
  urgencia?: string;
  estado?: string;
  municipio?: string;
  barrio?: string;
}

export interface CasoDisplay {
  id: string | number;
  titulo: string;
  descripcion: string;
  urgencia: string;
  estado: string;
  municipio: string;
  barrio: string;
}

export function fallbackCaseString(value?: string, fallback = ''): string {
  return value || fallback;
}

export function normalizeCasoDisplay(caso: RawCasoDisplay): CasoDisplay {
  return {
    id: caso.id,
    titulo: fallbackCaseString(caso.titulo, fallbackCaseString(caso.descripcion)),
    descripcion: fallbackCaseString(caso.descripcion),
    urgencia: caso.urgencia ? caso.urgencia.toUpperCase() : 'MEDIA',
    estado: fallbackCaseString(caso.estado, 'REGISTRADO').toUpperCase(),
    municipio: fallbackCaseString(caso.municipio),
    barrio: fallbackCaseString(caso.barrio),
  };
}

export function getUrgencyBadgeClass(urgencia: string): string {
  switch (urgencia?.toUpperCase()) {
    case 'CRITICA':
      return 'badge-emergencia';
    case 'ALTA':
      return 'badge-alta';
    case 'MEDIA':
      return 'badge-atencion';
    case 'BAJA':
      return 'badge-media';
    default:
      return 'badge-atencion';
  }
}

export function getUrgencyBadgeLabel(urgencia: string): string {
  switch (urgencia?.toUpperCase()) {
    case 'CRITICA':
      return 'Emergencia';
    case 'ALTA':
      return 'Alta';
    case 'MEDIA':
      return 'Media';
    case 'BAJA':
      return 'Baja';
    default:
      return urgencia || 'Media';
  }
}
