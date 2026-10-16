package co.edu.uniquindio.huellitas.dto.verificacion;

public record VerificacionPendienteDTO(
        Long usuarioId,
        String nombreEntidad,
        String rolSolicitado,
        String estado
) {}
