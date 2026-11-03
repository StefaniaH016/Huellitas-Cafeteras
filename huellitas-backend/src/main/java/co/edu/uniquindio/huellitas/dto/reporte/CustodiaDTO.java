package co.edu.uniquindio.huellitas.dto.reporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitar asumir o liberar la custodia de un caso.
 * Subtarea 13.3
 */
public record CustodiaDTO(
        /**
         * Acción a realizar: "ASUMIR" o "LIBERAR"
         */
        @NotBlank(message = "La acción es obligatoria")
        String accion,

        /**
         * Motivo requerido al liberar la custodia.
         */
        String motivo
) {}