package co.edu.uniquindio.huellitas.dto.bitacora;

public record EvolucionDetalleDTO(
        Long id,
        String fecha,
        String diagnostico,
        String procedimiento,
        String tratamiento,
        String medicamentos,
        String evolucion,
        String observaciones,
        String firmanteNombre,
        String fechaFirma
) {
    public EvolucionDetalleDTO(Long id, String fecha, String diagnostico, String tratamiento, String observaciones) {
        this(id, fecha, diagnostico, null, tratamiento, null, observaciones, observaciones, null, null);
    }
}
