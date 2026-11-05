package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import co.edu.uniquindio.huellitas.entity.Notificacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.NotificacionMapper;
import co.edu.uniquindio.huellitas.repository.NotificacionRepository;
import co.edu.uniquindio.huellitas.service.impl.NotificacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para NotificacionService - Subtarea 13.8
 * HU-13: Contador de notificaciones no leídas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionService - Pruebas Unitarias")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Spy
    private NotificacionMapper notificacionMapper = new NotificacionMapper();

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    private Usuario usuarioMock;
    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .correo("test@huellitas.com")
                .nombre("Test User")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();

        notificacionMock = Notificacion.builder()
                .id(1L)
                .usuario(usuarioMock)
                .mensaje("Notificación de prueba")
                .leida(false)
                .fecha(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Debe listar todas las notificaciones del usuario")
    void debeListarTodasLasNotificaciones() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Notificacion> page = new PageImpl<>(List.of(notificacionMock));
        when(notificacionRepository.findByUsuarioCorreoOrderByFechaDesc("test@huellitas.com", pageable))
                .thenReturn(page);

        // Act
        Page<NotificacionDTO> result = notificacionService.listarMisNotificaciones("test@huellitas.com", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe filtrar solo notificaciones no leídas")
    void debeFiltrarNoLeidas() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Notificacion> page = new PageImpl<>(List.of(notificacionMock));
        when(notificacionRepository.findByUsuarioCorreoAndLeidaOrderByFechaDesc("test@huellitas.com", false, pageable))
                .thenReturn(page);

        // Act
        Page<NotificacionDTO> result = notificacionService.listarMisNotificaciones("test@huellitas.com", true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificacionRepository).findByUsuarioCorreoAndLeidaOrderByFechaDesc(eq("test@huellitas.com"), eq(false), any());
    }

    @Test
    @DisplayName("Debe marcar notificación como leída")
    void debeMarcarNotificacionLeida() {
        // Arrange
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionMock));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionMock);

        // Act
        NotificacionDTO result = notificacionService.marcarLeida(1L, "test@huellitas.com");

        // Assert
        assertNotNull(result);
        assertTrue(notificacionMock.isLeida());
        verify(notificacionRepository).save(notificacionMock);
    }

    @Test
    @DisplayName("Debe rechazar si notificación no pertenece al usuario")
    void debeRechazarNotificacionAjena() {
        // Arrange
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionMock));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            notificacionService.marcarLeida(1L, "otro@huellitas.com");
        });

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si notificación no existe")
    void debeLanzarExcepcionSiNotificacionNoExiste() {
        // Arrange
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            notificacionService.marcarLeida(999L, "test@huellitas.com");
        });
    }

    @Test
    @DisplayName("13.8: Debe contar notificaciones no leídas")
    void debeContarNoLeidas() {
        // Arrange
        when(notificacionRepository.countByUsuarioCorreoAndLeida("test@huellitas.com", false))
                .thenReturn(5L);

        // Act
        long count = notificacionService.contarNoLeidas("test@huellitas.com");

        // Assert
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("13.8: Debe retornar cero si no hay notificaciones no leídas")
    void debeRetornarCeroSiNoHayNoLeidas() {
        // Arrange
        when(notificacionRepository.countByUsuarioCorreoAndLeida("test@huellitas.com", false))
                .thenReturn(0L);

        // Act
        long count = notificacionService.contarNoLeidas("test@huellitas.com");

        // Assert
        assertEquals(0L, count);
    }
}