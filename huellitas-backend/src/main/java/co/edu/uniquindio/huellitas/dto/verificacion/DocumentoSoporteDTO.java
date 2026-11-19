package co.edu.uniquindio.huellitas.dto.verificacion;

import java.util.Arrays;
import java.util.Objects;

public record DocumentoSoporteDTO(
        String nombreArchivo,
        String tipoContenido,
        byte[] contenido
) {
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DocumentoSoporteDTO that)) {
            return false;
        }
        return Objects.equals(nombreArchivo, that.nombreArchivo)
                && Objects.equals(tipoContenido, that.tipoContenido)
                && Arrays.equals(contenido, that.contenido);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(nombreArchivo, tipoContenido);
        return 31 * result + Arrays.hashCode(contenido);
    }

    @Override
    public String toString() {
        return "DocumentoSoporteDTO[nombreArchivo=" + nombreArchivo
                + ", tipoContenido=" + tipoContenido
                + ", contenidoLength=" + (contenido == null ? "null" : contenido.length)
                + ", contenidoHash=" + Arrays.hashCode(contenido) + "]";
    }
}
