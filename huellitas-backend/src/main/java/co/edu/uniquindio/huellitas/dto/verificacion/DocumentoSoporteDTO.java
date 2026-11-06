package co.edu.uniquindio.huellitas.dto.verificacion;

public record DocumentoSoporteDTO(
        String nombreArchivo,
        String tipoContenido,
        byte[] contenido
) {}
