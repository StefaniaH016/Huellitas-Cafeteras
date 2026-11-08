package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.BitacoraMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.impl.BitacoraServiceImpl;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import co.edu.uniquindio.huellitas.worker.PdfWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para BitacoraServiceImpl - Subtarea 14.7
 * Cubre:
 * - Tabla de decisión de permisos (Rol × Verificado × Relación con el caso)
 * - Restricción de lectura de entradas clínicas a usuarios autorizados por caso
 * - Pruebas de estado (REGISTRADO, EN_ATENCION, RESUELTO, ARCHIVADO)
 * - Inmutabilidad y bloqueo tras alta médica
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BitacoraService - Pruebas Unitarias (HU-14)")
class BitacoraServiceTest {

    @Mock
    private BitacoraMedicaRepository bitacoraMedicaRepository;
    @Mock
    private RegistroMedicoRepository registroMedicoRepository;
    @Mock
    private CasoRepository casoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PdfWorker pdfWorker;
    @Mock
    private NotificacionWorker notificacionWorker;

    @Spy
    private BitacoraMapper bitacoraMapper = new BitacoraMapper();

    @InjectMocks
    private BitacoraServiceImpl bitacoraService;

    private Usuario veterinariaVerificada;
    private Usuario veterinariaNoVerificada;
    private Usuario reportante;
    private Usuario ciudadanoAjeno;
    private Usuario admin;
    private Fundacion fundacionCustodioEntity;
    private Usuario usuarioFundacion;
    private Caso casoEnAtencion;
    private Caso casoRegistrado;
    private Caso casoArchivado;
    private BitacoraMedica bitacoraAbierta;

    @BeforeEach
    void setUp() {
        veterinariaVerificada = Usuario.builder()
                .id(10L)
                .nombre("Dra. Gómez")
                .correo("vet.verificada@huellitas.co")
                .rol(Rol.VETERINARIA)
                .verificado(true)
                .build();

        veterinariaNoVerificada = Usuario.builder()
                .id(11L)
                .nombre("Dr. Pérez")
                .correo("vet.noverificada@huellitas.co")
                .rol(Rol.VETERINARIA)
                .verificado(false)
                .build();

        reportante = Usuario.builder()
                .id(20L)
                .nombre("Juan Reportante")
                .correo("reportante@huellitas.co")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();

        ciudadanoAjeno = Usuario.builder()
                .id(21L)
                .nombre("Carlos Ajeno")
                .correo("ajeno@huellitas.co")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();

        admin = Usuario.builder()
                .id(99L)
                .nombre("Admin General")
                .correo("admin@huellitas.co")
                .rol(Rol.ADMINISTRADOR)
                .verificado(true)
                .build();

        fundacionCustodioEntity = Fundacion.builder()
                .id(30L)
                .nombre("Huellas de Amor")
                .nit("900123456-1")
                .build();

        usuarioFundacion = Usuario.builder()
                .id(31L)
                .nombre("Usuario Fundación")
                .correo("fundacion@huellitas.co")
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacionCustodioEntity)
                .verificado(true)
                .build();

        Animal animal = Animal.builder()
                .id(1L)
                .nombre("Pelusa")
                .especie(Especie.PERRO)
                .build();

        casoEnAtencion = Caso.builder()
                .id(100L)
                .estado(EstadoCaso.EN_ATENCION)
                .descripcion("Perro con laceraciones")
                .animal(animal)
                .reportante(reportante)
                .fundacionCustodio(fundacionCustodioEntity)
                .build();

        casoRegistrado = Caso.builder()
                .id(101L)
                .estado(EstadoCaso.REGISTRADO)
                .descripcion("Caso recién registrado")
                .animal(animal)
                .reportante(reportante)
                .build();

        casoArchivado = Caso.builder()
                .id(102L)
                .estado(EstadoCaso.ARCHIVADO)
                .descripcion("Caso archivado por inactividad")
                .animal(animal)
                .reportante(reportante)
                .build();

        bitacoraAbierta = BitacoraMedica.builder()
                .id(50L)
                .caso(casoEnAtencion)
                .veterinario(veterinariaVerificada)
                .fechaApertura(Instant.now())
                .altaMedica(false)
                .build();
    }

    // ── 14.1: Reglas de Apertura ───────────────────────────────────

    @Nested
    @DisplayName("Subtarea 14.1 - Apertura de Bitácora")
    class AperturaBitacoraTests {

        @Test
        @DisplayName("Abre bitácora exitosamente con veterinaria verificada y caso EN_ATENCION")
        void testAbrirBitacora_Exito() {
            when(bitacoraMedicaRepository.existsByCasoId(100L)).thenReturn(false);
            when(casoRepository.findById(100L)).thenReturn(Optional.of(casoEnAtencion));
            when(usuarioRepository.findByCorreo("vet.verificada@huellitas.co")).thenReturn(Optional.of(veterinariaVerificada));
            when(bitacoraMedicaRepository.save(any(BitacoraMedica.class))).thenAnswer(invocation -> {
                BitacoraMedica bm = invocation.getArgument(0);
                bm.setId(50L);
                return bm;
            });

            AbrirBitacoraDTO dto = new AbrirBitacoraDTO("Ingreso para curación y monitoreo");
            BitacoraResponseDTO response = bitacoraService.abrirBitacora(100L, dto, "vet.verificada@huellitas.co");

            assertNotNull(response);
            assertEquals(50L, response.id());
            assertEquals(100L, response.casoId());
            assertEquals("ABIERTA", response.estado());
            verify(registroMedicoRepository, times(1)).save(any(RegistroMedico.class));
            verify(casoRepository, times(1)).save(casoEnAtencion);
        }

        @Test
        @DisplayName("Lanza 409 Conflict si ya existe bitácora para el caso")
        void testAbrirBitacora_ConflictoDuplicada() {
            when(bitacoraMedicaRepository.existsByCasoId(100L)).thenReturn(true);

            assertThrows(ConflictException.class, () ->
                    bitacoraService.abrirBitacora(100L, null, "vet.verificada@huellitas.co"));
            verify(bitacoraMedicaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lanza 409 Conflict si el caso no está en estado EN_ATENCION (ej: REGISTRADO)")
        void testAbrirBitacora_CasoNoEnAtencion() {
            when(bitacoraMedicaRepository.existsByCasoId(101L)).thenReturn(false);
            when(casoRepository.findById(101L)).thenReturn(Optional.of(casoRegistrado));

            ConflictException ex = assertThrows(ConflictException.class, () ->
                    bitacoraService.abrirBitacora(101L, null, "vet.verificada@huellitas.co"));
            assertTrue(ex.getMessage().contains("EN_ATENCION"));
        }

        @Test
        @DisplayName("Lanza 403 Forbidden si la veterinaria no está verificada")
        void testAbrirBitacora_VeterinariaNoVerificada() {
            when(bitacoraMedicaRepository.existsByCasoId(100L)).thenReturn(false);
            when(casoRepository.findById(100L)).thenReturn(Optional.of(casoEnAtencion));
            when(usuarioRepository.findByCorreo("vet.noverificada@huellitas.co")).thenReturn(Optional.of(veterinariaNoVerificada));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.abrirBitacora(100L, null, "vet.noverificada@huellitas.co"));
        }

        @Test
        @DisplayName("Lanza 403 Forbidden si el rol no es VETERINARIA")
        void testAbrirBitacora_RolInvalido() {
            when(bitacoraMedicaRepository.existsByCasoId(100L)).thenReturn(false);
            when(casoRepository.findById(100L)).thenReturn(Optional.of(casoEnAtencion));
            when(usuarioRepository.findByCorreo("reportante@huellitas.co")).thenReturn(Optional.of(reportante));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.abrirBitacora(100L, null, "reportante@huellitas.co"));
        }

        @Test
        @DisplayName("Lanza 404 Not Found si el caso no existe")
        void testAbrirBitacora_CasoInexistente() {
            when(bitacoraMedicaRepository.existsByCasoId(999L)).thenReturn(false);
            when(casoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    bitacoraService.abrirBitacora(999L, null, "vet.verificada@huellitas.co"));
        }
    }

    // ── 14.2 & 14.6: Evolución Médica ──────────────────────────────

    @Nested
    @DisplayName("Subtarea 14.2 y 14.6 - Registro de Evoluciones e Inmutabilidad")
    class EvolucionMedicaTests {

        @Test
        @DisplayName("Agrega evolución exitosamente y dispara notificación W-01")
        void testAgregarEvolucion_Exito() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("vet.verificada@huellitas.co")).thenReturn(Optional.of(veterinariaVerificada));
            when(registroMedicoRepository.save(any(RegistroMedico.class))).thenAnswer(invocation -> {
                RegistroMedico r = invocation.getArgument(0);
                r.setId(201L);
                return r;
            });

            EvolucionDTO dto = new EvolucionDTO(
                    "Fiebre reducida", "Examen físico", "Suero ringer", "Amoxicilina", "Paciente más activo"
            );

            RegistroMedicoResponseDTO response = bitacoraService.agregarEvolucion(100L, dto, "vet.verificada@huellitas.co");

            assertNotNull(response);
            assertEquals(201L, response.id());
            assertEquals(50L, response.bitacoraId());
            verify(registroMedicoRepository, times(1)).save(any(RegistroMedico.class));
            verify(notificacionWorker, times(1)).notificarEvolucionMedica(eq(bitacoraAbierta), any(RegistroMedico.class), eq(veterinariaVerificada));
        }

        @Test
        @DisplayName("Lanza 409 Conflict al intentar agregar evolución tras emitir alta médica")
        void testAgregarEvolucion_BloqueadaPorAltaMedica() {
            bitacoraAbierta.setAltaMedica(true);
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));

            EvolucionDTO dto = new EvolucionDTO("Control post alta", null, null, null, null);

            ConflictException ex = assertThrows(ConflictException.class, () ->
                    bitacoraService.agregarEvolucion(100L, dto, "vet.verificada@huellitas.co"));
            assertTrue(ex.getMessage().contains("alta médica registrada"));
            verify(registroMedicoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lanza 409 Conflict si el caso está ARCHIVADO")
        void testAgregarEvolucion_CasoArchivado() {
            BitacoraMedica bitacoraArchivada = BitacoraMedica.builder()
                    .id(52L)
                    .caso(casoArchivado)
                    .altaMedica(false)
                    .build();
            when(bitacoraMedicaRepository.findByCasoId(102L)).thenReturn(Optional.of(bitacoraArchivada));

            EvolucionDTO dto = new EvolucionDTO("Intento de evolución", null, null, null, null);

            assertThrows(ConflictException.class, () ->
                    bitacoraService.agregarEvolucion(102L, dto, "vet.verificada@huellitas.co"));
        }

        @Test
        @DisplayName("Lanza 403 Forbidden si la veterinaria no está verificada")
        void testAgregarEvolucion_VeterinariaNoVerificada() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("vet.noverificada@huellitas.co")).thenReturn(Optional.of(veterinariaNoVerificada));

            EvolucionDTO dto = new EvolucionDTO("Diagnóstico", null, null, null, null);

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.agregarEvolucion(100L, dto, "vet.noverificada@huellitas.co"));
        }

        @Test
        @DisplayName("Verifica inmutabilidad en RegistroMedico: preUpdate arroja UnsupportedOperationException")
        void testRegistroMedico_InmutabilidadPreUpdate() {
            RegistroMedico rm = new RegistroMedico();
            assertThrows(UnsupportedOperationException.class, rm::preUpdate);
        }
    }

    // ── 14.3 & 14.6: Alta Médica ───────────────────────────────────

    @Nested
    @DisplayName("Subtarea 14.3 y 14.6 - Emisión de Alta Médica")
    class AltaMedicaTests {

        @Test
        @DisplayName("Registra alta médica exitosamente, guarda fechaAlta y dispara W-01")
        void testRegistrarAlta_Exito() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("vet.verificada@huellitas.co")).thenReturn(Optional.of(veterinariaVerificada));

            AltaMedicaDTO dto = new AltaMedicaDTO("Completamente sano", "Dieta balanceada y vacunación");
            AltaMedicaResponseDTO response = bitacoraService.registrarAlta(100L, dto, "vet.verificada@huellitas.co");

            assertNotNull(response);
            assertEquals("ALTA_REGISTRADA", response.estadoBitacora());
            assertTrue(bitacoraAbierta.isAltaMedica());
            assertNotNull(bitacoraAbierta.getFechaAlta());
            verify(bitacoraMedicaRepository, times(1)).save(bitacoraAbierta);
            verify(registroMedicoRepository, times(1)).save(any(RegistroMedico.class));
            verify(notificacionWorker, times(1)).notificarAltaMedica(eq(bitacoraAbierta), eq(veterinariaVerificada));
        }

        @Test
        @DisplayName("Lanza 409 Conflict si el alta médica ya fue emitida previamente")
        void testRegistrarAlta_Duplicada() {
            bitacoraAbierta.setAltaMedica(true);
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));

            AltaMedicaDTO dto = new AltaMedicaDTO("Segunda alta", null);
            assertThrows(ConflictException.class, () ->
                    bitacoraService.registrarAlta(100L, dto, "vet.verificada@huellitas.co"));
        }

        @Test
        @DisplayName("Lanza 403 Forbidden si la veterinaria no está verificada al dar alta")
        void testRegistrarAlta_NoVerificada() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("vet.noverificada@huellitas.co")).thenReturn(Optional.of(veterinariaNoVerificada));

            AltaMedicaDTO dto = new AltaMedicaDTO("Alta", null);
            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.registrarAlta(100L, dto, "vet.noverificada@huellitas.co"));
        }
    }

    // ── 14.4: Privacidad y Lectura (Ley 1581 / Ley 576) ─────────────

    @Nested
    @DisplayName("Subtarea 14.4 - Tabla de Decisión de Permisos de Lectura")
    class LecturaBitacoraTests {

        @Test
        @DisplayName("Veterinaria verificada tiene acceso a la bitácora")
        void testObtenerBitacora_AccesoVeterinariaVerificada() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("vet.verificada@huellitas.co")).thenReturn(Optional.of(veterinariaVerificada));
            when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(50L)).thenReturn(Collections.emptyList());

            BitacoraDetalleDTO detalle = bitacoraService.obtenerBitacora(100L, "vet.verificada@huellitas.co");
            assertNotNull(detalle);
            assertEquals(50L, detalle.id());
        }

        @Test
        @DisplayName("Veterinaria no verificada no puede consultar las entradas clínicas")
        void testObtenerBitacora_DenegadoVeterinariaNoVerificada() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("vet.noverificada@huellitas.co"))
                    .thenReturn(Optional.of(veterinariaNoVerificada));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.obtenerBitacora(100L, "vet.noverificada@huellitas.co"));
        }

        @Test
        @DisplayName("El reportante del caso puede consultar las entradas clínicas")
        void testObtenerBitacora_AccesoReportante() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("reportante@huellitas.co")).thenReturn(Optional.of(reportante));
            when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(50L))
                    .thenReturn(Collections.emptyList());

            assertNotNull(bitacoraService.obtenerBitacora(100L, "reportante@huellitas.co"));
        }

        @Test
        @DisplayName("La fundación custodio puede consultar las entradas clínicas")
        void testObtenerBitacora_AccesoFundacionCustodia() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("fundacion@huellitas.co")).thenReturn(Optional.of(usuarioFundacion));
            when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(50L))
                    .thenReturn(Collections.emptyList());

            assertNotNull(bitacoraService.obtenerBitacora(100L, "fundacion@huellitas.co"));
        }

        @Test
        @DisplayName("La administración puede consultar las entradas clínicas")
        void testObtenerBitacora_AccesoAdministrador() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("admin@huellitas.co")).thenReturn(Optional.of(admin));
            when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(50L))
                    .thenReturn(Collections.emptyList());

            assertNotNull(bitacoraService.obtenerBitacora(100L, "admin@huellitas.co"));
        }

        @Test
        @DisplayName("Ciudadano ajeno es denegado (403 Forbidden - Ley 1581 / Ley 576)")
        void testObtenerBitacora_DenegadoCiudadanoAjeno() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
            when(usuarioRepository.findByCorreo("ajeno@huellitas.co")).thenReturn(Optional.of(ciudadanoAjeno));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.obtenerBitacora(100L, "ajeno@huellitas.co"));
        }

        @Test
        @DisplayName("Petición anónima (sin autenticación) es denegada con 403")
        void testObtenerBitacora_DenegadoSinAutenticacion() {
            when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.obtenerBitacora(100L, null));
        }
    }

    // ── 14.5: Panel de Veterinaria (mis-pacientes) ──────────────────

    @Nested
    @DisplayName("Subtarea 14.5 - Mis Pacientes")
    class MisPacientesTests {

        @Test
        @DisplayName("Lista los pacientes de la veterinaria verificada")
        void testListarMisPacientes_Exito() {
            when(usuarioRepository.findByCorreo("vet.verificada@huellitas.co")).thenReturn(Optional.of(veterinariaVerificada));
            when(bitacoraMedicaRepository.findMisPacientes(10L, null)).thenReturn(List.of(bitacoraAbierta));

            List<PacienteResponseDTO> pacientes = bitacoraService.listarMisPacientes("vet.verificada@huellitas.co");

            assertNotNull(pacientes);
            assertEquals(1, pacientes.size());
            assertEquals(100L, pacientes.get(0).casoId());
            assertEquals("Pelusa", pacientes.get(0).nombre());
        }

        @Test
        @DisplayName("Deniega consulta a veterinaria no verificada")
        void testListarMisPacientes_NoVerificada() {
            when(usuarioRepository.findByCorreo("vet.noverificada@huellitas.co")).thenReturn(Optional.of(veterinariaNoVerificada));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.listarMisPacientes("vet.noverificada@huellitas.co"));
        }

        @Test
        @DisplayName("Deniega consulta a usuario con rol distinto de VETERINARIA")
        void testListarMisPacientes_RolInvalido() {
            when(usuarioRepository.findByCorreo("reportante@huellitas.co")).thenReturn(Optional.of(reportante));

            assertThrows(AccessDeniedException.class, () ->
                    bitacoraService.listarMisPacientes("reportante@huellitas.co"));
        }
    }

    // ── Resumen Clínico PDF ─────────────────────────────────────────

    @Test
    @DisplayName("Genera PDF resumen delegando al PdfWorker")
    void testGenerarPdfResumen() {
        byte[] pdfBytes = "%PDF-1.4 test bytes".getBytes();
        when(casoRepository.existsById(100L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
        when(usuarioRepository.findByCorreo("vet.verificada@huellitas.co")).thenReturn(Optional.of(veterinariaVerificada));
        when(pdfWorker.generarResumenClinico(100L)).thenReturn(pdfBytes);

        byte[] result = bitacoraService.generarPdfResumen(100L);
        assertThat(result).isEqualTo(pdfBytes);
        verify(pdfWorker, times(1)).generarResumenClinico(100L);
    }

    @Test
    @DisplayName("La administración autorizada puede generar el PDF clínico")
    void testGenerarPdfResumen_AccesoAdministrador() {
        byte[] pdfBytes = "%PDF-1.4 test bytes".getBytes();
        when(casoRepository.existsById(100L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(100L)).thenReturn(Optional.of(bitacoraAbierta));
        when(usuarioRepository.findByCorreo("admin@huellitas.co")).thenReturn(Optional.of(admin));
        when(pdfWorker.generarResumenClinico(100L)).thenReturn(pdfBytes);

        assertThat(bitacoraService.generarPdfResumen(100L, "admin@huellitas.co")).isEqualTo(pdfBytes);
        verify(pdfWorker).generarResumenClinico(100L);
    }
}
