package co.edu.uniquindio.huellitas.dto.verificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DecisionVerificacionDTO(
        @NotBlank(message = "La decisión es obligatoria")
        @Pattern(regexp = "^(APROBAR|RECHAZAR)$", message = "La decisión debe ser APROBAR o RECHAZAR")
        String decision,

        String observacion
) {}
