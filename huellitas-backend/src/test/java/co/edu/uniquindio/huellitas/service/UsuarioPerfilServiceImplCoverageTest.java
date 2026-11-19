package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.usuario.ActualizarPerfilDTO;
import co.edu.uniquindio.huellitas.dto.usuario.UsuarioPerfilDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.UsuarioMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.impl.UsuarioPerfilServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPerfilServiceImplCoverageTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BarrioRepository barrioRepository;

    private UsuarioPerfilServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioPerfilServiceImpl(usuarioRepository, barrioRepository, new UsuarioMapper());
    }

    @Test
    void obtenerPerfilRetornaDTOYNuevaExcepcionSiNoExiste() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Laura")
                .correo("laura@test.com")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .verificado(true)
                .disponibleRescate(true)
                .build();
        when(usuarioRepository.findByCorreo("laura@test.com")).thenReturn(Optional.of(usuario));

        UsuarioPerfilDTO perfil = service.obtenerPerfil(" LAURA@test.com ");
        assertThat(perfil.nombre()).isEqualTo("Laura");
        assertThat(perfil.disponibleRescate()).isTrue();

        when(usuarioRepository.findByCorreo("desconocido@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPerfil("desconocido@test.com"));
    }

    @Test
    void actualizarPerfilCubreNombreTelefonoBarrioYErrores() {
        Municipio armenia = Municipio.builder()
                .id(4L)
                .nombre("Armenia")
                .departamento(Departamento.builder().id(1L).nombre("Quindío").build())
                .build();
        Barrio granada = Barrio.builder().id(20L).nombre("Granada").municipio(armenia).build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Original")
                .correo("ana@test.com")
                .telefono("3000000000")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();

        when(usuarioRepository.findByCorreo("ana@test.com")).thenReturn(Optional.of(usuario));
        when(barrioRepository.findById(20L)).thenReturn(Optional.of(granada));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioPerfilDTO actualizado = service.actualizarPerfil(
                "ANA@test.com", new ActualizarPerfilDTO("  Ana María ", " 3112223344 ", 20L, false));

        assertThat(actualizado.nombre()).isEqualTo("Ana María");
        assertThat(actualizado.telefono()).isEqualTo("3112223344");
        assertThat(actualizado.barrioId()).isEqualTo(20L);
        assertThat(actualizado.municipioId()).isEqualTo(4L);
        assertThat(actualizado.departamentoId()).isEqualTo(1L);

        UsuarioPerfilDTO sinCambiarNombre = service.actualizarPerfil(
                "ana@test.com", new ActualizarPerfilDTO("   ", null, null, null));
        assertThat(sinCambiarNombre.nombre()).isEqualTo("Ana María");

        when(barrioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.actualizarPerfil(
                "ana@test.com", new ActualizarPerfilDTO("Ana", null, 999L, null)));

        when(usuarioRepository.findByCorreo("no@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.actualizarPerfil(
                "no@test.com", new ActualizarPerfilDTO("Ana", null, null, null)));
    }
}
