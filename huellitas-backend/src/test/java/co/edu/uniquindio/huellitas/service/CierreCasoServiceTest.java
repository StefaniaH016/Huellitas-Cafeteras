package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.reporte.CierreCasoRequestDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CasoResponseDTO;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.mapper.AporteMapper;
import co.edu.uniquindio.huellitas.mapper.CasoMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.impl.CasoServiceImpl;
import co.edu.uniquindio.huellitas.service.validator.EstadoCasoValidator;
import co.edu.uniquindio.huellitas.worker.FiltroWorker;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de las transiciones de EstadoCaso y las reglas de cierre (Subtarea 16.6).
 */
@ExtendWith(MockitoExtension.class)
class CierreCasoServiceTest {

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
    private CasoMapper casoMapper;
    @Mock
    private AporteMapper aporteMapper;
    @Mock
    private NotificacionWorker notificacionWorker;
    @Mock
    private FiltroWorker filtroWorker;
    @Mock
    private EmailService emailService;
    @Mock
    private BitacoraMedicaRepository bitacoraMedicaRepository;

    private EstadoCasoValidator estadoCasoValidator;
    private CasoServiceImpl casoService;

    private Usuario reportante;
    private Usuario admin;
    private Usuario fundacionUsuario;
    private Fundacion fundacionOrg;
    private Usuario usuarioAjeno;

    @BeforeEach
    void setUp() {
        estadoCasoValidator = new EstadoCasoValidator();
        casoService = new CasoServiceImpl(
                casoRepository,
                usuarioRepository,
                barrioRepository,
                animalRepository,
                historialEstadoRepository,
                seguimientoRepository,
                cierreCasoRepository,
                notificacionRepository,
                cloudinaryService,
                casoMapper,
                aporteMapper,
                notificacionWorker,
                filtroWorker,
                emailService,
                estadoCasoValidator,
                bitacoraMedicaRepository
        );

        reportante = Usuario.builder()
                .id(1L)
                .nombre("Juan Reportante")
                .correo("reportante@test.co")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();

        admin = Usuario.builder()
                .id(2L)
                .nombre("Admin")
                .correo("admin@test.co")
                .rol(Rol.ADMINISTRADOR)
                .verificado(true)
                .build();

        fundacionOrg = Fundacion.builder()
                .id(10L)
                .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                .aceptaCustodia(true)
                .capacidadAlbergue(10)
                .build();

        fundacionUsuario = Usuario.builder()
                .id(3L)
                .nombre("Fundación Amigos")
                .correo("fundacion@test.co")
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacionOrg)
                .verificado(true)
                .build();

        usuarioAjeno = Usuario.builder()
                .id(4L)
                .nombre("Pedro Ajeno")
                .correo("ajeno@test.co")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();
    }

    // ── 1. Pruebas de Transiciones de Estado (Subtarea 16.1) ──────────

    @Test
    @DisplayName("Transición válida: REGISTRADO -> EN_ATENCION")
    void transicion_registrado_a_enAtencion_valida() {
        assertDoesNotThrow(() -> estadoCasoValidator.validarTransicion(EstadoCaso.REGISTRADO, EstadoCaso.EN_ATENCION));
        assertTrue(estadoCasoValidator.esTransicionValida(EstadoCaso.REGISTRADO, EstadoCaso.EN_ATENCION));
    }

    @Test
    @DisplayName("Transición válida: EN_ATENCION -> RESUELTO")
    void transicion_enAtencion_a_resuelto_valida() {
        assertDoesNotThrow(() -> estadoCasoValidator.validarTransicion(EstadoCaso.EN_ATENCION, EstadoCaso.RESUELTO));
        assertTrue(estadoCasoValidator.esTransicionValida(EstadoCaso.EN_ATENCION, EstadoCaso.RESUELTO));
    }

    @Test
    @DisplayName("Transición válida: RESUELTO -> ARCHIVADO")
    void transicion_resuelto_a_archivado_valida() {
        assertDoesNotThrow(() -> estadoCasoValidator.validarTransicion(EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO));
        assertTrue(estadoCasoValidator.esTransicionValida(EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO));
    }

    @Test
    @DisplayName("Transición inválida: retroceder de EN_ATENCION a REGISTRADO")
    void transicion_invalida_enAtencion_a_registrado_falla() {
        ConflictException ex = assertThrows(ConflictException.class,
                () -> estadoCasoValidator.validarTransicion(EstadoCaso.EN_ATENCION, EstadoCaso.REGISTRADO));
        assertTrue(ex.getMessage().contains("Transición de estado inválida"));
    }

    @Test
    @DisplayName("Transición inválida: retroceder de RESUELTO a EN_ATENCION")
    void transicion_invalida_resuelto_a_enAtencion_falla() {
        ConflictException ex = assertThrows(ConflictException.class,
                () -> estadoCasoValidator.validarTransicion(EstadoCaso.RESUELTO, EstadoCaso.EN_ATENCION));
        assertTrue(ex.getMessage().contains("Transición de estado inválida"));
    }

    @Test
    @DisplayName("Transición inválida: intentar cambiar desde ARCHIVADO")
    void transicion_invalida_archivado_terminal_falla() {
        ConflictException ex = assertThrows(ConflictException.class,
                () -> estadoCasoValidator.validarTransicion(EstadoCaso.ARCHIVADO, EstadoCaso.RESUELTO));
        assertTrue(ex.getMessage().contains("Transición de estado inválida"));
    }

    @Test
    @DisplayName("Transición inválida: auto-transición al mismo estado")
    void transicion_mismoEstado_falla() {
        ConflictException ex = assertThrows(ConflictException.class,
                () -> estadoCasoValidator.validarTransicion(EstadoCaso.EN_ATENCION, EstadoCaso.EN_ATENCION));
        assertTrue(ex.getMessage().contains("ya se encuentra en estado"));
    }

    // ── 2. Reglas de Cierre: REENCUENTRO (Subtarea 16.2.A) ─────────────

    @Test
    @DisplayName("Cierre exitoso: REENCUENTRO en caso EXTRAVIADO por el reportante")
    void cierre_reencuentro_extraviado_porReportante_exitoso() {
        Caso caso = Caso.builder()
                .id(100L)
                .estado(EstadoCaso.REGISTRADO)
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .reportante(reportante)
                .build();

        when(casoRepository.findById(100L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("reportante@test.co")).thenReturn(Optional.of(reportante));
        when(cierreCasoRepository.save(any(CierreCaso.class))).thenAnswer(i -> i.getArgument(0));
        when(casoRepository.save(any(Caso.class))).thenAnswer(i -> i.getArgument(0));
        when(casoMapper.toResponseDTO(any(Caso.class)))
                .thenReturn(CasoResponseDTO.builder().id(100L).estado(EstadoCaso.RESUELTO).build());

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.REENCUENTRO, "Encontrado en parque", null);
        CasoResponseDTO res = casoService.cerrarCaso(100L, dto, "reportante@test.co");

        assertNotNull(res);
        assertEquals(EstadoCaso.RESUELTO, res.estado());
        verify(notificacionWorker).notificarCierreCaso(any(Caso.class), eq("REENCUENTRO"));
        verify(emailService).enviarCorreoCierre(any(Caso.class), eq("reportante@test.co"), eq("REENCUENTRO"));
    }

    @Test
    @DisplayName("Cierre rechazado: REENCUENTRO en caso que NO es EXTRAVIADO")
    void cierre_reencuentro_casoNoExtraviado_falla() {
        Caso caso = Caso.builder()
                .id(101L)
                .estado(EstadoCaso.REGISTRADO)
                .tipoCaso(TipoCaso.VULNERABLE) // No es EXTRAVIADO
                .reportante(reportante)
                .build();

        when(casoRepository.findById(101L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("reportante@test.co")).thenReturn(Optional.of(reportante));

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.REENCUENTRO, "Encontrado", null);
        ConflictException ex = assertThrows(ConflictException.class,
                () -> casoService.cerrarCaso(101L, dto, "reportante@test.co"));

        assertTrue(ex.getMessage().contains("REENCUENTRO solo aplica para casos de tipo EXTRAVIADO"));
        verify(cierreCasoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cierre rechazado: REENCUENTRO intentado por usuario que no es el reportante")
    void cierre_reencuentro_porNoReportante_falla() {
        Caso caso = Caso.builder()
                .id(102L)
                .estado(EstadoCaso.REGISTRADO)
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .reportante(reportante)
                .build();

        when(casoRepository.findById(102L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("admin@test.co")).thenReturn(Optional.of(admin));

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.REENCUENTRO, "Encontrado", null);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> casoService.cerrarCaso(102L, dto, "admin@test.co"));

        assertTrue(ex.getMessage().contains("Solo el ciudadano reportante del caso puede registrar el cierre por REENCUENTRO"));
        verify(cierreCasoRepository, never()).save(any());
    }

    // ── 3. Reglas de Cierre: Custodia y entregadoA (Subtarea 16.2.B) ────

    @Test
    @DisplayName("Cierre rechazado: Caso con custodia sin campo entregadoA")
    void cierre_conCustodia_sinEntregadoA_falla() {
        Caso caso = Caso.builder()
                .id(103L)
                .estado(EstadoCaso.EN_ATENCION)
                .tipoCaso(TipoCaso.HERIDO)
                .reportante(reportante)
                .fundacionCustodio(fundacionOrg)
                .build();

        when(casoRepository.findById(103L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("fundacion@test.co")).thenReturn(Optional.of(fundacionUsuario));

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.ENTREGADO_A_CUIDADOR, "Adopción", null); // entregadoA null
        ConflictException ex = assertThrows(ConflictException.class,
                () -> casoService.cerrarCaso(103L, dto, "fundacion@test.co"));

        assertTrue(ex.getMessage().contains("El campo 'entregadoA' es obligatorio"));
        verify(cierreCasoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cierre exitoso: Caso con custodia con entregadoA por fundación custodia")
    void cierre_conCustodia_conEntregadoA_porFundacion_exitoso() {
        Caso caso = Caso.builder()
                .id(104L)
                .estado(EstadoCaso.EN_ATENCION)
                .tipoCaso(TipoCaso.HERIDO)
                .reportante(reportante)
                .fundacionCustodio(fundacionOrg)
                .build();

        when(casoRepository.findById(104L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("fundacion@test.co")).thenReturn(Optional.of(fundacionUsuario));
        when(cierreCasoRepository.save(any(CierreCaso.class))).thenAnswer(i -> i.getArgument(0));
        when(casoRepository.save(any(Caso.class))).thenAnswer(i -> i.getArgument(0));
        when(casoMapper.toResponseDTO(any(Caso.class)))
                .thenReturn(CasoResponseDTO.builder().id(104L).estado(EstadoCaso.RESUELTO).build());

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.ENTREGADO_A_CUIDADOR, "Adoptado", "Familia Gómez");
        CasoResponseDTO res = casoService.cerrarCaso(104L, dto, "fundacion@test.co");

        assertNotNull(res);
        assertEquals(EstadoCaso.RESUELTO, res.estado());
        verify(cierreCasoRepository).save(any(CierreCaso.class));
    }

    // ── 4. Reglas de Cierre: Alta Médica Obligatoria (Subtarea 16.2.C) ──

    @Test
    @DisplayName("Cierre rechazado: Caso con bitácora abierta pero sin alta médica emitida")
    void cierre_conBitacoraAbierta_sinAltaMedica_falla() {
        BitacoraMedica bitacora = BitacoraMedica.builder()
                .id(50L)
                .altaMedica(false) // NO TIENE ALTA
                .build();

        Caso caso = Caso.builder()
                .id(105L)
                .estado(EstadoCaso.EN_ATENCION)
                .tipoCaso(TipoCaso.HERIDO)
                .reportante(reportante)
                .bitacoraMedica(bitacora)
                .build();

        when(casoRepository.findById(105L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("reportante@test.co")).thenReturn(Optional.of(reportante));

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.RECUPERADO, "Recuperado", null);
        ConflictException ex = assertThrows(ConflictException.class,
                () -> casoService.cerrarCaso(105L, dto, "reportante@test.co"));

        assertTrue(ex.getMessage().contains("la bitácora médica asociada requiere alta médica previa"));
        verify(cierreCasoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cierre exitoso: Caso con bitácora que ya cuenta con alta médica")
    void cierre_conBitacoraAbierta_conAltaMedica_exitoso() {
        BitacoraMedica bitacora = BitacoraMedica.builder()
                .id(51L)
                .altaMedica(true) // CON ALTA MÉDICA
                .fechaAlta(Instant.now())
                .build();

        Caso caso = Caso.builder()
                .id(106L)
                .estado(EstadoCaso.EN_ATENCION)
                .tipoCaso(TipoCaso.HERIDO)
                .reportante(reportante)
                .bitacoraMedica(bitacora)
                .build();

        when(casoRepository.findById(106L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("reportante@test.co")).thenReturn(Optional.of(reportante));
        when(cierreCasoRepository.save(any(CierreCaso.class))).thenAnswer(i -> i.getArgument(0));
        when(casoRepository.save(any(Caso.class))).thenAnswer(i -> i.getArgument(0));
        when(casoMapper.toResponseDTO(any(Caso.class)))
                .thenReturn(CasoResponseDTO.builder().id(106L).estado(EstadoCaso.RESUELTO).build());

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.RECUPERADO, "Completamente sano", null);
        CasoResponseDTO res = casoService.cerrarCaso(106L, dto, "reportante@test.co");

        assertNotNull(res);
        assertEquals(EstadoCaso.RESUELTO, res.estado());
        verify(cierreCasoRepository).save(any(CierreCaso.class));
    }

    @Test
    @DisplayName("Cierre rechazado: Caso ya RESUELTO intentando cerrarse de nuevo")
    void cierre_casoYaResuelto_falla() {
        Caso caso = Caso.builder()
                .id(107L)
                .estado(EstadoCaso.RESUELTO)
                .reportante(reportante)
                .build();

        when(casoRepository.findById(107L)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo("reportante@test.co")).thenReturn(Optional.of(reportante));

        CierreCasoRequestDTO dto = new CierreCasoRequestDTO(ResultadoCierre.RECUPERADO, "Cierre", null);
        ConflictException ex = assertThrows(ConflictException.class,
                () -> casoService.cerrarCaso(107L, dto, "reportante@test.co"));

        assertTrue(ex.getMessage().contains("ya se encuentra en estado") || ex.getMessage().contains("ya está cerrado"));
        verify(cierreCasoRepository, never()).save(any());
    }
}
