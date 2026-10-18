package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import lombok.Builder;

@Builder
public record CasoResponseDTO(
        Long id,
        TipoCaso tipoCaso,
        Urgencia urgencia,
        EstadoCaso estado,
        String descripcion,
        String direccionReferencia,
        Long autorId,
        Long barrioId,
        String fechaCreacion
) {}
