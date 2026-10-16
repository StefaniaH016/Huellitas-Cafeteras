package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import lombok.Builder;

@Builder
public record FiltrosCasoDTO(
        Long departamentoId,
        Long municipioId,
        Long barrioId,
        EstadoCaso estado,
        Especie especie,
        Urgencia urgencia,
        TipoCaso tipoCaso
) {}
