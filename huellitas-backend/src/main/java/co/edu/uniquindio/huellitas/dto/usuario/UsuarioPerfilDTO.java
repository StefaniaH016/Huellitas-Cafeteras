package co.edu.uniquindio.huellitas.dto.usuario;

import co.edu.uniquindio.huellitas.entity.enums.Rol;
import lombok.Builder;

@Builder
public record UsuarioPerfilDTO(
        Long id,
        String nombre,
        String correo,
        String telefono,
        Rol rol,
        boolean verificado,
        Long departamentoId,
        Long municipioId,
        Long barrioId
) {}
