import {
  ESTADO_LABELS,
  TIPO_LABELS,
  esCasoAbierto,
  estadoBadge,
  formatFecha,
  formatFechaHora,
  haceCuanto,
  labelOf,
  labelOptions,
  lugarTexto,
  urgenciaBadge,
} from './caso-display.utils';

it('includes the En adopción case type from the backend enum', () => {
  expect(labelOptions(TIPO_LABELS).map((o) => o.value)).toContain('EN_ADOPCION');
  expect(labelOf(TIPO_LABELS, 'en_adopcion')).toBe('En adopción');
});

it('returns the raw value for unknown labels and empty text for missing ones', () => {
  expect(labelOf(ESTADO_LABELS, 'OTRO')).toBe('OTRO');
  expect(labelOf(ESTADO_LABELS, '')).toBe('');
});

it('maps urgency and state to badge classes with safe fallbacks', () => {
  expect(urgenciaBadge('critica')).toBe('badge-emergencia');
  expect(urgenciaBadge('BAJA')).toBe('badge-media');
  expect(urgenciaBadge('')).toBe('badge-atencion');
  expect(estadoBadge('CERRADO')).toBe('badge-success');
  expect(estadoBadge('X')).toBe('badge-secondary');
});

it('knows which states are still open', () => {
  expect(esCasoAbierto('REGISTRADO')).toBe(true);
  expect(esCasoAbierto('EN_ATENCION')).toBe(true);
  expect(esCasoAbierto('RESUELTO')).toBe(false);
  expect(esCasoAbierto('ARCHIVADO')).toBe(false);
  expect(esCasoAbierto('CERRADO')).toBe(false);
  expect(esCasoAbierto('DERIVADO')).toBe(false);
});

it('formats dates and ignores invalid ones', () => {
  expect(formatFecha('2026-10-01T12:00:00Z')).toMatch(/2026/);
  expect(formatFecha('no es fecha')).toBe('');
  expect(formatFechaHora('')).toBe('');
});

it('describes how long ago something happened', () => {
  const now = new Date();
  const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000).toISOString();
  const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000).toISOString();
  const fiveDaysAgo = new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString();
  const tenDaysAgo = new Date(now.getTime() - 10 * 24 * 60 * 60 * 1000).toISOString();

  expect(haceCuanto(oneHourAgo)).toMatch(/hace/i);
  expect(haceCuanto(oneDayAgo)).toMatch(/hace/i);
  expect(haceCuanto(fiveDaysAgo)).toMatch(/hace/i);
  expect(haceCuanto(tenDaysAgo)).toMatch(/2026|202[0-9]/);
  expect(haceCuanto('')).toBe('');
});

it('joins place parts skipping the empty ones', () => {
  expect(lugarTexto('Armenia', '', undefined)).toBe('Armenia');
});
