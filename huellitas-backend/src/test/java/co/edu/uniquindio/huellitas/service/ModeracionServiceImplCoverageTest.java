package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.ModeracionMapper;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.SeguimientoRepository;
import co.edu.uniquindio.huellitas.service.impl.ModeracionServiceImpl;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModeracionServiceImplCoverageTest {

    @Mock
    private CasoRepository casoRepository;
    @Mock
    private SeguimientoRepository seguimientoRepository;
    @Mock
    private NotificacionWorker notificacionWorker;

    private ModeracionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ModeracionServiceImpl(
                casoRepository, seguimientoRepository, new ModeracionMapper(), notificacionWorker);
    }

    @Test
    void listarCasosReportadosMapeaListaDePendientes() {
        Caso c = Caso.builder()
                .id(7L)
                .motivoModeracion("Denuncia")
                .moderacionPendiente(true)
                .fechaRegistro(Instant.parse("2026-05-01T10:00:00Z"))
                .build();
        when(casoRepository.findByModeracionPendienteTrue()).thenReturn(List.of(c));

        List<CasoModeracionDTO> lista = service.listarCasosReportados();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).casoId()).isEqualTo(7L);
        assertThat(lista.get(0).estadoRevision()).isEqualTo("PENDIENTE");
        verify(casoRepository).findByModeracionPendienteTrue();
    }

    @Test
    void moderarCasoMantenerYOcultarCasoYaOcultoNoNotificanOcultamientoNuevo() {
        Caso casoOculto = Caso.builder().id(8L).visible(false).moderacionPendiente(true).build();
        when(casoRepository.findById(8L)).thenReturn(Optional.of(casoOculto));
        when(casoRepository.save(casoOculto)).thenReturn(casoOculto);

        ModeracionResponseDTO mantener = service.moderarCaso(
                8L, new DecisionModeracionDTO("MANTENER", "Falsa alarma"), "admin@test.com");
        assertThat(mantener.decision()).isEqualTo("MANTENIDO");
        assertThat(casoOculto.isVisible()).isTrue();
        assertThat(casoOculto.isModeracionPendiente()).isFalse();

        casoOculto.setVisible(false);
        ModeracionResponseDTO yaOculto = service.moderarCaso(
                8L, new DecisionModeracionDTO("OCULTAR", "Sigue oculto"), "admin@test.com");
        assertThat(yaOculto.decision()).isEqualTo("OCULTADO");
        verifyNoInteractions(notificacionWorker);
    }

    @Test
    void ocultarAporteCubreNoEncontradoYIdempotenciaCuandoYaOculto() {
        when(seguimientoRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.ocultarAporte(404L, "Spam", "admin@test.com"));

        Seguimiento yaOculto = Seguimiento.builder().id(9L).visible(false).build();
        when(seguimientoRepository.findById(9L)).thenReturn(Optional.of(yaOculto));

        ModeracionResponseDTO resp = service.ocultarAporte(9L, "Spam", "admin@test.com");
        assertThat(resp.decision()).isEqualTo("OCULTADO");
        verify(seguimientoRepository, never()).save(any());
    }

    @Test
    void marcarCasoParaModeracionLanzaNotFoundCuandoNoExiste() {
        when(casoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.marcarCasoParaModeracion(404L, "Reporte comunitario"));
        verify(casoRepository, never()).save(any());
    }
}
