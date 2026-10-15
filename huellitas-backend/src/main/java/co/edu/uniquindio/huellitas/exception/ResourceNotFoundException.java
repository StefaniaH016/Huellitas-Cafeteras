package co.edu.uniquindio.huellitas.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " no encontrado con id: " + id);
    }
}
