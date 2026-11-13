package co.edu.uniquindio.huellitas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuración del bean Clock para facilitar pruebas unitarias.
 *
 * <p>Al inyectar Clock en lugar de usar Instant.now() directamente,
 * permitimos que las pruebas utilicen un Clock fijo o controlado.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
