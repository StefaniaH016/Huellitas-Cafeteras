package co.edu.uniquindio.huellitas.dto.aporte;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CrearAporteDTO(
        @NotBlank(message = "La descripción del aporte es obligatoria")
        String descripcion,

        String ubicacionTexto,

        Long barrioId,

        List<String> fotografias
) {}
