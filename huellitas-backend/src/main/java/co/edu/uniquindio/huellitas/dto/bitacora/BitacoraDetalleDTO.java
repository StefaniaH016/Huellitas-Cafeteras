package co.edu.uniquindio.huellitas.dto.bitacora;

import java.util.List;

public record BitacoraDetalleDTO(
        Long id,
        Long casoId,
        String estado,
        List<EvolucionDetalleDTO> evoluciones,
        Object alta
) {}
