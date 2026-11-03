package co.edu.uniquindio.huellitas.exception;

/**
 * Excepción de negocio para errores al integrar con el servicio externo Cloudinary.
 * Extiende RuntimeException para que pueda propagarse sin necesidad de declarar
 * throws en la firma de los métodos de servicio.
 */
public class CloudinaryIntegrationException extends RuntimeException {

    public CloudinaryIntegrationException(String message) {
        super(message);
    }

    public CloudinaryIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}

