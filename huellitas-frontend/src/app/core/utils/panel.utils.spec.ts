import { getPanelBadgeClass, getPanelBadgeLabel, scrollToDashboardPanel } from './panel.utils';

describe('panel utilities', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it.each([
    ['critica', 'badge-emergencia'],
    ['EMERGENCIA', 'badge-emergencia'],
    ['alta', 'badge-alta'],
    ['extraviado', 'badge-alta'],
    ['media', 'badge-atencion'],
  ])('maps urgency %s to badge class %s', (urgency, expected) => {
    expect(getPanelBadgeClass(urgency)).toBe(expected);
  });

  it.each([
    ['CRITICA', 'Emergencia'],
    ['emergencia', 'Emergencia'],
    ['ALTA', 'Extraviado'],
    ['extraviado', 'Extraviado'],
    ['MEDIA', 'En atención'],
  ])('maps urgency %s to badge label %s', (urgency, expected) => {
    expect(getPanelBadgeLabel(urgency)).toBe(expected);
  });

  it('scrolls to the dashboard panel when it exists', () => {
    const scrollIntoView = vi.fn();
    vi.spyOn(document, 'getElementById').mockReturnValue({
      scrollIntoView,
    } as unknown as HTMLElement);

    scrollToDashboardPanel();

    expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
  });

  it('does nothing when the dashboard panel is absent', () => {
    vi.spyOn(document, 'getElementById').mockReturnValue(null);

    expect(() => scrollToDashboardPanel()).not.toThrow();
  });
});
