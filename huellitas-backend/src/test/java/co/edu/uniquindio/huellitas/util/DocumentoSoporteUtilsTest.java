package co.edu.uniquindio.huellitas.util;

import co.edu.uniquindio.huellitas.entity.DocumentoSoporte;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentoSoporteUtilsTest {

    @Test
    void attachesPdfAndSanitizesItsOriginalPath() {
        var organizacion = new Organizacion();
        byte[] contenido = "%PDF-1.4".getBytes();
        var archivo = new MockMultipartFile(
                "documento", "C:\\documentos\\soporte final.pdf", "text/plain", contenido);

        DocumentoSoporteUtils.adjuntar(organizacion, archivo);

        assertDocumento(organizacion, "soporte_final.pdf", "application/pdf", contenido);
    }

    @Test
    void detectsPngAndJpegAndUsesSafeFallbackNames() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

        var pngOrganizacion = new Organizacion();
        DocumentoSoporteUtils.adjuntar(
                pngOrganizacion, new MockMultipartFile("documento", "", "image/jpeg", png));
        assertDocumento(pngOrganizacion, "documento.png", "image/png", png);

        var jpegOrganizacion = new Organizacion();
        DocumentoSoporteUtils.adjuntar(
                jpegOrganizacion, new MockMultipartFile("documento", "..", "image/png", jpeg));
        assertDocumento(jpegOrganizacion, "documento.jpg", "image/jpeg", jpeg);
    }

    @Test
    void rejectsFilesWithAnyIncorrectSignatureByte() {
        byte[][] signatures = {
                "%PDF-".getBytes(),
                {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A},
                {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        };

        for (byte[] signature : signatures) {
            for (int index = 0; index < signature.length; index++) {
                byte[] invalid = signature.clone();
                invalid[index] ^= 0x01;
                var archivo = new MockMultipartFile("documento", "archivo.bin", "application/octet-stream", invalid);

                assertThrows(
                        IllegalArgumentException.class,
                        () -> DocumentoSoporteUtils.adjuntar(new Organizacion(), archivo));
            }
        }
    }

    @Test
    void usesPdfFallbackForNullNamesAndSanitizesDotNames() throws IOException {
        byte[] pdf = "%PDF-1.4".getBytes();
        var pdfOrganizacion = new Organizacion();
        MultipartFile archivoSinNombre = mock(MultipartFile.class);
        when(archivoSinNombre.isEmpty()).thenReturn(false);
        when(archivoSinNombre.getSize()).thenReturn((long) pdf.length);
        when(archivoSinNombre.getBytes()).thenReturn(pdf);
        when(archivoSinNombre.getOriginalFilename()).thenReturn(null);
        DocumentoSoporteUtils.adjuntar(pdfOrganizacion, archivoSinNombre);
        assertDocumento(pdfOrganizacion, "documento.pdf", "application/pdf", pdf);

        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        var pngOrganizacion = new Organizacion();
        DocumentoSoporteUtils.adjuntar(
                pngOrganizacion, new MockMultipartFile("documento", ".", "image/png", png));
        assertDocumento(pngOrganizacion, "documento.png", "image/png", png);
    }

    @Test
    void keepsOnlyTheLast255CharactersOfLongFileNames() {
        byte[] contenido = "%PDF-1.4".getBytes();
        String nombre = "a".repeat(260) + ".pdf";
        var organizacion = new Organizacion();

        DocumentoSoporteUtils.adjuntar(
                organizacion, new MockMultipartFile("documento", nombre, "application/pdf", contenido));

        assertEquals(255, organizacion.getDocumentoSoporteNombre().length());
        assertEquals(nombre.substring(nombre.length() - 255), organizacion.getDocumentoSoporteNombre());
    }

    @Test
    void rejectsMissingEmptyOversizedAndUnsupportedFiles() {
        assertThrows(IllegalArgumentException.class, () -> DocumentoSoporteUtils.adjuntar(new Organizacion(), null));
        assertThrows(IllegalArgumentException.class, () -> DocumentoSoporteUtils.adjuntar(
                new Organizacion(), new MockMultipartFile("documento", "vacio.pdf", "application/pdf", new byte[0])));
        assertThrows(IllegalArgumentException.class, () -> DocumentoSoporteUtils.adjuntar(
                new Organizacion(),
                new MockMultipartFile("documento", "grande.pdf", "application/pdf", new byte[5 * 1024 * 1024 + 1])));
        assertThrows(IllegalArgumentException.class, () -> DocumentoSoporteUtils.adjuntar(
                new Organizacion(), new MockMultipartFile("documento", "invalido.pdf", "application/pdf", new byte[]{1})));
    }

    @Test
    void wrapsIoErrorsWhenReadingFileContents() throws IOException {
        MultipartFile archivo = mock(MultipartFile.class);
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getSize()).thenReturn(1L);
        when(archivo.getBytes()).thenThrow(new IOException("read failed"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DocumentoSoporteUtils.adjuntar(new Organizacion(), archivo));

        assertEquals("No fue posible leer el documento de soporte.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    private static void assertDocumento(
            Organizacion organizacion, String nombre, String tipo, byte[] contenido) {
        DocumentoSoporte documento = organizacion.getDocumentoSoporte();
        assertNotNull(documento);
        assertEquals(nombre, documento.getNombreArchivo());
        assertEquals(tipo, documento.getTipoContenido());
        assertArrayEquals(contenido, documento.getContenido());
        assertEquals(nombre, organizacion.getDocumentoSoporteNombre());
        assertEquals(tipo, organizacion.getDocumentoSoporteContentType());
    }
}
