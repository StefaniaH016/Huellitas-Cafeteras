package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import co.edu.uniquindio.huellitas.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Worker W-06: Procesamiento y vinculación de fotografías de reportes.
 * 
 * Según la arquitectura documentada en Soft III, los workers W-02, W-03, W-05 y W-06
 * operan de forma síncrona dentro del ciclo de vida de la petición HTTP, evitando
 * sobrecarga innecesaria de infraestructura de mensajería (colas/RabbitMQ/Kafka) para
 * casos donde el cliente necesita de forma inmediata la confirmación y URL segura del recurso.
 */
@Component
@RequiredArgsConstructor
public class ImagenWorker {

    private final CloudinaryService cloudinaryService;

    public ImagenResponseDTO procesarYVincularFoto(MultipartFile archivo, Long casoId) {
        return cloudinaryService.subirImagen(archivo, casoId);
    }
}
