package co.edu.uniquindio.huellitas.dto.reporte;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ArchivarCasoDTO(
        @NotBlank(message = "El motivo de archivado es obligatorio")
        String motivo
) {}
