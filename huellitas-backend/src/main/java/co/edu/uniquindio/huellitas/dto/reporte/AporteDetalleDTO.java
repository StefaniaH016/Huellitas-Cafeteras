package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import lombok.Builder;

import java.util.List;

@Builder
public record AporteDetalleDTO(
        Long id,
        TipoAporte tipo,
        String comentario,
        String fecha,
        String autorNombre,
        String zona,
        String ubicacionTexto,
        List<String> fotografias
) {}
