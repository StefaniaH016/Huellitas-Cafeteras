package co.edu.uniquindio.huellitas.dto.moderacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportarCasoDTO(
        @NotBlank(message = "El motivo del reporte es obligatorio")
        @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
        String motivo
) {}
