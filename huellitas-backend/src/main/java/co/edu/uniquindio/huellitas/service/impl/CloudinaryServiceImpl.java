package co.edu.uniquindio.huellitas.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación del servicio de integración con Cloudinary (Worker W-06).
 * Gestiona la validación, compresión, optimización y almacenamiento en Cloudinary,
 * además de la persistencia de fotos asociadas al caso con compensación en caso de fallo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CloudinaryServiceImpl implements CloudinaryService {

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;
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
        // 1. Validación segura del archivo de imagen
        validarArchivo(archivo);

        // 2. Si casoId está presente, validar existencia previa para evitar consumo innecesario de Cloudinary
        Caso caso = null;
        if (casoId != null) {
            caso = casoRepository.findById(casoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));
        }

        // 3. Subir archivo a Cloudinary con optimización (calidad y formato automáticos)
        String cleanCloudName = cloudName != null ? cloudName.trim() : "";
        String url;
        String publicId;

        if (esModoSimulado(cleanCloudName)) {
            publicId = "huellitas_" + UUID.randomUUID();
            url = "https://res.cloudinary.com/huellitas-test/image/upload/v1/" + publicId + ".jpg";
        } else {
            try {
                Map<String, Object> uploadParams = ObjectUtils.asMap(
                        "folder", "huellitas/casos",
                        "resource_type", "image",
                        "transformation", new Transformation<>().quality("auto").fetchFormat("auto")
                );
                Map<?, ?> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), uploadParams);
                url = (String) uploadResult.get("secure_url");
                publicId = (String) uploadResult.get("public_id");
            } catch (IOException | RuntimeException e) {
                log.error("Fallo al subir imagen a Cloudinary: {}", e.getMessage());
                throw new IllegalStateException("No fue posible procesar y optimizar la imagen con Cloudinary", e);
            }
        }

        // 4. Si casoId está presente, persistir Foto asociada con limpieza compensatoria ante fallos
        if (caso != null) {
            try {
                boolean yaTieneFotos = fotoRepository.existsByCasoId(casoId);
                Foto foto = Foto.builder()
                        .url(url)
                        .publicId(publicId)
                        .principal(!yaTieneFotos)
                        .caso(caso)
                        .build();

                fotoRepository.save(foto);
            } catch (Exception ex) {
                log.error("Fallo al persistir la foto en base de datos para el caso {}. Ejecutando limpieza compensatoria en Cloudinary para publicId: {}",
                        casoId, publicId, ex);
                revertirSubidaCloudinary(publicId, cleanCloudName);
                throw ex;
            }
        }

        return new ImagenResponseDTO(url, publicId);
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es obligatorio y no puede estar vacío");
        }

        if (archivo.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de 5 MB");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato de imagen no permitido. Solo se admiten JPG, PNG y WebP");
        }
    }

    boolean esModoSimulado(String cleanCloudName) {
        return cleanCloudName.isBlank() || cleanCloudName.equals("test-cloud");
    }

    void revertirSubidaCloudinary(String publicId, String cleanCloudName) {
        if (publicId == null || publicId.isBlank() || esModoSimulado(cleanCloudName)) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Limpieza compensatoria exitosa en Cloudinary para publicId: {}", publicId);
        } catch (Exception e) {
            log.warn("Fallo durante la limpieza compensatoria en Cloudinary para publicId {}: {}", publicId, e.getMessage());
        }
    }
}
