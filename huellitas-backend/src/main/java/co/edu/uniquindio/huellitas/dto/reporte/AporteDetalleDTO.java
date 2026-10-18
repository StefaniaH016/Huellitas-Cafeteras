package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import lombok.Builder;

@Builder
public record AporteDetalleDTO(
        Long id,
        TipoAporte tipo,
        String comentario,
        String fecha,
        String autorNombre
) {}
