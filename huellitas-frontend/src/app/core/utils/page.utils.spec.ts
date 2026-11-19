import { compactParams, pageContent, pageTotal } from './page.utils';

describe('page utilities', () => {
  const page = { content: [1, 2], totalElements: 9, totalPages: 5, number: 0, size: 2 };

  it('reads the content and total of a page or a plain list', () => {
    expect(pageContent(page)).toEqual([1, 2]);
    expect(pageTotal(page)).toBe(9);
    expect(pageContent([3])).toEqual([3]);
    expect(pageTotal([3, 4])).toBe(2);
    expect(pageContent(null)).toEqual([]);
    expect(pageTotal(undefined)).toBe(0);
  });

  it('drops empty filters', () => {
    expect(compactParams({ a: 1, b: null, c: '', d: undefined, e: false })).toEqual({
      a: 1,
      e: false,
    });
  });
});
