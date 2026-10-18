package co.edu.uniquindio.huellitas.dto.bitacora;

import jakarta.validation.constraints.NotBlank;

public record AltaMedicaDTO(
        @NotBlank(message = "La condición de alta es obligatoria")
        String condicionAlta,

        String indicaciones
) {}
