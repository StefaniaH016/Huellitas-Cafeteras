package co.edu.uniquindio.huellitas.dto.bitacora;

public record BitacoraResponseDTO(
        Long id,
        Long casoId,
        Long veterinariaId,
        String fechaApertura,
        String estado
) {}
