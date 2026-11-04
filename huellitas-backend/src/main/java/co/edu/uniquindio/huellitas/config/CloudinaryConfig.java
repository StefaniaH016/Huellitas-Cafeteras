package co.edu.uniquindio.huellitas.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del cliente Cloudinary para Worker W-06.
 * Registra el bean de Cloudinary a partir de las propiedades de entorno,
 * garantizando que nunca se registre ni exponga el api-secret.
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${cloudinary.timeout:30}") int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("cloudinary.timeout debe ser mayor que cero.");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName != null ? cloudName.trim() : "");
        config.put("api_key", apiKey != null ? apiKey.trim() : "");
        config.put("api_secret", apiSecret != null ? apiSecret.trim() : "");
        config.put("secure", true);
        config.put("timeout", timeoutSeconds);
        return new Cloudinary(config);
    }
}
