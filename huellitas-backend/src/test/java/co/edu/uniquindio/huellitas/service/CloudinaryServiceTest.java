package co.edu.uniquindio.huellitas.service;

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

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CloudinaryService (Worker W-06).
 * HU-10 Subtarea 1: Integración del Worker W-06 para compresión/subida de imágenes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService - Pruebas Unitarias (W-06)")
class CloudinaryServiceTest {

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
        // Configurar modo test (sin credenciales reales de Cloudinary)
        ReflectionTestUtils.setField(cloudinaryService, "cloudName", "test-cloud");
        ReflectionTestUtils.setField(cloudinaryService, "apiKey", "test-key");
        ReflectionTestUtils.setField(cloudinaryService, "apiSecret", "test-secret");

        // Crear archivo mock
        archivoMock = new MockMultipartFile(
                "archivo",
                "test-image.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        // Crear caso mock
        casoMock = Caso.builder()
                .id(1L)
                .estado(EstadoCaso.REGISTRADO)
                .fotos(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Debe subir imagen en modo fallback cuando no hay credenciales reales")
    void debeSubirImagenEnModoFallback() {
        // Arrange
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, 1L);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.url());
        assertNotNull(resultado.publicId());
        assertTrue(resultado.url().contains("res.cloudinary.com"));
        assertTrue(resultado.publicId().startsWith("huellitas_"));

        // Verificar que se guardó en BD
        verify(fotoRepository, times(1)).save(any(Foto.class));
        verify(casoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe marcar la primera foto como principal")
    void debeMarcaPrimeraFotoComoPrincipal() {
        // Arrange
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        ArgumentCaptor<Foto> fotoCaptor = ArgumentCaptor.forClass(Foto.class);
        when(fotoRepository.save(fotoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cloudinaryService.subirImagen(archivoMock, 1L);

        // Assert
        Foto fotoGuardada = fotoCaptor.getValue();
        assertTrue(fotoGuardada.isPrincipal(), "La primera foto debe ser principal");
        assertEquals(casoMock, fotoGuardada.getCaso());
    }

    @Test
    @DisplayName("Debe marcar foto como NO principal si ya existen otras fotos")
    void noDebeMarcaComoPrincipalSiYaExistenFotos() {
        // Arrange
        Foto fotoExistente = Foto.builder()
                .id(100L)
                .principal(true)
                .url("https://existing-url.com")
                .build();
        casoMock.getFotos().add(fotoExistente);

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));

        // usar anyLong() para evitar problemas de matching
        when(fotoRepository.existsByCasoId(anyLong())).thenReturn(true);

        ArgumentCaptor<Foto> fotoCaptor = ArgumentCaptor.forClass(Foto.class);
        when(fotoRepository.save(fotoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cloudinaryService.subirImagen(archivoMock, 1L);

        // Assert: verificar que existsByCasoId fue llamado
        verify(fotoRepository, times(1)).existsByCasoId(1L);

        // verificar save y la propiedad principal
        verify(fotoRepository, times(1)).save(any(Foto.class));
        Foto fotoGuardada = fotoCaptor.getValue();
        assertFalse(fotoGuardada.isPrincipal(), "No debe ser principal si ya hay fotos");

    }

    @Test
    @DisplayName("Debe subir imagen sin asociar a caso cuando casoId es null")
    void debeSubirImagenSinCaso() {
        // Arrange - sin caso

        // Act
        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, null);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.url());
        assertNotNull(resultado.publicId());

        // NO debe intentar guardar foto en BD
        verify(fotoRepository, never()).save(any(Foto.class));
        verify(casoRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando caso no existe")
    void debeLanzarExcepcionCuandoCasoNoExiste() {
        // Arrange
        when(casoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cloudinaryService.subirImagen(archivoMock, 999L);
        });

        verify(fotoRepository, never()).save(any(Foto.class));
    }

    @Test
    @DisplayName("Debe manejar archivo null usando fallback")
    void debeManejarArchivoNull() {
        // Arrange
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ImagenResponseDTO resultado = cloudinaryService.subirImagen(null, 1L);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.url());
        assertTrue(resultado.url().contains("res.cloudinary.com"));
        verify(fotoRepository, times(1)).save(any(Foto.class));
    }

    @Test
    @DisplayName("Debe manejar archivo vacío usando fallback")
    void debeManejarArchivoVacio() {
        // Arrange
        MultipartFile archivoVacio = new MockMultipartFile(
                "archivo",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoVacio, 1L);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.url());
        verify(fotoRepository, times(1)).save(any(Foto.class));
    }

    @Test
    @DisplayName("Debe generar publicId único para cada subida")
    void debeGenerarPublicIdUnico() {
        // Arrange
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ImagenResponseDTO resultado1 = cloudinaryService.subirImagen(archivoMock, 1L);
        ImagenResponseDTO resultado2 = cloudinaryService.subirImagen(archivoMock, 1L);

        // Assert
        assertNotEquals(resultado1.publicId(), resultado2.publicId(),
                "Cada subida debe generar un publicId único");
    }

    @Test
    @DisplayName("Debe construir URL válida de Cloudinary")
    void debeConstruirUrlValidaCloudinary() {
        // Arrange
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ImagenResponseDTO resultado = cloudinaryService.subirImagen(archivoMock, 1L);

        // Assert
        assertTrue(resultado.url().startsWith("https://res.cloudinary.com/"));
        assertTrue(resultado.url().contains("/image/upload/"));
        assertTrue(resultado.url().endsWith(".jpg"));
    }
}
