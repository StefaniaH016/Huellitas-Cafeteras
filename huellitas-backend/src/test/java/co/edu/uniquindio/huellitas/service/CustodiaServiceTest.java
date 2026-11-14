package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.reporte.CasoResponseDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CustodiaDTO;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.AporteMapper;
import co.edu.uniquindio.huellitas.mapper.CasoMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.impl.CasoServiceImpl;
import co.edu.uniquindio.huellitas.worker.FiltroWorker;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para Gestión de Custodia - Subtarea 13.3
 * HU-13: Tabla de decisión rol × fundación verificada × custodia
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CasoService - Pruebas de Custodia (HU-13)")
class CustodiaServiceTest {

    @Mock
    private CasoRepository casoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BarrioRepository barrioRepository;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    @Mock
    private SeguimientoRepository seguimientoRepository;
    @Mock
    private CierreCasoRepository cierreCasoRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private NotificacionWorker notificacionWorker;
    @Mock
    private FiltroWorker filtroWorker;
    @Mock
    private EmailService emailService;

    @Spy
    private CasoMapper casoMapper = new CasoMapper();
    @Spy
    private AporteMapper aporteMapper = new AporteMapper();

    @InjectMocks
    private CasoServiceImpl casoService;

    // Datos para pruebas
    private Usuario fundacionVerificada;
    private Usuario fundacionNoVerificada;
    private Usuario ciudadano;
    private Fundacion fundacionEntity;
    private Caso casoSinCustodia;
    private Caso casoConCustodia;

    @BeforeEach
    void setUp() {
        // Fundación verificada
        fundacionEntity = Fundacion.builder()
                .id(1L)
                .nombre("Fundación Patitas")
                .nit("123456789")
                .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                .aceptaCustodia(true)
                .capacidadAlbergue(10)
                .build();

        fundacionVerificada = Usuario.builder()
                .id(1L)
                .correo("fundacion@huellitas.com")
                .nombre("Fundación Patitas")
                .rol(Rol.FUNDACION_REFUGIO)
                .verificado(true)
                .organizacion(fundacionEntity)
                .build();

        // Fundación no verificada
        fundacionNoVerificada = Usuario.builder()
                .id(2L)
                .correo("fundacionnov@huellitas.com")
                .nombre("Fundación Nueva")
                .rol(Rol.FUNDACION_REFUGIO)
                .verificado(false)
                .organizacion(Fundacion.builder()
                        .id(2L)
                        .nombre("Fundación Nueva")
                        .estadoVerificacion(EstadoVerificacion.PENDIENTE)
                        .build())
                .build();

        // Ciudadano
        ciudadano = Usuario.builder()
                .id(3L)
                .correo("ciudadano@huellitas.com")
                .nombre("Ciudadano")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(false)
                .build();

        // Caso sin custodia
        Animal animal = Animal.builder().id(1L).nombre("Firulais").especie(Especie.PERRO).build();
        Barrio barrio = Barrio.builder().id(1L).nombre("Centro").build();
        casoSinCustodia = Caso.builder()
                .id(1L)
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .urgencia(Urgencia.MEDIA)
                .estado(EstadoCaso.REGISTRADO)
                .reportante(ciudadano)
                .animal(animal)
                .barrio(barrio)
                .visible(true)
                .build();

        // Caso con custodia
        casoConCustodia = Caso.builder()
                .id(2L)
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .urgencia(Urgencia.MEDIA)
                .estado(EstadoCaso.EN_ATENCION)
                .reportante(ciudadano)
                .animal(animal)
                .barrio(barrio)
                .fundacionCustodio(fundacionEntity)
                .visible(true)
                .build();
    }

    // === Tabla de decisión: rol × verificada × custodia ===

    @Test
    @DisplayName("13.3: Fundación verificada con aceptaCustodia=true debe poder asumir custodia")
    void debeAsumirCustodia_FundacionVerificada() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoSinCustodia));
        when(casoRepository.save(any(Caso.class))).thenReturn(casoSinCustodia);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));
        when(seguimientoRepository.save(any(Seguimiento.class))).thenAnswer(i -> i.getArgument(0));
        when(casoRepository.countByFundacionCustodio(fundacionEntity)).thenReturn(5L);

        CustodiaDTO dto = new CustodiaDTO("ASUMIR", null);

        // Act
        CasoResponseDTO result = casoService.gestionarCustodia(1L, dto, "fundacion@huellitas.com");

        // Assert
        assertNotNull(result);
        verify(notificacionWorker).notificarAsuncionCustodia(any(Caso.class), eq(fundacionEntity));
    }

    @Test
    @DisplayName("13.3: Fundación NO verificada debe recibir 403")
    void debeRechazarCustodia_FundacionNoVerificada() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacionnov@huellitas.com")).thenReturn(Optional.of(fundacionNoVerificada));
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoSinCustodia));

        CustodiaDTO dto = new CustodiaDTO("ASUMIR", null);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            casoService.gestionarCustodia(1L, dto, "fundacionnov@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("verificada"));
    }

    @Test
    @DisplayName("13.3: Ciudadano debe recibir 403 al intentar asumir custodia")
    void debeRechazarCustodia_Ciudadano() {
        // Arrange
        when(usuarioRepository.findByCorreo("ciudadano@huellitas.com")).thenReturn(Optional.of(ciudadano));

        CustodiaDTO dto = new CustodiaDTO("ASUMIR", null);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            casoService.gestionarCustodia(1L, dto, "ciudadano@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("fundaciones"));
    }

    @Test
    @DisplayName("13.3: Fundación con aceptaCustodia=false debe recibir 403")
    void debeRechazarCustodia_FundacionNoAcepta() {
        // Arrange
        fundacionEntity.setAceptaCustodia(false);
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoSinCustodia));

        CustodiaDTO dto = new CustodiaDTO("ASUMIR", null);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            casoService.gestionarCustodia(1L, dto, "fundacion@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("no acepta"));
    }

    @Test
    @DisplayName("13.3: Caso con custodia existente debe retornar 409")
    void debeRechazarCustodia_CasoYaTieneCustodio() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(2L)).thenReturn(Optional.of(casoConCustodia));
        when(casoRepository.countByFundacionCustodio(fundacionEntity)).thenReturn(5L);

        CustodiaDTO dto = new CustodiaDTO("ASUMIR", null);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            casoService.gestionarCustodia(2L, dto, "fundacion@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("custodio"));
    }

    @Test
    @DisplayName("13.3: Capacidad de albergue llena debe retornar 409")
    void debeRechazarCustodia_CapacidadLlena() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoSinCustodia));
        when(casoRepository.countByFundacionCustodio(fundacionEntity)).thenReturn(10L); // Capacidad llena

        CustodiaDTO dto = new CustodiaDTO("ASUMIR", null);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            casoService.gestionarCustodia(1L, dto, "fundacion@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("capacidad"));
    }

    @Test
    @DisplayName("13.3: Debe liberar custodia con motivo válido")
    void debeLiberarCustodia() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(2L)).thenReturn(Optional.of(casoConCustodia));
        when(casoRepository.save(any(Caso.class))).thenReturn(casoConCustodia);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));
        when(seguimientoRepository.save(any(Seguimiento.class))).thenAnswer(i -> i.getArgument(0));

        CustodiaDTO dto = new CustodiaDTO("LIBERAR", "El animal fue adoptado");

        // Act
        CasoResponseDTO result = casoService.gestionarCustodia(2L, dto, "fundacion@huellitas.com");

        // Assert
        assertNotNull(result);
        assertNull(casoConCustodia.getFundacionCustodio());
        verify(notificacionWorker).notificarLiberacionCustodia(casoConCustodia, "El animal fue adoptado");
    }

    @Test
    @DisplayName("13.3: Liberar custodia sin motivo debe fallar")
    void debeRechazarLiberacionSinMotivo() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(2L)).thenReturn(Optional.of(casoConCustodia));

        CustodiaDTO dto = new CustodiaDTO("LIBERAR", null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            casoService.gestionarCustodia(2L, dto, "fundacion@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("motivo"));
    }

    @Test
    @DisplayName("13.3: Solo el custodio puede liberar custodia")
    void debeRechazarLiberacion_CustodioDistinto() {
        // Arrange
        Fundacion otraFundacion = Fundacion.builder()
                .id(99L)
                .nombre("Otra Fundación")
                .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                .build();
        Usuario otraFundacionUsuario = Usuario.builder()
                .id(99L)
                .correo("otra@huellitas.com")
                .organizacion(otraFundacion)
                .build();

        when(usuarioRepository.findByCorreo("otra@huellitas.com")).thenReturn(Optional.of(otraFundacionUsuario));
        when(casoRepository.findById(2L)).thenReturn(Optional.of(casoConCustodia));

        CustodiaDTO dto = new CustodiaDTO("LIBERAR", "Motivo de prueba");

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            casoService.gestionarCustodia(2L, dto, "otra@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("custodio"));
    }

    @Test
    @DisplayName("13.3: Acción inválida debe fallar")
    void debeRechazarAccionInvalida() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoSinCustodia));

        CustodiaDTO dto = new CustodiaDTO("INVALIDO", null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            casoService.gestionarCustodia(1L, dto, "fundacion@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("ASUMIR"));
    }

    @Test
    @DisplayName("13.4: Listar casos en custodia de fundación")
    void debeListarCasosEnCustodia() {
        // Arrange
        when(usuarioRepository.findByCorreo("fundacion@huellitas.com")).thenReturn(Optional.of(fundacionVerificada));
        when(casoRepository.findByFundacionCustodioAndVisibleTrue(eq(fundacionEntity), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // Act
        var result = casoService.listarCasosEnCustodia("fundacion@huellitas.com", 
                org.springframework.data.domain.PageRequest.of(0, 10));

        // Assert
        verify(casoRepository).findByFundacionCustodioAndVisibleTrue(eq(fundacionEntity), any());
    }

    @Test
    @DisplayName("13.4: Listar casos sin custodia por municipio")
    void debeListarCasosSinCustodia() {
        // Arrange
        when(casoRepository.findBySinCustodiaAndMunicipioId(eq(1L), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // Act
        var result = casoService.listarCasosSinCustodia(1L, 
                org.springframework.data.domain.PageRequest.of(0, 10));

        // Assert
        verify(casoRepository).findBySinCustodiaAndMunicipioId(eq(1L), any());
    }

    @Test
    @DisplayName("13.4: Listar sin custodia sin municipio debe fallar")
    void debeRechazarSinCustodiaSinMunicipio() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            casoService.listarCasosSinCustodia(null, null);
        });

        assertTrue(exception.getMessage().contains("municipioId"));
    }
}