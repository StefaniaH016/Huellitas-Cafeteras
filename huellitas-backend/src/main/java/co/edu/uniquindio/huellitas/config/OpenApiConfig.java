package co.edu.uniquindio.huellitas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para HU-13 y todas las HU anteriores.
 * Subtarea 13.9
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Huellitas Cafeteras API")
                        .version("1.0.0")
                        .description("API REST para la gestión de casos de animales vulnerables y extraviados en el Eje Cafetero. " +
                                "Incluye funcionalidades de HU-13: Seguimiento, custodia y notificaciones.")
                        .contact(new Contact()
                                .name("Equipo Huellitas")
                                .email("soporte@huellitas.org")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token de autenticación")));
    }
}