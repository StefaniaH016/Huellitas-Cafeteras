package co.edu.uniquindio.huellitas.dto.aporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CrearAporteDTO(
        @NotBlank(message = "La descripción del aporte es obligatoria")
        String descripcion,

        @Size(max = 300, message = "El punto de referencia no puede exceder 300 caracteres")
        String ubicacionTexto,

        Long barrioId,

        List<String> fotografias
) {}
