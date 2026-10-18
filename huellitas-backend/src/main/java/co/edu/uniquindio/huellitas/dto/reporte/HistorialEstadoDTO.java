package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import lombok.Builder;

@Builder
public record HistorialEstadoDTO(
        Long id,
        EstadoCaso estadoAnterior,
        EstadoCaso estadoNuevo,
        String observacion,
        String fecha
) {}
