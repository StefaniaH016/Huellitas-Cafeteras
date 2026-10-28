import { isPanelRole, normalizeRole } from './role.utils';

describe('role utilities', () => {
  it.each([
    ['ADMINISTRADOR', 'ADMINISTRADOR'],
    ['FUNDACION_REFUGIO', 'FUNDACION_REFUGIO'],
    ['VETERINARIA', 'VETERINARIA'],
    ['CIUDADANO_VOLUNTARIO', 'CIUDADANO_VOLUNTARIO'],
    [' fundacion ', 'FUNDACION_REFUGIO'],
  ])('normalizes %s to %s', (input, expected) => {
    expect(normalizeRole(input)).toBe(expected);
  });

  it('returns null for missing or unknown roles', () => {
    const warning = vi.spyOn(console, 'warn').mockImplementation(() => undefined);

    expect(normalizeRole(null)).toBeNull();
    expect(normalizeRole('UNKNOWN')).toBeNull();
    expect(warning).toHaveBeenCalledOnce();
  });

  it('identifies panel roles but excludes citizens and null', () => {
    expect(isPanelRole('ADMINISTRADOR')).toBe(true);
    expect(isPanelRole('VETERINARIA')).toBe(true);
    expect(isPanelRole('CIUDADANO_VOLUNTARIO')).toBe(false);
    expect(isPanelRole(null)).toBe(false);
  });
});
