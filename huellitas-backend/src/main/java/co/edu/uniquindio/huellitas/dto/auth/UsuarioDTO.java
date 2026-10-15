package co.edu.uniquindio.huellitas.dto.auth;

import co.edu.uniquindio.huellitas.entity.enums.Rol;

public record UsuarioDTO(
        Long id,
        String nombre,
        String correo,
        Rol rol,
        boolean verificado
) {}
