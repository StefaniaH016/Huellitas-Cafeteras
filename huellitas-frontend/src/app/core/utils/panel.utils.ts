export interface PanelCaso {
  id: number;
  titulo?: string;
  descripcion: string;
  urgencia: string;
  municipio: string;
  barrio: string;
  foto?: string;
  reportadoHace?: string;
  estado?: string;
  nombre?: string;
  tipo?: string;
  solicitadoPor?: string;
  casoId?: number;
  proximoControl?: string;
}

export function getPanelBadgeClass(urgencia: string): string {
  const urgenciaLower = (urgencia || '').toLowerCase();
  if (urgenciaLower === 'critica' || urgenciaLower === 'emergencia') {
    return 'badge-emergencia';
  }
  if (urgenciaLower === 'alta' || urgenciaLower === 'extraviado') {
    return 'badge-alta';
  }
  return 'badge-atencion';
}

export function getPanelBadgeLabel(urgencia: string): string {
  const urgenciaLower = (urgencia || '').toLowerCase();
  if (urgenciaLower === 'critica' || urgenciaLower === 'emergencia') {
    return 'Emergencia';
  }
  if (urgenciaLower === 'alta' || urgenciaLower === 'extraviado') {
    return 'Extraviado';
  }
  return 'En atención';
}

export function scrollToDashboardPanel(): void {
  document.getElementById('mi-panel')?.scrollIntoView({ behavior: 'smooth' });
}
