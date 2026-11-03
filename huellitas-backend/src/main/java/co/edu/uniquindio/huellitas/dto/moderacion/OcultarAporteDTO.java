package co.edu.uniquindio.huellitas.dto.moderacion;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitar el ocultamiento de un aporte (Seguimiento).
 * Subtarea 13.2
 */
public record OcultarAporteDTO(
        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {}