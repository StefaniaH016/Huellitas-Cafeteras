package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ActualizarEstadoDTO(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoCaso estado,

        String observacion
) {}
