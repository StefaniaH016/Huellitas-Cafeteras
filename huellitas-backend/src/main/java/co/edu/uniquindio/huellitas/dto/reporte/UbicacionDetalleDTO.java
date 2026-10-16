package co.edu.uniquindio.huellitas.dto.reporte;

import lombok.Builder;

@Builder
public record UbicacionDetalleDTO(
        Long departamentoId,
        String departamento,
        Long municipioId,
        String municipio,
        Long barrioId,
        String barrio,
        String direccionReferencia
) {}
