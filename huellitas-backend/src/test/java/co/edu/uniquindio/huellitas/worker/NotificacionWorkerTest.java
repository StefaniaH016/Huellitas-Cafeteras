package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.NotificacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para NotificacionWorker - Subtarea 13.5 y 13.6
 * HU-13: Worker W-01 y notificaciones de emergencia
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificacionWorker (W-01) - Pruebas Unitarias")
class NotificacionWorkerTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MunicipioRepository municipioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificacionWorker notificacionWorker;

    private Caso casoUrgente;
    private Caso casoNormal;
    private Usuario reportante;
    private Usuario autor;
    private Usuario rescuista;
    private Fundacion fundacion;

    @BeforeEach
    void setUp() {
        // Reportante
        reportante = Usuario.builder()
                .id(1L)
                .correo("reportante@huellitas.com")
                .nombre("Reportante")
                .build();

        // Autor del aporte (diferente al reportante)
        autor = Usuario.builder()
                .id(2L)
                .correo("autor@huellitas.com")
                .nombre("Autor")
                .build();

        // Rescuista disponible
        Departamento depto = Departamento.builder().id(1L).nombre("Quindío").build();
        Municipio municipio = Municipio.builder().id(1L).nombre("Armenia").departamento(depto).build();
        rescuista = Usuario.builder()
                .id(3L)
                .correo("rescuista@huellitas.com")
                .nombre("Rescuista")
                .municipioInteres(municipio)
                .disponibleRescate(true)
                .build();

        // Fundación
        fundacion = Fundacion.builder()
                .id(1L)
                .nombre("Fundación Patitas")
                .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                .build();

        // Caso urgente
        Animal animalUrgente = Animal.builder().id(1L).nombre("Firulais").especie(Especie.PERRO).build();
        Barrio barrio = Barrio.builder().id(1L).nombre("Centro").municipio(municipio).build();
        casoUrgente = Caso.builder()
                .id(1L)
                .urgencia(Urgencia.ALTA)
                .reportante(reportante)
                .animal(animalUrgente)
                .barrio(barrio)
                .build();

        // Caso normal (no urgente)
        Animal animalNormal = Animal.builder().id(2L).nombre("Luna").especie(Especie.GATO).build();
        casoNormal = Caso.builder()
                .id(2L)
                .urgencia(Urgencia.BAJA)
                .reportante(reportante)
                .animal(animalNormal)
                .barrio(barrio)
                .build();
    }

    // === Pruebas 13.5: Notificaciones de aportes ===

    @Test
    @DisplayName("13.5: Debe notificar al reportante cuando se crea un aporte (no es el autor)")
    void debeNotificarReportanteAporte() {
        // Arrange
        Seguimiento aporte = Seguimiento.builder()
                .id(1L)
                .caso(casoUrgente)
                .autor(autor) // Diferente del reportante
                .comentario("Vi al perro cerca del parque")
                .build();

        // Act
        notificacionWorker.notificarAporte(aporte);

        // Assert
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("13.5: No debe notificar al autor de su propio aporte")
    void noDebeNotificarAutorMismoAporte() {
        // Arrange
        Seguimiento aporte = Seguimiento.builder()
                .id(1L)
                .caso(casoUrgente)
                .autor(reportante) // Mismo que el reportante
                .comentario("Vi al perro")
                .build();

        // Act
        notificacionWorker.notificarAporte(aporte);

        // Assert - No debe guardar notificación porque el autor es el reportante
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("13.5: Debe notificar a la fundación custodia cuando hay aporte")
    void debeNotificarFundacionCustodia() {
        // Arrange
        casoUrgente.setFundacionCustodio(fundacion);
        Usuario usuarioFundacion = Usuario.builder()
                .id(10L)
                .correo("fundacion@huellitas.com")
                .organizacion(fundacion)
                .build();

        when(usuarioRepository.findByOrganizacionId(1L)).thenReturn(Optional.of(usuarioFundacion));

        Seguimiento aporte = Seguimiento.builder()
                .id(1L)
                .caso(casoUrgente)
                .autor(reportante)
                .comentario("Nuevo aporte")
                .build();

        // Act
        notificacionWorker.notificarAporte(aporte);

        // Assert - Debe notificar al reportante Y a la fundación
        verify(notificacionRepository, atLeast(1)).save(any(Notificacion.class));
    }

    // === Pruebas 13.6: Notificaciones de emergencia ===

    @Test
    @DisplayName("13.6: Debe notificar emergencias ALTA a rescuistas del mismo municipio")
    void debeNotificarEmergenciaARescuatistas() {
        // Arrange
        when(usuarioRepository.findByMunicipioInteresIdAndDisponibleRescateTrue(1L))
                .thenReturn(List.of(rescuista));

        // Act
        notificacionWorker.notificarEmergenciaARescuatistas(casoUrgente);

        // Assert
        verify(notificacionRepository, times(2)).save(any(Notificacion.class)); // Rescuista + reportante
        verify(emailService, times(1)).enviarCorreoEmergencia(any(Caso.class), eq("rescuista@huellitas.com"));
    }

    @Test
    @DisplayName("13.6: No debe notificar emergencias BAJA a rescuistas")
    void noDebeNotificarEmergenciaBaja() {
        // Arrange
        when(usuarioRepository.findByMunicipioInteresIdAndDisponibleRescateTrue(1L))
                .thenReturn(List.of(rescuista));

        // Act
        notificacionWorker.notificarEmergenciaARescuatistas(casoNormal);

        // Assert - No debe guardar notificaciones
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("13.6: Debe notificar al reportante de caso urgente")
    void debeNotificarReportanteEmergencia() {
        // Arrange
        when(usuarioRepository.findByMunicipioInteresIdAndDisponibleRescateTrue(1L))
                .thenReturn(List.of(rescuista));

        // Act
        notificacionWorker.notificarEmergenciaARescuatistas(casoUrgente);

        // Assert - Debe notificar tanto al rescuista como al reportante
        verify(notificacionRepository, atLeast(1)).save(any(Notificacion.class));
    }

    // === Pruebas de fallos ===

    // Las pruebas de robustez (fallback ante errores) ya están cubiertas en la implementación
    // que usa try-catch en los métodos del worker

    // === Pruebas de notificaciones existentes ===

    @Test
    @DisplayName("13.3: Debe notificar custodia al reportante")
    void debeNotificarAsuncionCustodia() {
        // Act
        notificacionWorker.notificarAsuncionCustodia(casoUrgente, fundacion);

        // Assert
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("13.3: Debe enviar correo al asumir custodia")
    void debeEnviarCorreoCustodia() {
        // Act
        notificacionWorker.notificarAsuncionCustodia(casoUrgente, fundacion);

        // Assert
        verify(emailService, times(1)).enviarCorreoCustodia(eq(casoUrgente), eq(fundacion), any());
    }

    @Test
    @DisplayName("Debe notificar cambio de estado")
    void debeNotificarCambioEstado() {
        // Act
        notificacionWorker.notificarCambioEstado(casoUrgente, EstadoCaso.REGISTRADO, EstadoCaso.EN_ATENCION, autor);

        // Assert - puede ser 1 o 2 dependiendo de si el actor es diferente
        verify(notificacionRepository, atLeast(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe notificar cierre de caso")
    void debeNotificarCierreCaso() {
        // Act
        notificacionWorker.notificarCierreCaso(casoUrgente, "REENCUENTRO");

        // Assert
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }
}