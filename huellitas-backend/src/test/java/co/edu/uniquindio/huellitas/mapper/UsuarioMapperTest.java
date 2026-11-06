package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.usuario.UsuarioPerfilDTO;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void toPerfilDTOIncludesOrganizationId() {
        Fundacion fundacion = Fundacion.builder().id(12L).nombre("Huellitas").build();
        Usuario usuario = Usuario.builder()
                .id(7L)
                .nombre("Ana")
                .correo("ana@example.test")
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacion)
                .build();

        UsuarioPerfilDTO perfil = mapper.toPerfilDTO(usuario);

        assertEquals(12L, perfil.organizacionId());
        assertEquals("Huellitas", perfil.organizacionNombre());
    }
}
