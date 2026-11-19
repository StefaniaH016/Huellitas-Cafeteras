package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.usuario.ActualizarPerfilDTO;
import co.edu.uniquindio.huellitas.dto.usuario.UsuarioPerfilDTO;
import co.edu.uniquindio.huellitas.entity.Usuario;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPerfilServiceTest {

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
    void actualizarPerfilPersistsRescueAvailability() {
        Usuario usuario = Usuario.builder()
                .nombre("Ana")
                .correo("ana@example.test")
                .disponibleRescate(false)
                .build();
        when(usuarioRepository.findByCorreo("ana@example.test")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO("Ana", null, null, true);

        UsuarioPerfilDTO actualizado = service.actualizarPerfil("ana@example.test", dto);

        assertTrue(usuario.isDisponibleRescate());
        assertTrue(actualizado.disponibleRescate());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarPerfilLeavesRescueAvailabilityUnchangedWhenOmitted() {
        Usuario usuario = Usuario.builder()
                .nombre("Ana")
                .correo("ana@example.test")
                .disponibleRescate(true)
                .build();
        when(usuarioRepository.findByCorreo("ana@example.test")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO("Ana", null, null, null);

        service.actualizarPerfil("ana@example.test", dto);

        assertTrue(usuario.isDisponibleRescate());
        verify(usuarioRepository).save(usuario);
    }
}
