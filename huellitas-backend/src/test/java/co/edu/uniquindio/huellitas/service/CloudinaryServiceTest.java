package co.edu.uniquindio.huellitas.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.FotoRepository;
import co.edu.uniquindio.huellitas.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CloudinaryService (Worker W-06).
 * Valida reglas de archivo, optimización, carga a Cloudinary, persistencia de Foto y limpieza compensatoria.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService - Pruebas Unitarias (W-06)")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private CasoRepository casoRepository;

    @Mock
    private FotoRepository fotoRepository;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    private MultipartFile archivoMock;
    private Caso casoMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cloudinaryService, "cloudName", "test-cloud");
        ReflectionTestUtils.setField(cloudinaryService, "apiKey", "test-key");
        ReflectionTestUtils.setField(cloudinaryService, "apiSecret", "test-secret");

        archivoMock = new MockMultipartFile(
                "archivo",
                "test-image.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        casoMock = Caso.builder()
                .id(1L)
                .estado(EstadoCaso.REGISTRADO)
                .fotos(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Debe subir imagen en modo fallback cuando no hay credenciales reales")
    void debeSubirImagenEnModoFallback() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, 1L);

        assertNotNull(resultado);
        assertNotNull(resultado.url());
        assertNotNull(resultado.publicId());
        assertTrue(resultado.url().contains("res.cloudinary.com"));
        assertTrue(resultado.publicId().startsWith("huellitas_"));

        verify(fotoRepository, times(1)).save(any(Foto.class));
        verify(casoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe marcar la primera foto como principal")
    void debeMarcaPrimeraFotoComoPrincipal() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        ArgumentCaptor<Foto> fotoCaptor = ArgumentCaptor.forClass(Foto.class);
        when(fotoRepository.save(fotoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        cloudinaryService.subirImagen(archivoMock, 1L);

        Foto fotoGuardada = fotoCaptor.getValue();
        assertTrue(fotoGuardada.isPrincipal(), "La primera foto debe ser principal");
        assertEquals(casoMock, fotoGuardada.getCaso());
    }

    @Test
    @DisplayName("Debe marcar foto como NO principal si ya existen otras fotos")
    void noDebeMarcaComoPrincipalSiYaExistenFotos() {
        Foto fotoExistente = Foto.builder()
                .id(100L)
                .principal(true)
                .url("https://existing-url.com")
                .build();
        casoMock.getFotos().add(fotoExistente);

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.existsByCasoId(anyLong())).thenReturn(true);

        ArgumentCaptor<Foto> fotoCaptor = ArgumentCaptor.forClass(Foto.class);
        when(fotoRepository.save(fotoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        cloudinaryService.subirImagen(archivoMock, 1L);

        verify(fotoRepository, times(1)).existsByCasoId(1L);
        verify(fotoRepository, times(1)).save(any(Foto.class));
        Foto fotoGuardada = fotoCaptor.getValue();
        assertFalse(fotoGuardada.isPrincipal(), "No debe ser principal si ya hay fotos");
    }

    @Test
    @DisplayName("Debe subir imagen sin asociar a caso cuando casoId es null")
    void debeSubirImagenSinCaso() {
        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, null);

        assertNotNull(resultado);
        assertNotNull(resultado.url());
        assertNotNull(resultado.publicId());

        verify(fotoRepository, never()).save(any(Foto.class));
        verify(casoRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando caso no existe")
    void debeLanzarExcepcionCuandoCasoNoExiste() {
        when(casoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            cloudinaryService.subirImagen(archivoMock, 999L);
        });

        verify(fotoRepository, never()).save(any(Foto.class));
    }

    @Test
    @DisplayName("Debe rechazar archivo nulo con IllegalArgumentException")
    void debeRechazarArchivoNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.subirImagen(null, 1L);
        });
        assertTrue(ex.getMessage().contains("obligatorio"));
    }

    @Test
    @DisplayName("Debe rechazar archivo vacío con IllegalArgumentException")
    void debeRechazarArchivoVacio() {
        MultipartFile archivoVacio = new MockMultipartFile(
                "archivo",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.subirImagen(archivoVacio, 1L);
        });
        assertTrue(ex.getMessage().contains("no puede estar vacío"));
    }

    @Test
    @DisplayName("Debe rechazar formatos no permitidos")
    void debeRechazarFormatoNoPermitido() {
        MultipartFile archivoPdf = new MockMultipartFile(
                "archivo",
                "documento.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.subirImagen(archivoPdf, 1L);
        });
        assertTrue(ex.getMessage().contains("Formato de imagen no permitido"));
    }

    @Test
    @DisplayName("Debe rechazar archivos que excedan el límite de 5 MB")
    void debeRechazarArchivoQueSuperaTamanoMaximo() {
        byte[] grandesBytes = new byte[(int) CloudinaryServiceImpl.MAX_FILE_SIZE_BYTES + 1];
        MultipartFile archivoPesado = new MockMultipartFile(
                "archivo",
                "pesada.jpg",
                "image/jpeg",
                grandesBytes
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.subirImagen(archivoPesado, 1L);
        });
        assertTrue(ex.getMessage().contains("excede el tamaño máximo permitido"));
    }

    @Test
    @DisplayName("Debe generar publicId único para cada subida")
    void debeGenerarPublicIdUnico() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImagenResponseDTO resultado1 = cloudinaryService.subirImagen(archivoMock, 1L);
        ImagenResponseDTO resultado2 = cloudinaryService.subirImagen(archivoMock, 1L);

        assertNotEquals(resultado1.publicId(), resultado2.publicId(),
                "Cada subida debe generar un publicId único");
    }

    @Test
    @DisplayName("Debe construir URL válida de Cloudinary")
    void debeConstruirUrlValidaCloudinary() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, 1L);

        assertTrue(resultado.url().startsWith("https://res.cloudinary.com/"));
        assertTrue(resultado.url().contains("/image/upload/"));
        assertTrue(resultado.url().endsWith(".jpg"));
    }

    @Test
    @DisplayName("Carga real: invoca uploader de Cloudinary con opciones de optimización")
    void debeSubirACloudinaryConOptimizacionCuandoHayCredenciales() throws Exception {
        ReflectionTestUtils.setField(cloudinaryService, "cloudName", "prod-cloud");

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/prod-cloud/image/upload/v1/huellitas/casos/animal.png");
        uploadResult.put("public_id", "huellitas/casos/animal_1");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, null);

        assertEquals("https://res.cloudinary.com/prod-cloud/image/upload/v1/huellitas/casos/animal.png", resultado.url());
        assertEquals("huellitas/casos/animal_1", resultado.publicId());

        verify(uploader, times(1)).upload(eq(archivoMock.getBytes()), anyMap());
    }

    @Test
    @DisplayName("Fallo de Cloudinary: lanza IllegalStateException y no persiste")
    void debeLanzarExcepcionSiFallaCloudinary() throws Exception {
        ReflectionTestUtils.setField(cloudinaryService, "cloudName", "prod-cloud");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Error de conexión"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            cloudinaryService.subirImagen(archivoMock, null);
        });

        assertTrue(ex.getMessage().contains("No fue posible procesar y optimizar"));
        verifyNoInteractions(fotoRepository);
    }

    @Test
    @DisplayName("Limpieza compensatoria: si falla persistencia en BD, destruye imagen en Cloudinary")
    void debeEjecutarLimpiezaCompensatoriaSiFallaPersistencia() throws Exception {
        ReflectionTestUtils.setField(cloudinaryService, "cloudName", "prod-cloud");

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/prod-cloud/image/upload/v1/huerfana.jpg");
        uploadResult.put("public_id", "huellitas/casos/huerfana_123");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);
        when(fotoRepository.existsByCasoId(1L)).thenReturn(false);
        when(fotoRepository.save(any(Foto.class))).thenThrow(new RuntimeException("Error en base de datos"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            cloudinaryService.subirImagen(archivoMock, 1L);
        });

        assertEquals("Error en base de datos", ex.getMessage());
        verify(uploader, times(1)).destroy(eq("huellitas/casos/huerfana_123"), anyMap());
    }

    @Test
    @DisplayName("Debe eliminar las fotos del caso de Cloudinary")
    void debeEliminarFotosDeCloudinary() throws Exception {
        ReflectionTestUtils.setField(cloudinaryService, "cloudName", "prod-cloud");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq("huellitas/casos/animal_1"), anyMap()))
                .thenReturn(Map.of("result", "ok"));
        Foto foto = Foto.builder().publicId("huellitas/casos/animal_1").build();

        cloudinaryService.eliminarFotos(List.of(foto));

        verify(uploader).destroy(eq("huellitas/casos/animal_1"), anyMap());
    }
}
