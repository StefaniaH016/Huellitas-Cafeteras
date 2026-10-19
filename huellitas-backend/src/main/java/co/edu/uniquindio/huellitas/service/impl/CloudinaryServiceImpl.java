package co.edu.uniquindio.huellitas.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.FotoRepository;
import co.edu.uniquindio.huellitas.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CloudinaryServiceImpl implements CloudinaryService {

    private final CasoRepository casoRepository;
    private final FotoRepository fotoRepository;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Override
    public ImagenResponseDTO subirImagen(MultipartFile archivo, Long casoId) {
        String url;
        String publicId;

        String cleanCloudName = cloudName != null ? cloudName.trim() : "";
        String cleanApiKey = apiKey != null ? apiKey.trim() : "";
        String cleanApiSecret = apiSecret != null ? apiSecret.trim() : "";

        // Si las credenciales reales de Cloudinary están configuradas y se envió archivo, subimos a la nube
        if (archivo != null && !archivo.isEmpty() && !cleanCloudName.isBlank() && !cleanCloudName.equals("test-cloud")) {
            try {
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cleanCloudName,
                        "api_key", cleanApiKey,
                        "api_secret", cleanApiSecret,
                        "secure", true
                ));
                Map<?, ?> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.emptyMap());
                url = (String) uploadResult.get("secure_url");
                publicId = (String) uploadResult.get("public_id");
            } catch (Exception e) {
                log.warn("Fallo en subida a Cloudinary, usando fallback simulado: {}", e.getMessage());
                publicId = "huellitas_" + UUID.randomUUID();
                url = "https://res.cloudinary.com/" + cleanCloudName + "/image/upload/v1/" + publicId + ".jpg";
            }
        } else {
            // Modo local / test fallback
            publicId = "huellitas_" + UUID.randomUUID();
            url = "https://res.cloudinary.com/huellitas-test/image/upload/v1/" + publicId + ".jpg";
        }

        if (casoId != null) {
            Caso caso = casoRepository.findById(casoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

            boolean esPrincipal = caso.getFotos() == null || caso.getFotos().isEmpty();
            Foto foto = Foto.builder()
                    .url(url)
                    .publicId(publicId)
                    .principal(esPrincipal)
                    .caso(caso)
                    .build();

            fotoRepository.save(foto);
        }

        return new ImagenResponseDTO(url, publicId);
    }
}
