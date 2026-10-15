package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    ImagenResponseDTO subirImagen(MultipartFile archivo, Long casoId);
}
