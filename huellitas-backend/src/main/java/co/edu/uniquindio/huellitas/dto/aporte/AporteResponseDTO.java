package co.edu.uniquindio.huellitas.dto.aporte;

import java.util.List;

public record AporteResponseDTO(
        Long id,
        Long casoId,
        Long autorId,
        String descripcion,
        String ubicacionTexto,
        List<String> fotografias,
        String fecha
) {}
