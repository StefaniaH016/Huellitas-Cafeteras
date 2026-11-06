package co.edu.uniquindio.huellitas.dto.verificacion;

public record VerificacionPendienteDTO(
        Long usuarioId,
        String nombreEntidad,
        String rolSolicitado,
        String estado,
        String nit,
        String municipio,
        String documentoSoporteNombre,
        boolean documentoSoporteDisponible
) {
    public VerificacionPendienteDTO(
            Long usuarioId,
            String nombreEntidad,
            String rolSolicitado,
            String estado) {
        this(usuarioId, nombreEntidad, rolSolicitado, estado, null, null, null, false);
    }
}
