package co.edu.uniquindio.huellitas.dto.moderacion;

public record CasoModeracionDTO(
        Long casoId,
        String motivo,
        String estadoRevision,
        String fechaCreacion
) {}
