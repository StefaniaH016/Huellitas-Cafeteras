package co.edu.uniquindio.huellitas.dto.verificacion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoSoporteDTOTest {

    @Test
    void comparesContentByValue() {
        var first = new DocumentoSoporteDTO("soporte.pdf", "application/pdf", new byte[]{1, 2, 3});
        var sameContent = new DocumentoSoporteDTO("soporte.pdf", "application/pdf", new byte[]{1, 2, 3});
        var differentContent = new DocumentoSoporteDTO("soporte.pdf", "application/pdf", new byte[]{1, 2, 4});

        assertEquals(first, first);
        assertEquals(first, sameContent);
        assertEquals(first.hashCode(), sameContent.hashCode());
        assertNotEquals(first, differentContent);
        assertNotEquals(first, null);
        assertNotEquals(first, new Object());
    }

    @Test
    void comparesNullableComponents() {
        var empty = new DocumentoSoporteDTO(null, null, null);

        assertEquals(empty, new DocumentoSoporteDTO(null, null, null));
        assertNotEquals(empty, new DocumentoSoporteDTO("soporte.pdf", null, null));
        assertNotEquals(empty, new DocumentoSoporteDTO(null, "application/pdf", null));
        assertNotEquals(empty, new DocumentoSoporteDTO(null, null, new byte[]{1}));
        assertTrue(empty.toString().contains("contenidoLength=null"));
    }

    @Test
    void describesContentWithoutIncludingDocumentBytes() {
        var dto = new DocumentoSoporteDTO("soporte.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertTrue(dto.toString().contains("contenidoLength=3"));
        assertTrue(dto.toString().contains("contenidoHash="));
        assertFalse(dto.toString().contains("contenido=[1, 2, 3]"));
    }
}
