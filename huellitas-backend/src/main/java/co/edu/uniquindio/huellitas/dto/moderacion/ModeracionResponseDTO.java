package co.edu.uniquindio.huellitas.dto.moderacion;

public record ModeracionResponseDTO(
        Long casoId,
        String decision,
        String fechaRevision
) {}
