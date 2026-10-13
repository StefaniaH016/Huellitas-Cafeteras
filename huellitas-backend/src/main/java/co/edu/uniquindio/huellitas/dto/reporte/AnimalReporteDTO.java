package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.Sexo;
import jakarta.validation.constraints.*;

/**
 * Sub-DTO con los datos del animal dentro de CrearReporteDTO.
 */
public record AnimalReporteDTO(

        @Size(max = 100)
        String nombre,

        @NotNull(message = "La especie es obligatoria")
        Especie especie,

        @Size(max = 100)
        String raza,

        @Size(max = 100)
        String color,

        Sexo sexo,

        @Size(max = 50)
        String edadAproximada,

        @Size(max = 1000)
        String senasParticulares
) {}
