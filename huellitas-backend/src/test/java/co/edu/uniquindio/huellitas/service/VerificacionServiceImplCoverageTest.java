package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.VerificacionMapper;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.impl.VerificacionServiceImpl;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificacionServiceImplCoverageTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OrganizacionRepository organizacionRepository;
    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private NotificacionWorker notificacionWorker;

    private VerificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VerificacionServiceImpl(
                usuarioRepository,
                organizacionRepository,
                municipioRepository,
                new VerificacionMapper(),
                notificacionWorker);
    }

    @Test
    void resolverVerificacionRechazaFundacionYApruebaVeterinaria() {
        Fundacion fundacion = Fundacion.builder()
                .id(10L)
                .nombre("Refugio")
                .estadoVerificacion(EstadoVerificacion.PENDIENTE)
                .aceptaCustodia(true)
                .capacidadAlbergue(10)
                .build();
        Usuario contactoFundacion = Usuario.builder()
                .id(2L)
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacion)
                .build();
        Usuario admin = Usuario.builder().id(1L).correo("admin@test.com").build();

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(contactoFundacion));
        when(usuarioRepository.findByCorreo("admin@test.com")).thenReturn(Optional.of(admin));
        when(organizacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var rechazo = service.resolverVerificacion(
                2L, new DecisionVerificacionDTO("RECHAZAR", "Falta firma"), "ADMIN@test.com ");
        assertThat(rechazo.estado()).isEqualTo("RECHAZADA");
        assertThat(rechazo.verificado()).isFalse();
        assertThat(fundacion.isAceptaCustodia()).isFalse();
        assertThat(fundacion.getCapacidadAlbergue()).isZero();
        verify(notificacionWorker).notificarVerificacionOrganizacion(contactoFundacion, false);

        Veterinaria vet = Veterinaria.builder().id(20L).nombre("Vet").estadoVerificacion(EstadoVerificacion.PENDIENTE).build();
        Usuario contactoVet = Usuario.builder().id(3L).rol(Rol.VETERINARIA).organizacion(vet).build();
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(contactoVet));

        var aprobacionVet = service.resolverVerificacion(
                3L, new DecisionVerificacionDTO("APROBAR", null), "admin@test.com");
        assertThat(aprobacionVet.estado()).isEqualTo("VERIFICADA");
        assertThat(aprobacionVet.verificado()).isTrue();
        verify(notificacionWorker).notificarVerificacionOrganizacion(contactoVet, true);
    }

    @Test
    void resolverVerificacionValidaNoEncontradosYDesajusteRolOrganizacion() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.resolverVerificacion(
                404L, new DecisionVerificacionDTO("APROBAR", null), "admin@test.com"));

        Usuario contacto = Usuario.builder()
                .id(2L)
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(Veterinaria.builder().build())
                .build();
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(contacto));
        when(usuarioRepository.findByCorreo("no@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.resolverVerificacion(
                2L, new DecisionVerificacionDTO("APROBAR", null), "no@test.com"));

        Usuario admin = Usuario.builder().id(1L).correo("admin@test.com").build();
        when(usuarioRepository.findByCorreo("admin@test.com")).thenReturn(Optional.of(admin));
        assertThrows(ConflictException.class, () -> service.resolverVerificacion(
                2L, new DecisionVerificacionDTO("APROBAR", null), "admin@test.com"));

        Usuario vetConFundacion = Usuario.builder()
                .id(3L)
                .rol(Rol.VETERINARIA)
                .organizacion(Fundacion.builder().build())
                .build();
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(vetConFundacion));
        assertThrows(ConflictException.class, () -> service.resolverVerificacion(
                3L, new DecisionVerificacionDTO("APROBAR", null), "admin@test.com"));
    }

    @Test
    void solicitarRevisionCubreErroresYVeterinariaSinOrganizacion() {
        MockMultipartFile doc = new MockMultipartFile(
                "documentoSoporte", "soporte.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.UTF_8));

        when(usuarioRepository.findByCorreo("no@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.solicitarRevision("no@test.com", doc, null, null, null));

        Usuario ciudadano = Usuario.builder().id(1L).correo("c@test.com").rol(Rol.CIUDADANO_VOLUNTARIO).build();
        when(usuarioRepository.findByCorreo("c@test.com")).thenReturn(Optional.of(ciudadano));
        assertThrows(ConflictException.class, () -> service.solicitarRevision("c@test.com", doc, null, null, null));

        Usuario vetVerificada = Usuario.builder()
                .id(2L)
                .correo("v@test.com")
                .rol(Rol.VETERINARIA)
                .organizacion(Veterinaria.builder().estadoVerificacion(EstadoVerificacion.VERIFICADA).build())
                .build();
        when(usuarioRepository.findByCorreo("v@test.com")).thenReturn(Optional.of(vetVerificada));
        assertThrows(ConflictException.class, () -> service.solicitarRevision("v@test.com", doc, null, null, null));

        Usuario vetInconsistente = Usuario.builder()
                .id(3L)
                .correo("vi@test.com")
                .rol(Rol.VETERINARIA)
                .organizacion(Fundacion.builder().estadoVerificacion(EstadoVerificacion.PENDIENTE).build())
                .build();
        when(usuarioRepository.findByCorreo("vi@test.com")).thenReturn(Optional.of(vetInconsistente));
        assertThrows(ConflictException.class, () -> service.solicitarRevision("vi@test.com", doc, null, null, null));

        Usuario vetSinOrg = Usuario.builder().id(4L).correo("vs@test.com").rol(Rol.VETERINARIA).build();
        when(usuarioRepository.findByCorreo("vs@test.com")).thenReturn(Optional.of(vetSinOrg));
        assertThrows(ConflictException.class, () -> service.solicitarRevision("vs@test.com", doc, "", "900", "Armenia"));

        when(organizacionRepository.existsByNit("900")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.solicitarRevision("vs@test.com", doc, "Clínica", "900", "Armenia"));

        when(organizacionRepository.existsByNit("901")).thenReturn(false);
        when(municipioRepository.findFirstByNombreIgnoreCase("Narnia")).thenReturn(Optional.empty());
        assertThrows(ConflictException.class, () -> service.solicitarRevision("vs@test.com", doc, "Clínica", "901", "Narnia"));

        when(municipioRepository.findFirstByNombreIgnoreCase("Armenia"))
                .thenReturn(Optional.of(Municipio.builder().id(1L).nombre("Armenia").build()));
        when(organizacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.solicitarRevision("vs@test.com", doc, "Clínica Norte", "901", "Armenia");
        assertThat(resp.estado()).isEqualTo("PENDIENTE");
        assertThat(vetSinOrg.getOrganizacion()).isInstanceOf(Veterinaria.class);
        verify(notificacionWorker).notificarSolicitudRevisionOrganizacion(vetSinOrg.getId(), "Clínica Norte");
    }

    @Test
    void obtenerDocumentoSoporteLanzaNotFoundCuandoFaltaUsuarioOrgODocumento() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.obtenerDocumentoSoporte(99L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(Usuario.builder().id(1L).build()));
        assertThrows(ResourceNotFoundException.class, () -> service.obtenerDocumentoSoporte(1L));

        Usuario sinDoc = Usuario.builder().id(2L).organizacion(Fundacion.builder().build()).build();
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(sinDoc));
        assertThrows(ResourceNotFoundException.class, () -> service.obtenerDocumentoSoporte(2L));
    }
}
