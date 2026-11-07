package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.BitacoraMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.impl.BitacoraServiceImpl;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import co.edu.uniquindio.huellitas.worker.PdfWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para generación de PDF clínico y validación de permisos (Subtarea 16.7).
 */
@ExtendWith(MockitoExtension.class)
class PdfGeneracionYPermisosTest {

    @Mock
    private BitacoraMedicaRepository bitacoraMedicaRepository;
    @Mock
    private RegistroMedicoRepository registroMedicoRepository;
    @Mock
    private CasoRepository casoRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    @Mock
    private SeguimientoRepository seguimientoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BitacoraMapper bitacoraMapper;
    @Mock
    private NotificacionWorker notificacionWorker;

    private PdfWorker pdfWorker;
    private BitacoraServiceImpl bitacoraService;

    private Caso caso;
    private BitacoraMedica bitacora;
    private Usuario reportante;
    private Usuario admin;
    private Usuario veterinario;
    private Usuario fundacionUsuario;
    private Fundacion fundacionOrg;
    private Usuario ciudadanoAjeno;

    @BeforeEach
    void setUp() {
        pdfWorker = new PdfWorker(casoRepository, bitacoraMedicaRepository, registroMedicoRepository);
        bitacoraService = new BitacoraServiceImpl(
                bitacoraMedicaRepository,
                registroMedicoRepository,
                casoRepository,
                historialEstadoRepository,
                seguimientoRepository,
                usuarioRepository,
                bitacoraMapper,
                pdfWorker,
                notificacionWorker
        );

        reportante = Usuario.builder()
                .id(1L)
                .nombre("Laura Reportante")
                .correo("reportante@test.co")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();

        admin = Usuario.builder()
                .id(2L)
                .nombre("Carlos Admin")
                .correo("admin@test.co")
                .rol(Rol.ADMINISTRADOR)
                .verificado(true)
                .build();

        veterinario = Usuario.builder()
                .id(3L)
                .nombre("Dra. Andrea Vet")
                .correo("vet@test.co")
                .rol(Rol.VETERINARIA)
                .verificado(true)
                .build();

        fundacionOrg = Fundacion.builder()
                .id(10L)
                .nombre("Refugio Huellas")
                .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                .build();

        fundacionUsuario = Usuario.builder()
                .id(4L)
                .nombre("Fundación Usuario")
                .correo("fundacion@test.co")
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacionOrg)
                .verificado(true)
                .build();

        ciudadanoAjeno = Usuario.builder()
                .id(5L)
                .nombre("Extraño")
                .correo("ajeno@test.co")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();

        Animal animal = Animal.builder()
                .id(1L)
                .nombre("Firulais")
                .especie(Especie.PERRO)
                .raza("Criollo")
                .sexo(Sexo.MACHO)
                .build();

        caso = Caso.builder()
                .id(10L)
                .estado(EstadoCaso.EN_ATENCION)
                .tipoCaso(TipoCaso.HERIDO)
                .urgencia(Urgencia.ALTA)
                .animal(animal)
                .reportante(reportante)
                .fundacionCustodio(fundacionOrg)
                .build();

        bitacora = BitacoraMedica.builder()
                .id(20L)
                .caso(caso)
                .fechaApertura(Instant.now())
                .veterinario(veterinario)
                .altaMedica(true)
                .fechaAlta(Instant.now())
                .build();
    }

    // ── 1. Generación de PDF y Medición de Tiempo (< 5 s) ─────────────

    @Test
    @DisplayName("Generar PDF exitoso en menos de 5 segundos con formato válido (%PDF)")
    void generarPdf_exitoso_enMenosDe5Segundos_yFormatoValido() {
        when(casoRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.of(bitacora));

        RegistroMedico reg1 = RegistroMedico.builder()
                .id(1L)
                .fecha(Instant.now())
                .diagnostico("Herida superficial")
                .procedimiento("Limpieza y sutura")
                .tratamiento("Antibiótico")
                .medicamentos("Amoxicilina 500mg")
                .firmante(veterinario)
                .fechaFirma(Instant.now())
                .build();

        when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(20L))
                .thenReturn(List.of(reg1));

        long inicio = System.currentTimeMillis();
        byte[] pdf = pdfWorker.generarResumenClinico(10L);
        long duracion = System.currentTimeMillis() - inicio;

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // Validar que se generó en menos de 5000 ms (Subtarea 16.5)
        assertTrue(duracion < 5000, "La generación del PDF tomó " + duracion + " ms, debe ser menor a 5000 ms");

        // Validar firma de archivo PDF
        String header = new String(pdf, 0, 4);
        assertEquals("%PDF", header);
    }

    // ── 2. Validación de Permisos de Descarga (Ley 1581 / Ley 576) ─────

    @Test
    @DisplayName("Permiso concedido: Veterinario asignado puede descargar PDF")
    void generarPdf_permisoConcedido_veterinarioAsignado() {
        when(casoRepository.existsById(10L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.of(bitacora));
        when(usuarioRepository.findByCorreo("vet@test.co")).thenReturn(Optional.of(veterinario));

        when(casoRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(20L)).thenReturn(List.of());

        byte[] pdf = bitacoraService.generarPdfResumen(10L, "vet@test.co");
        assertNotNull(pdf);
    }

    @Test
    @DisplayName("Permiso concedido: Reportante del caso puede descargar PDF")
    void generarPdf_permisoConcedido_reportante() {
        when(casoRepository.existsById(10L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.of(bitacora));
        when(usuarioRepository.findByCorreo("reportante@test.co")).thenReturn(Optional.of(reportante));

        when(casoRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(20L)).thenReturn(List.of());

        byte[] pdf = bitacoraService.generarPdfResumen(10L, "reportante@test.co");
        assertNotNull(pdf);
    }

    @Test
    @DisplayName("Permiso concedido: Fundación custodia puede descargar PDF")
    void generarPdf_permisoConcedido_fundacionCustodia() {
        when(casoRepository.existsById(10L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.of(bitacora));
        when(usuarioRepository.findByCorreo("fundacion@test.co")).thenReturn(Optional.of(fundacionUsuario));

        when(casoRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(20L)).thenReturn(List.of());

        byte[] pdf = bitacoraService.generarPdfResumen(10L, "fundacion@test.co");
        assertNotNull(pdf);
    }

    @Test
    @DisplayName("Permiso concedido: Administrador puede descargar PDF")
    void generarPdf_permisoConcedido_administrador() {
        when(casoRepository.existsById(10L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.of(bitacora));
        when(usuarioRepository.findByCorreo("admin@test.co")).thenReturn(Optional.of(admin));

        when(casoRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(20L)).thenReturn(List.of());

        byte[] pdf = bitacoraService.generarPdfResumen(10L, "admin@test.co");
        assertNotNull(pdf);
    }

    @Test
    @DisplayName("Permiso denegado: Ciudadano ajeno no puede descargar PDF (403 Forbidden)")
    void generarPdf_permisoDenegado_ciudadanoAjeno_retorna403() {
        when(casoRepository.existsById(10L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.of(bitacora));
        when(usuarioRepository.findByCorreo("ajeno@test.co")).thenReturn(Optional.of(ciudadanoAjeno));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> bitacoraService.generarPdfResumen(10L, "ajeno@test.co"));

        assertTrue(ex.getMessage().contains("No tiene permisos para consultar la bitácora"));
    }

    @Test
    @DisplayName("Error 404: Caso inexistente al solicitar PDF")
    void generarPdf_casoInexistente_retorna404() {
        when(casoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> bitacoraService.generarPdfResumen(999L, "admin@test.co"));
    }

    @Test
    @DisplayName("Error 404: Caso sin bitácora médica al solicitar PDF")
    void generarPdf_casoSinBitacora_retorna404() {
        when(casoRepository.existsById(10L)).thenReturn(true);
        when(bitacoraMedicaRepository.findByCasoId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bitacoraService.generarPdfResumen(10L, "admin@test.co"));
    }
}
