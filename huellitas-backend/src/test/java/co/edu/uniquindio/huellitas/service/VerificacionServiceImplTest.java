package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.DocumentoSoporte;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.service.impl.VerificacionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import co.edu.uniquindio.huellitas.mapper.VerificacionMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class VerificacionServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OrganizacionRepository organizacionRepository;

    @Mock
    private MunicipioRepository municipioRepository;

    @Mock
    private NotificacionWorker notificacionWorker;

    private VerificacionMapper verificacionMapper = new VerificacionMapper();

    private VerificacionServiceImpl verificacionService;

    @BeforeEach
    void setUp() {
        verificacionService = new VerificacionServiceImpl(
                usuarioRepository,
                organizacionRepository,
                municipioRepository,
                verificacionMapper,
                notificacionWorker);
    }

    @Test
    void listsOnlyPendingInstitutionalUsers() {
        Usuario independiente = Usuario.builder()
                .id(1L)
                .nombre("Fundación independiente")
                .rol(Rol.FUNDACION_REFUGIO)
                .build();
        Organizacion organizacionPendiente = Organizacion.builder()
                .estadoVerificacion(EstadoVerificacion.PENDIENTE)
                .build();
        Usuario usuarioInstitucional = Usuario.builder()
                .id(2L)
                .nombre("Contacto")
                .rol(Rol.VETERINARIA)
                .organizacion(organizacionPendiente)
                .build();
        Organizacion organizacionVerificada = Organizacion.builder()
                .nombre("Veterinaria verificada")
                .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                .build();
        Usuario usuarioVerificado = Usuario.builder()
                .id(3L)
                .nombre("Contacto verificado")
                .rol(Rol.VETERINARIA)
                .organizacion(organizacionVerificada)
                .build();

        when(usuarioRepository.findByVerificadoFalseAndRolIn(
                List.of(Rol.FUNDACION_REFUGIO, Rol.VETERINARIA)))
                .thenReturn(List.of(independiente, usuarioInstitucional, usuarioVerificado));

        List<VerificacionPendienteDTO> pendientes = verificacionService.listarPendientes();

        assertThat(pendientes).containsExactly(
                new VerificacionPendienteDTO(
                        1L, "Fundación independiente", "FUNDACION_REFUGIO", "PENDIENTE"),
                new VerificacionPendienteDTO(2L, "Contacto", "VETERINARIA", "PENDIENTE"));
        verify(usuarioRepository).findByVerificadoFalseAndRolIn(
                List.of(Rol.FUNDACION_REFUGIO, Rol.VETERINARIA));
    }

    @Test
    void approvalPersistsOrganizationEligibilityAndNotifiesItsContact() {
        Fundacion fundacion = Fundacion.builder()
                .id(20L)
                .nombre("Fundación Huellitas")
                .estadoVerificacion(EstadoVerificacion.PENDIENTE)
                .capacidadAlbergue(0)
                .aceptaCustodia(false)
                .build();
        Usuario contacto = Usuario.builder()
                .id(2L)
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacion)
                .build();
        Usuario admin = Usuario.builder().id(1L).correo("admin@test.com").build();
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(contacto));
        when(usuarioRepository.findByCorreo("admin@test.com")).thenReturn(java.util.Optional.of(admin));
        when(organizacionRepository.save(fundacion)).thenReturn(fundacion);
        when(usuarioRepository.save(contacto)).thenReturn(contacto);

        var response = verificacionService.resolverVerificacion(
                2L,
                new DecisionVerificacionDTO("APROBAR", null),
                "admin@test.com");

        assertThat(response.estado()).isEqualTo("VERIFICADA");
        assertThat(response.verificado()).isTrue();
        assertThat(contacto.isVerificado()).isTrue();
        assertThat(fundacion.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADA);
        assertThat(fundacion.isAceptaCustodia()).isTrue();
        assertThat(fundacion.getCapacidadAlbergue()).isEqualTo(10);
        verify(organizacionRepository).save(fundacion);
        verify(usuarioRepository).save(contacto);
        verify(notificacionWorker).notificarVerificacionOrganizacion(contacto, true);
    }

    @Test
    void cannotApproveInstitutionalUserWithoutOrganization() {
        Usuario contacto = Usuario.builder().id(2L).rol(Rol.FUNDACION_REFUGIO).build();
        Usuario admin = Usuario.builder().id(1L).correo("admin@test.com").build();
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(contacto));
        when(usuarioRepository.findByCorreo("admin@test.com")).thenReturn(java.util.Optional.of(admin));

        assertThrows(ConflictException.class, () -> verificacionService.resolverVerificacion(
                2L,
                new DecisionVerificacionDTO("APROBAR", null),
                "admin@test.com"));

        verifyNoInteractions(organizacionRepository, notificacionWorker);
    }

        @Test
        void cannotResolveVerificationForAnAdminAccount() {
                Usuario admin = Usuario.builder()
                                .id(1L)
                                .correo("admin@test.com")
                                .rol(Rol.ADMINISTRADOR)
                                .build();
                when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(admin));

                assertThrows(ConflictException.class, () -> verificacionService.resolverVerificacion(
                                1L,
                                new DecisionVerificacionDTO("APROBAR", null),
                                "admin@test.com"));

                verify(usuarioRepository, never()).save(admin);
                verifyNoInteractions(organizacionRepository, notificacionWorker);
        }

    @Test
    void rejectedOrganizationCanSubmitNewSupportForReview() {
        Fundacion fundacion = Fundacion.builder()
                .id(20L)
                .nombre("Fundación Huellitas")
                .nit("900123")
                .estadoVerificacion(EstadoVerificacion.RECHAZADA)
                .build();
        Usuario contacto = Usuario.builder()
                .id(2L)
                .correo("fundacion@test.com")
                .rol(Rol.FUNDACION_REFUGIO)
                .verificado(false)
                .organizacion(fundacion)
                .build();
        when(usuarioRepository.findByCorreo("fundacion@test.com"))
                .thenReturn(java.util.Optional.of(contacto));
        when(organizacionRepository.save(fundacion)).thenReturn(fundacion);
        when(usuarioRepository.save(contacto)).thenReturn(contacto);
        MockMultipartFile documento = new MockMultipartFile(
                "documentoSoporte",
                "certificado.pdf",
                "application/pdf",
                "%PDF-1.7 soporte".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var response = verificacionService.solicitarRevision(
                "fundacion@test.com", documento, null, null, null);

        assertThat(response.estado()).isEqualTo("PENDIENTE");
        assertThat(response.verificado()).isFalse();
        assertThat(fundacion.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE);
        assertThat(fundacion.getDocumentoSoporteNombre()).isEqualTo("certificado.pdf");
        assertThat(fundacion.getDocumentoSoporteContentType()).isEqualTo("application/pdf");
        assertThat(fundacion.getDocumentoSoporte().getContenido())
                .isEqualTo("%PDF-1.7 soporte".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        verify(organizacionRepository).save(fundacion);
        verify(usuarioRepository).save(contacto);
        verify(notificacionWorker).notificarSolicitudRevisionOrganizacion(contacto, "Fundación Huellitas");
    }

    @Test
    void institutionalAccountWithoutOrganizationCanCreateAndSubmitItsOrganization() {
        Usuario contacto = Usuario.builder()
                .id(3L)
                .correo("antigua@test.com")
                .rol(Rol.FUNDACION_REFUGIO)
                .verificado(false)
                .build();
        Municipio municipio = Municipio.builder().id(1L).nombre("Armenia").build();
        when(usuarioRepository.findByCorreo("antigua@test.com"))
                .thenReturn(java.util.Optional.of(contacto));
        when(organizacionRepository.existsByNit("900123"))
                .thenReturn(false);
        when(municipioRepository.findFirstByNombreIgnoreCase("Armenia"))
                .thenReturn(java.util.Optional.of(municipio));
        when(organizacionRepository.save(any(Organizacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.save(contacto)).thenReturn(contacto);
        MockMultipartFile documento = new MockMultipartFile(
                "documentoSoporte",
                "certificado.pdf",
                "application/pdf",
                "%PDF-1.7 soporte".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var response = verificacionService.solicitarRevision(
                "antigua@test.com",
                documento,
                "Fundación Huellitas",
                "900123",
                "Armenia");

        assertThat(response.estado()).isEqualTo("PENDIENTE");
        assertThat(contacto.getOrganizacion()).isInstanceOf(Fundacion.class);
        assertThat(contacto.getOrganizacion().getNombre()).isEqualTo("Fundación Huellitas");
        assertThat(contacto.getOrganizacion().getDocumentoSoporteNombre()).isEqualTo("certificado.pdf");
        verify(organizacionRepository).save(any(Fundacion.class));
        verify(usuarioRepository).save(contacto);
    }

    @Test
    void adminCanRetrieveAttachedSupportDocument() {
        byte[] contenido = "%PDF-1.7 soporte".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Fundacion fundacion = Fundacion.builder()
                .documentoSoporte(DocumentoSoporte.builder()
                        .nombreArchivo("certificado.pdf")
                        .tipoContenido("application/pdf")
                        .contenido(contenido)
                        .build())
                .build();
        Usuario contacto = Usuario.builder()
                .id(2L)
                .organizacion(fundacion)
                .build();
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(contacto));

        var documento = verificacionService.obtenerDocumentoSoporte(2L);

        assertThat(documento.nombreArchivo()).isEqualTo("certificado.pdf");
        assertThat(documento.tipoContenido()).isEqualTo("application/pdf");
        assertThat(documento.contenido()).isEqualTo(contenido);
    }
}
