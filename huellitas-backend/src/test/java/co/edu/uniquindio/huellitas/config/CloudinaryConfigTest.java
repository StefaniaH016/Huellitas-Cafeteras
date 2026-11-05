package co.edu.uniquindio.huellitas.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CloudinaryConfigTest {

    private final CloudinaryConfig cloudinaryConfig = new CloudinaryConfig();

    @Test
    void configuraTimeoutDelClienteCloudinary() {
        Cloudinary cloudinary = cloudinaryConfig.cloudinary("cloud", "key", "secret", 12);

        assertEquals(12, cloudinary.config.timeout);
    }

    @Test
    void rechazaTimeoutNoPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryConfig.cloudinary("cloud", "key", "secret", 0));
    }
}
