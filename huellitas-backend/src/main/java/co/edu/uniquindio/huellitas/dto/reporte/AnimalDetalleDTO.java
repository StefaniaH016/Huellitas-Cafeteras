package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.Sexo;
import lombok.Builder;

@Builder
public record AnimalDetalleDTO(
        Long id,
        String nombre,
        Especie especie,
        String raza,
        String color,
        Sexo sexo,
        String edadAproximada,
        String senasParticulares
) {}
