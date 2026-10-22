package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.impl.VerificacionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificacionServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VerificacionServiceImpl verificacionService;

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
}
