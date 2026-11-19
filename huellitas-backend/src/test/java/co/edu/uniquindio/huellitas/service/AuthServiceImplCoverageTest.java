package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.auth.AuthResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.auth.UsuarioDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.UsuarioMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.security.JwtUtils;
import co.edu.uniquindio.huellitas.service.impl.AuthServiceImpl;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplCoverageTest {

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
    @Mock
    private NotificacionWorker notificacionWorker;

    private AuthServiceImpl authService;
    private Municipio armenia;
    private Barrio centro;

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
                new UsuarioMapper(),
                notificacionWorker);

        armenia = Municipio.builder()
                .id(1L)
                .nombre("Armenia")
                .departamento(Departamento.builder().id(1L).nombre("Quindío").build())
                .build();
        centro = Barrio.builder().id(10L).nombre("Centro").municipio(armenia).build();
    }

    @Test
    void registroCiudadanoConRolNuloYBarrioValido() {
        when(usuarioRepository.existsByCorreo("ana@test.com")).thenReturn(false);
        when(barrioRepository.findById(10L)).thenReturn(Optional.of(centro));
        when(passwordEncoder.encode("Clave123!")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        RegistroResponseDTO resp = authService.registro(new RegistroUsuarioDTO(
                "Ana López", "ana@test.com", "Clave123!", "3001112233", 10L, null));

        assertThat(resp.id()).isEqualTo(5L);
        assertThat(resp.rol()).isEqualTo(Rol.CIUDADANO_VOLUNTARIO);
        assertThat(resp.correo()).isEqualTo("ana@test.com");
    }

    @Test
    void registroVeterinariaConDocumentoLegalPersisteVeterinaria() {
        when(usuarioRepository.existsByCorreo("vet@test.com")).thenReturn(false);
        when(organizacionRepository.existsByNit("900888")).thenReturn(false);
        when(municipioRepository.findFirstByNombreIgnoreCase("Armenia")).thenReturn(Optional.of(armenia));
        when(organizacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("VetClave1!")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroResponseDTO resp = authService.registro(new RegistroUsuarioDTO(
                "Dr. Juan",
                "vet@test.com",
                "VetClave1!",
                "3109998877",
                null,
                Rol.VETERINARIA,
                "Veterinaria Andes",
                "900888",
                "TP-5566",
                "Armenia"));

        assertThat(resp.rol()).isEqualTo(Rol.VETERINARIA);
        verify(organizacionRepository).save(any(Veterinaria.class));
    }

    @Test
    void registroRechazaErroresDeNegocio() {
        when(usuarioRepository.existsByCorreo("duplicado@test.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.registro(new RegistroUsuarioDTO(
                "Dup", "duplicado@test.com", "Clave123!", "3001112233", null, Rol.CIUDADANO_VOLUNTARIO)));

        when(usuarioRepository.existsByCorreo("nuevo@test.com")).thenReturn(false);
        when(barrioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.registro(new RegistroUsuarioDTO(
                "Nuevo", "nuevo@test.com", "Clave123!", "3001112233", 999L, Rol.CIUDADANO_VOLUNTARIO)));

        MockMultipartFile soporte = new MockMultipartFile(
                "documentoSoporte", "doc.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.UTF_8));
        assertThrows(ConflictException.class, () -> authService.registro(new RegistroUsuarioDTO(
                "Ciudadano", "nuevo@test.com", "Clave123!", "3001112233", null, Rol.CIUDADANO_VOLUNTARIO), soporte));

        when(organizacionRepository.existsByNit("900111")).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.registro(new RegistroUsuarioDTO(
                "Vet", "nuevo@test.com", "Clave123!", "3001112233", null,
                Rol.VETERINARIA, "Vet", "900111", null, "Armenia")));

        when(organizacionRepository.existsByNit("900222")).thenReturn(false);
        when(municipioRepository.findFirstByNombreIgnoreCase("Inexistente")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.registro(new RegistroUsuarioDTO(
                "Vet", "nuevo@test.com", "Clave123!", "3001112233", null,
                Rol.VETERINARIA, "Vet", "900222", null, "Inexistente")));
    }

    @Test
    void loginYobtenerUsuarioActualFuncionanYLanzanNotFound() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Camila")
                .correo("camila@test.com")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .build();
        UserDetails userDetails = User.withUsername("camila@test.com")
                .password("encoded")
                .authorities("ROLE_CIUDADANO_VOLUNTARIO")
                .build();

        when(usuarioRepository.findByCorreo("camila@test.com")).thenReturn(Optional.of(usuario));
        when(userDetailsService.loadUserByUsername("camila@test.com")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails, "Camila")).thenReturn("jwt-token-123");

        AuthResponseDTO auth = authService.login(new LoginRequestDTO("Camila@Test.com ", "Clave123!"));
        assertThat(auth.token()).isEqualTo("jwt-token-123");
        assertThat(auth.tipo()).isEqualTo("Bearer");
        assertThat(auth.usuario().correo()).isEqualTo("camila@test.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        UsuarioDTO actual = authService.obtenerUsuarioActual("CAMILA@test.com ");
        assertThat(actual.nombre()).isEqualTo("Camila");

        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.login(new LoginRequestDTO("noexiste@test.com", "x")));
        assertThrows(ResourceNotFoundException.class, () -> authService.obtenerUsuarioActual("noexiste@test.com"));
    }
}
