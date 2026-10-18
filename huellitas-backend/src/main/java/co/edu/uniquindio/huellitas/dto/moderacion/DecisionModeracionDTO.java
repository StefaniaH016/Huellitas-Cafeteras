package co.edu.uniquindio.huellitas.dto.moderacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DecisionModeracionDTO(
        @NotBlank(message = "La decisión es obligatoria")
        @Pattern(regexp = "^(MANTENER|OCULTAR)$", message = "La decisión debe ser MANTENER u OCULTAR")
        String decision,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {}
