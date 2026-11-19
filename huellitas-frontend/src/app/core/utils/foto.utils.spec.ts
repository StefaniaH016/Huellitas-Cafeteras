import { FOTO_MAX_BYTES, validarFoto, validarLoteFotos } from './foto.utils';

function archivo(tipo: string, bytes = 10): File {
  const file = new File(['x'], 'foto', { type: tipo });
  Object.defineProperty(file, 'size', { value: bytes });
  return file;
}

describe('photo rules', () => {
  it('accepts JPG, PNG and WebP up to 5 MB', () => {
    expect(validarFoto(archivo('image/png'))).toBeNull();
    expect(validarFoto(archivo('image/gif'))).toContain('JPG, PNG o WebP');
    expect(validarFoto(archivo('image/jpeg', FOTO_MAX_BYTES + 1))).toContain('5 MB');
  });

  it('limits a batch to five photos in total', () => {
    expect(validarLoteFotos([archivo('image/png')], 4)).toBeNull();
    expect(validarLoteFotos([archivo('image/png'), archivo('image/png')], 4)).toContain('5 fotos');
    expect(validarLoteFotos([archivo('text/plain')], 0)).toContain('Solo se admiten');
  });
});
