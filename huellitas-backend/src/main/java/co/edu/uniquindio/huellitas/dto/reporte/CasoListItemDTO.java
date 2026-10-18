package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import lombok.Builder;

@Builder
public record CasoListItemDTO(
        Long id,
        TipoCaso tipoCaso,
        EstadoCaso estado,
        Urgencia urgencia,
        String descripcion,
        Especie especie,
        String fotoPrincipal,
        String departamento,
        String municipio,
        String barrio,
        String fechaCreacion
) {}
