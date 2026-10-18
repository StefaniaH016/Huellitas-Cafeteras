package co.edu.uniquindio.huellitas.dto.bitacora;

import jakarta.validation.constraints.NotBlank;

public record EvolucionDTO(
        @NotBlank(message = "El diagnóstico es obligatorio")
        String diagnostico,

        String procedimientos,

        String tratamiento,

        String medicamentos,

        String observaciones
) {}
