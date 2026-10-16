package co.edu.uniquindio.huellitas.dto.bitacora;

public record AltaMedicaResponseDTO(
        Long casoId,
        String fechaAlta,
        String estadoBitacora
) {}
