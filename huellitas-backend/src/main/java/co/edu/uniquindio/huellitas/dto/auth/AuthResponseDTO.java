package co.edu.uniquindio.huellitas.dto.auth;

import co.edu.uniquindio.huellitas.entity.enums.Rol;

public record AuthResponseDTO(
        String token,
        String email,
        String nombre,
        Rol rol,
        Long usuarioId
) {}
