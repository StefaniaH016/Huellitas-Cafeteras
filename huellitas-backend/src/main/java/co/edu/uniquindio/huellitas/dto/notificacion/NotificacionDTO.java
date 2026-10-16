package co.edu.uniquindio.huellitas.dto.notificacion;

public record NotificacionDTO(
        Long id,
        String tipo,
        String mensaje,
        boolean leida,
        String fecha,
        Long casoId
) {}
