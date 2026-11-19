package co.edu.uniquindio.huellitas.util;

import co.edu.uniquindio.huellitas.entity.DocumentoSoporte;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public final class DocumentoSoporteUtils {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;

    private DocumentoSoporteUtils() {}

    public static void adjuntar(Organizacion organizacion, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar un documento de soporte.");
        }
        if (archivo.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("El documento de soporte no puede superar 5 MB.");
        }

        try {
            byte[] contenido = archivo.getBytes();
            String tipoContenido = detectarTipo(contenido);
            String nombreArchivo = limpiarNombre(archivo.getOriginalFilename(), tipoContenido);
            organizacion.setDocumentoSoporte(DocumentoSoporte.builder()
                    .nombreArchivo(nombreArchivo)
                    .tipoContenido(tipoContenido)
                    .contenido(contenido)
                    .build());
            organizacion.setDocumentoSoporteNombre(nombreArchivo);
            organizacion.setDocumentoSoporteContentType(tipoContenido);
        } catch (IOException e) {
            throw new IllegalArgumentException("No fue posible leer el documento de soporte.", e);
        }
    }

    private static String detectarTipo(byte[] contenido) {
        if (contenido.length >= 5
                && contenido[0] == '%'
                && contenido[1] == 'P'
                && contenido[2] == 'D'
                && contenido[3] == 'F'
                && contenido[4] == '-') {
            return "application/pdf";
        }
        if (contenido.length >= 8
                && (contenido[0] & 0xFF) == 0x89
                && contenido[1] == 'P'
                && contenido[2] == 'N'
                && contenido[3] == 'G'
                && (contenido[4] & 0xFF) == 0x0D
                && (contenido[5] & 0xFF) == 0x0A
                && (contenido[6] & 0xFF) == 0x1A
                && (contenido[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        if (contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF
                && (contenido[1] & 0xFF) == 0xD8
                && (contenido[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        throw new IllegalArgumentException("El soporte debe ser un PDF, JPG o PNG válido.");
    }

    private static String limpiarNombre(String nombreOriginal, String tipoContenido) {
        String base = nombreOriginal == null ? "" : nombreOriginal.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1)
                .replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isBlank() || ".".equals(base) || "..".equals(base)) {
            String extension = switch (tipoContenido) {
                case "application/pdf" -> ".pdf";
                case "image/png" -> ".png";
                default -> ".jpg";
            };
            base = "documento" + extension;
        }
        return base.length() > 255 ? base.substring(base.length() - 255) : base;
    }
}
