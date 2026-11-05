package co.edu.uniquindio.huellitas.dto.bitacora;

/**
 * DTO para alimentar el panel de veterinaria (HU 14 subtarea 14.5 y HU 15).
 */
public record PacienteResponseDTO(
        Long id,
        Long casoId,
        String nombre,
        String especie,
        String raza,
        String sexo,
        String descripcion,
        String urgencia,
        String estado,
        String municipio,
        String barrio,
        String foto,
        String fechaApertura,
        boolean altaMedica,
        String fechaAlta,
        String proximoControl
) {}
