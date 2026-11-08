package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.mapper.UsuarioMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.security.JwtUtils;
import co.edu.uniquindio.huellitas.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BarrioRepository barrioRepository;
    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private OrganizacionRepository organizacionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserDetailsService userDetailsService;

    private AuthServiceImpl authService;
    private Municipio municipio;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                usuarioRepository,
                barrioRepository,
                municipioRepository,
                organizacionRepository,
                passwordEncoder,
                authenticationManager,
                jwtUtils,
                userDetailsService,
                new UsuarioMapper());
        municipio = Municipio.builder()
                .id(4L)
                .nombre("Armenia")
                .departamento(Departamento.builder().id(1L).nombre("Quindío").build())
                .build();
    }

    @Test
    void registrationCreatesPendingFoundationWithOrganizationAndMunicipality() {
        when(municipioRepository.findFirstByNombreIgnoreCase("Armenia"))
                .thenReturn(Optional.of(municipio));
        when(organizacionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");

        authService.registro(new RegistroUsuarioDTO(
                "Contacto Patitas",
                "contacto@patitas.co",
                "Password123!",
                "3001234567",
                null,
                Rol.FUNDACION_REFUGIO,
                "Fundación Patitas",
                "900123",
                null,
                "Armenia"));

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario guardado = usuarioCaptor.getValue();
        assertThat(guardado.getOrganizacion()).isInstanceOf(Fundacion.class);
        Fundacion fundacion = (Fundacion) guardado.getOrganizacion();
        assertThat(fundacion.getNombre()).isEqualTo("Fundación Patitas");
        assertThat(fundacion.getNit()).isEqualTo("900123");
        assertThat(fundacion.getMunicipio()).isEqualTo(municipio);
        assertThat(fundacion.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE);
        assertThat(fundacion.isAceptaCustodia()).isFalse();
        assertThat(guardado.getMunicipioInteres()).isEqualTo(municipio);
        verify(organizacionRepository).save(fundacion);
    }

    @Test
    void institutionalRegistrationRequiresOrganizationData() {
        assertThrows(IllegalArgumentException.class, () -> authService.registro(new RegistroUsuarioDTO(
                "Contacto Patitas",
                "contacto@patitas.co",
                "Password123!",
                "3001234567",
                null,
                Rol.FUNDACION_REFUGIO)));
    }

    @Test
    void institutionalRegistrationPersistsTheSupportDocument() {
        when(municipioRepository.findFirstByNombreIgnoreCase("Armenia"))
                .thenReturn(Optional.of(municipio));
        when(organizacionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        MockMultipartFile documento = new MockMultipartFile(
                "documentoSoporte",
                "resolucion.pdf",
                "application/pdf",
                "%PDF-1.7 soporte".getBytes(StandardCharsets.UTF_8));

        authService.registro(new RegistroUsuarioDTO(
                "Contacto Patitas",
                "contacto@patitas.co",
                "Password123!",
                "3001234567",
                null,
                Rol.FUNDACION_REFUGIO,
                "Fundación Patitas",
                "900123",
                null,
                "Armenia"), documento);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Fundacion fundacion = (Fundacion) usuarioCaptor.getValue().getOrganizacion();
        assertThat(fundacion.getDocumentoSoporteNombre()).isEqualTo("resolucion.pdf");
        assertThat(fundacion.getDocumentoSoporte().getContenido())
                .isEqualTo("%PDF-1.7 soporte".getBytes(StandardCharsets.UTF_8));
        verify(organizacionRepository, org.mockito.Mockito.times(2)).save(fundacion);
    }
}
