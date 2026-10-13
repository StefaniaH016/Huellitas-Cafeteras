package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * DTO para crear un nuevo reporte de caso (HU-10).
 */
public record CrearReporteDTO(

        @NotNull(message = "El tipo de caso es obligatorio")
        TipoCaso tipoCaso,

        @NotNull(message = "La urgencia es obligatoria")
        Urgencia urgencia,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
        String descripcion,

        @Size(max = 300)
        String direccionReferencia,

        /** ID del barrio donde se encontró/reportó el animal. */
        @NotNull(message = "El barrio es obligatorio")
        Long barrioId,

        /** Datos del animal involucrado. */
        @NotNull(message = "Los datos del animal son obligatorios")
        @Valid
        AnimalReporteDTO animal

) {}
