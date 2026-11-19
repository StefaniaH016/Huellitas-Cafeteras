package co.edu.uniquindio.huellitas.config;

import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.FotoRepository;
import co.edu.uniquindio.huellitas.service.CloudinaryService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Configuración de test que proporciona un mock de CloudinaryService.
 * Evita intentar conectarse al servicio real de Cloudinary durante los tests.
 */
@TestConfiguration
@Profile("test")
public class TestCloudinaryConfig {

    @Bean
    @Primary
    public CloudinaryService testCloudinaryService(
            FotoRepository fotoRepository,
            CasoRepository casoRepository) {
        return new CloudinaryService() {
            @Override
            public ImagenResponseDTO subirImagen(MultipartFile archivo, Long casoId) {
                if (archivo == null || archivo.isEmpty()) {
                    throw new IllegalArgumentException("El archivo es obligatorio");
                }

                // Generar URL y publicId fake pero válidos
                String publicId = "test/" + UUID.randomUUID().toString();
                String url = "https://res.cloudinary.com/test-cloud/image/upload/" + publicId + ".jpg";

                // Si no hay casoId, solo retornar URL sin persistir en BD
                if (casoId == null) {
                    return new ImagenResponseDTO(url, publicId);
                }

                // Validar que el caso exista (lanzar excepción si no existe)
                Caso caso = casoRepository.findById(casoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

                // Crear y guardar la foto en la BD de test
                Foto foto = new Foto();
                foto.setUrl(url);
                foto.setPublicId(publicId);
                foto.setCaso(caso);

                // Primera foto del caso debe ser principal
                List<Foto> fotosExistentes = fotoRepository.findByCasoId(casoId);
                foto.setPrincipal(fotosExistentes.isEmpty());

                fotoRepository.save(foto);

                return new ImagenResponseDTO(url, publicId);
            }

            @Override
            public void eliminarFotos(List<Foto> fotos) {
                // En tests, simplemente eliminar de BD sin llamar a Cloudinary
                if (fotos != null && !fotos.isEmpty()) {
                    fotoRepository.deleteAll(fotos);
                }
            }
        };
    }
}
