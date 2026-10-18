package co.edu.uniquindio.huellitas.dto.reporte;

import lombok.Builder;

@Builder
public record ResumenMedicoDTO(
        Long id,
        String fechaApertura,
        boolean altaMedica,
        String fechaAlta,
        String veterinariaNombre
) {}
