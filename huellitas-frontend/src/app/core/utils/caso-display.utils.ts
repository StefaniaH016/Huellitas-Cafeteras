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

export const ESTADO_LABELS: Record<string, string> = {
  REGISTRADO: 'Registrado',
  EN_ATENCION: 'En atención',
  RESUELTO: 'Resuelto',
  ARCHIVADO: 'Archivado',
};

export const TIPO_LABELS: Record<string, string> = {
  EXTRAVIADO: 'Extraviado',
  ABANDONO: 'Abandono',
  MALTRATO: 'Maltrato',
  ENFERMEDAD: 'Enfermedad',
  VULNERABLE: 'Vulnerable',
  HERIDO: 'Herido',
  EN_ADOPCION: 'En adopción',
  OTRO: 'Otro',
};

export const URGENCIA_LABELS: Record<string, string> = {
  CRITICA: 'Emergencia',
  ALTA: 'Alta',
  MEDIA: 'Media',
  BAJA: 'Baja',
};

export const APORTE_LABELS: Record<string, string> = {
  COMENTARIO: 'Comentario',
  AVISTAMIENTO: 'Avistamiento',
  ACTUALIZACION: 'Actualización',
  INFO: 'Información',
  DENUNCIA: 'Denuncia',
};

export const RESULTADO_LABELS: Record<string, string> = {
  REENCUENTRO: 'Reencuentro',
  ENTREGADO_A_CUIDADOR: 'Adopción',
  RECUPERADO: 'Recuperado',
  FALLECIDO: 'Fallecimiento',
};

export function labelOptions(labels: Record<string, string>): { label: string; value: string }[] {
  return Object.entries(labels).map(([key, label]) => ({
    label,
    value: key,
  }));
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

export function esCasoAbierto(estado: string): boolean {
  const estadoUpper = (estado || '').toUpperCase();
  return estadoUpper === 'REGISTRADO' || estadoUpper === 'EN_ATENCION';
}

export function formatFecha(fecha?: string): string {
  if (!fecha) {
    return '';
  }
  try {
    const date = new Date(fecha);
    if (isNaN(date.getTime())) {
      return '';
    }
    return date.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return '';
  }
}

export function formatFechaHora(fecha?: string): string {
  if (!fecha) {
    return '';
  }
  try {
    const date = new Date(fecha);
    if (isNaN(date.getTime())) {
      return '';
    }
    return date.toLocaleString('es-CO', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return '';
  }
}

export function haceCuanto(fecha?: string): string {
  if (!fecha) {
    return '';
  }
  try {
    const date = new Date(fecha);
    if (isNaN(date.getTime())) {
      return '';
    }
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (seconds < 60) {
      return 'hace poco';
    }
    if (minutes < 60) {
      return `hace ${minutes}m`;
    }
    if (hours < 24) {
      return `hace ${hours}h`;
    }
    if (days < 7) {
      return `hace ${days}d`;
    }
    return formatFecha(fecha);
  } catch {
    return '';
  }
}

export function labelOf(labels: Record<string, string>, key: string): string {
  // Try exact match first
  if (labels[key]) {
    return labels[key];
  }
  // Try uppercase match for case-insensitive lookup
  const upperKey = key.toUpperCase();
  if (labels[upperKey]) {
    return labels[upperKey];
  }
  // Return the original key as fallback
  return key;
}

export function estadoBadge(estado: string): string {
  switch ((estado || '').toUpperCase()) {
    case 'RESUELTO':
    case 'CERRADO':
      return 'badge-success';
    case 'EN_ATENCION':
    case 'EN_CUSTODIA':
      return 'badge-warning';
    case 'ARCHIVADO':
    case 'DERIVADO':
      return 'badge-info';
    default:
      return 'badge-secondary';
  }
}

export function urgenciaBadge(urgencia: string): string {
  return getUrgencyBadgeClass(urgencia);
}

export function lugarTexto(municipio?: string, barrio?: string, fecha?: string): string {
  const partes = [municipio, barrio].filter((p) => p);
  const lugar = partes.join(' · ') || 'Ubicación desconocida';
  return fecha ? `${lugar} · ${fecha}` : lugar;
}
