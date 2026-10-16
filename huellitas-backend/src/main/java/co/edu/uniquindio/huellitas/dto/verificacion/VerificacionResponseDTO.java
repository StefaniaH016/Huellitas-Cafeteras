package co.edu.uniquindio.huellitas.dto.verificacion;

public record VerificacionResponseDTO(
        Long usuarioId,
        String estado,
        boolean verificado
) {}
