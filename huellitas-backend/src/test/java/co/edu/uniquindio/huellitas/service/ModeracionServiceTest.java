package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.moderacion.ReportarCasoDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.mapper.ModeracionMapper;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.SeguimientoRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.impl.ModeracionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModeracionServiceTest {

    @Mock
    private CasoRepository casoRepository;
    @Mock
    private SeguimientoRepository seguimientoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private ModeracionServiceImpl moderacionService;
    private Usuario reportante;
    private Usuario propietario;
    private Caso caso;

    @BeforeEach
    void setUp() {
        moderacionService = new ModeracionServiceImpl(
                casoRepository,
                seguimientoRepository,
                usuarioRepository,
                new ModeracionMapper());
        reportante = Usuario.builder()
                .id(2L)
                .correo("ciudadano@test.com")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();
        propietario = Usuario.builder()
                .id(1L)
                .correo("propietario@test.com")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();
        caso = Caso.builder()
                .id(5L)
                .reportante(propietario)
                .visible(true)
                .build();
    }

    @Test
    void permiteReportarCasoDeOtroCiudadanoYLoMarcaPendiente() {
        when(casoRepository.findById(5L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("ciudadano@test.com")).thenReturn(Optional.of(reportante));
        when(casoRepository.save(caso)).thenReturn(caso);

        var response = moderacionService.reportarCaso(
                5L,
                new ReportarCasoDTO("Contenido posiblemente duplicado"),
                "CIUDADANO@TEST.COM ");

        assertEquals("REPORTADO", response.decision());
        assertEquals("Contenido posiblemente duplicado", caso.getMotivoModeracion());
        assertTrue(caso.isModeracionPendiente());
        verify(casoRepository).save(caso);
    }

    @Test
    void impideReportarUnCasoPropio() {
        when(casoRepository.findById(5L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("propietario@test.com")).thenReturn(Optional.of(propietario));

        assertThrows(AccessDeniedException.class, () -> moderacionService.reportarCaso(
                5L,
                new ReportarCasoDTO("Contenido posiblemente duplicado"),
                "propietario@test.com"));

        verify(casoRepository, never()).save(any(Caso.class));
    }

    @Test
    void conservaElPrimerReporteSiYaEstaPendiente() {
        caso.setModeracionPendiente(true);
        caso.setMotivoModeracion("Primer reporte");
        when(casoRepository.findById(5L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("ciudadano@test.com")).thenReturn(Optional.of(reportante));

        var response = moderacionService.reportarCaso(
                5L,
                new ReportarCasoDTO("Otro motivo de reporte válido"),
                "ciudadano@test.com");

        assertEquals("YA_REPORTADO", response.decision());
        assertEquals("Primer reporte", caso.getMotivoModeracion());
        verify(casoRepository, never()).save(any(Caso.class));
    }
}
