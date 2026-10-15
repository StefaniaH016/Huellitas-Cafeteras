package co.edu.uniquindio.huellitas.dto.bitacora;

public record EvolucionDetalleDTO(
        Long id,
        String fecha,
        String diagnostico,
        String tratamiento,
        String observaciones
) {}
