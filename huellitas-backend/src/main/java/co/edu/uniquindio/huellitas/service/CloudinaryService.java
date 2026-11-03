package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import co.edu.uniquindio.huellitas.entity.Foto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {
    ImagenResponseDTO subirImagen(MultipartFile archivo, Long casoId);

    void eliminarFotos(List<Foto> fotos);
}
