package co.edu.uniquindio.huellitas.dto.reporte;

import lombok.Builder;

@Builder
public record FotoDetalleDTO(
        Long id,
        String url,
        boolean principal
) {}
