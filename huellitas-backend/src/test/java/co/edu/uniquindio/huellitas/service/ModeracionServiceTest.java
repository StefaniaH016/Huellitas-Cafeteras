package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.mapper.ModeracionMapper;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.SeguimientoRepository;
import co.edu.uniquindio.huellitas.service.impl.ModeracionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModeracionServiceTest {

    @Mock
    private CasoRepository casoRepository;
    @Mock
    private SeguimientoRepository seguimientoRepository;
    private ModeracionServiceImpl moderacionService;
    private Caso caso;

    @BeforeEach
    void setUp() {
        moderacionService = new ModeracionServiceImpl(
                casoRepository,
                seguimientoRepository,
                new ModeracionMapper());
        caso = Caso.builder()
                .id(5L)
                .fechaRegistro(Instant.parse("2026-01-01T00:00:00Z"))
                .visible(true)
                .moderacionPendiente(true)
                .build();
    }

    @Test
    void administraLaVisibilidadDelCasoYResuelveLaModeracionPendiente() {
        when(casoRepository.findById(5L)).thenReturn(Optional.of(caso));
        when(casoRepository.save(caso)).thenReturn(caso);

        var response = moderacionService.moderarCaso(
                5L, new DecisionModeracionDTO("OCULTAR", "Contenido duplicado"), "admin@test.com");

        assertEquals("OCULTADO", response.decision());
        assertFalse(caso.isVisible());
        assertFalse(caso.isModeracionPendiente());
        assertEquals("Contenido duplicado", caso.getMotivoModeracion());
        verify(casoRepository).save(caso);
    }

    @Test
    void ocultaAportesConMotivoAdministrativo() {
        Seguimiento aporte = Seguimiento.builder().id(8L).visible(true).build();
        when(seguimientoRepository.findById(8L)).thenReturn(Optional.of(aporte));
        when(seguimientoRepository.save(aporte)).thenReturn(aporte);

        var response = moderacionService.ocultarAporte(8L, "Dato personal", "admin@test.com");

        assertEquals("OCULTADO", response.decision());
        assertFalse(aporte.isVisible());
        assertEquals("Dato personal", aporte.getMotivoOcultacion());
        verify(seguimientoRepository).save(aporte);
    }

    @Test
    void retornaErrorSiElCasoNoExisteAlModerar() {
        when(casoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> moderacionService.moderarCaso(
                404L, new DecisionModeracionDTO("MANTENER", "Revisión administrativa"), "admin@test.com"));
        verify(casoRepository, never()).save(any(Caso.class));
    }
}
