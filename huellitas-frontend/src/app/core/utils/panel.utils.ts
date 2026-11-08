import { getUrgencyBadgeClass, getUrgencyBadgeLabel } from './caso-display.utils';

export interface PanelCaso {
  id: number;
  titulo?: string;
  descripcion: string;
  urgencia: string;
  municipio: string;
  barrio: string;
  foto?: string;
  fotoPrincipal?: string;
  reportadoHace?: string;
  estado?: string;
  nombre?: string;
  tipo?: string;
  solicitadoPor?: string;
  casoId?: number;
  proximoControl?: string;
}

export function getPanelBadgeClass(urgencia: string): string {
  if (urgencia?.toUpperCase() === 'EMERGENCIA') {
    return 'badge-emergencia';
  }
  return getUrgencyBadgeClass(urgencia);
}

export function getPanelBadgeLabel(urgencia: string): string {
  if (urgencia?.toUpperCase() === 'EMERGENCIA') {
    return 'Emergencia';
  }
  return getUrgencyBadgeLabel(urgencia);
}

export function scrollToDashboardPanel(): void {
  document.getElementById('mi-panel')?.scrollIntoView({ behavior: 'smooth' });
}

/**
 * Default filter for the feed/panel list of cases
 */
export const FEED_PANEL = {
  page: 0,
  size: 10,
  state: 'OPEN',
};
