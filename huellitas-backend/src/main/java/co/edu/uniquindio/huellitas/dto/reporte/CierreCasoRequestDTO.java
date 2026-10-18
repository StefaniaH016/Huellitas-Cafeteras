package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.ResultadoCierre;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CierreCasoRequestDTO(
        @NotNull(message = "El motivo de cierre es obligatorio")
        ResultadoCierre motivoCierre,

        String observacion,

        String entregadoA
) {}
