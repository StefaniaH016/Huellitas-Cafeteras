package co.edu.uniquindio.huellitas.dto.moderacion;

import jakarta.validation.constraints.NotBlank;

public record MarcarCasoModeracionDTO(
        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {}