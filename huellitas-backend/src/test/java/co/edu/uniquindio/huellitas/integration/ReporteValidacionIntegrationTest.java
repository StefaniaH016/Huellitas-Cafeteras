package co.edu.uniquindio.huellitas.integration;

import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.reporte.AnimalReporteDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para validaciones HTTP de HU-10.
 * 
 * Cubre escenarios de FALLO requeridos por la tarea SQA:
 * - Validaciones de DTOs (422 Unprocessable Entity)
 * - Autenticación (403 Forbidden sin token)
 * - Recursos no encontrados (404 Not Found)
 * 
 * Nota: Spring Boot con Jakarta Validation retorna 422 para errores de validación,
 * y Spring Security retorna 403 cuando no hay token de autenticación.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Reporte - Validaciones y Escenarios de Fallo")
class ReporteValidacionIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private BarrioRepository barrioRepository;

    private Long barrioId;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Reutilizar barrio existente (DataLoader)
        Barrio barrio = barrioRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No hay barrios en BD de prueba"));
        barrioId = barrio.getId();

        // Registrar usuario único para esta clase
        String correoUnico = "validacion-test-" + System.currentTimeMillis() + "@huellitas.co";
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO(
                "Test Validacion",
                correoUnico,
                "Password123!",
                "3001234567",
                barrioId,
                Rol.CIUDADANO_VOLUNTARIO
        );

        mockMvc.perform(post("/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isCreated());

        // Obtener token JWT
        LoginRequestDTO loginDTO = new LoginRequestDTO(correoUnico, "Password123!");
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        token = node.get("token").asText();
    }

    @Test
    @DisplayName("[RF-05] Debe rechazar creación sin autenticación (403)")
    void debeRechazarCreacionSinAutenticacion() throws Exception {
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Firulais",
                Especie.PERRO,
                "Criollo",
                "Marrón",
                Sexo.MACHO,
                "2 años",
                null
        );

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.EXTRAVIADO,
                Urgencia.ALTA,
                "Perro perdido en el parque",
                "Parque Principal",
                barrioId,
                animalDTO
        );

        // Intentar crear caso SIN header Authorization
        mockMvc.perform(post("/v1/casos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()); // Spring Security retorna 403 sin token
    }

    @Test
    @DisplayName("[RNF-07] Debe rechazar DTO sin descripción (422)")
    void debeRechazarDTOSinDescripcion() throws Exception {
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Max",
                Especie.PERRO,
                "Labrador",
                "Dorado",
                Sexo.MACHO,
                "Cachorro",
                null
        );

        // DTO con descripción null (viola @NotBlank)
        String jsonInvalido = String.format("""
                {
                    "tipoCaso": "VULNERABLE",
                    "urgencia": "ALTA",
                    "descripcion": null,
                    "direccionReferencia": "Calle 15",
                    "barrioId": %d,
                    "animal": {
                        "nombre": "Max",
                        "especie": "PERRO",
                        "raza": "Labrador",
                        "color": "Dorado",
                        "sexo": "MACHO",
                        "edadAproximada": "Cachorro"
                    }
                }
                """, barrioId);

        mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isUnprocessableEntity()); // 422 para validaciones Jakarta
    }

    @Test
    @DisplayName("[RNF-07] Debe rechazar descripción mayor a 2000 caracteres (422)")
    void debeRechazarDescripcionMuyLarga() throws Exception {
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Luna",
                Especie.GATO,
                "Siamés",
                "Blanco",
                Sexo.HEMBRA,
                "Adulto",
                null
        );

        // Generar descripción de 2001 caracteres (viola @Size(max = 2000))
        String descripcionLarga = "A".repeat(2001);

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.VULNERABLE,
                Urgencia.MEDIA,
                descripcionLarga,
                "Calle 10",
                barrioId,
                animalDTO
        );

        mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity()); // 422 para validaciones Jakarta
    }

    @Test
    @DisplayName("[RNF-07] Debe rechazar DTO sin barrioId (422)")
    void debeRechazarDTOSinBarrioId() throws Exception {
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Toby",
                Especie.PERRO,
                "Beagle",
                "Tricolor",
                Sexo.MACHO,
                "Cachorro",
                "Orejas caídas"
        );

        // DTO sin barrioId (viola @NotNull)
        String jsonInvalido = """
                {
                    "tipoCaso": "VULNERABLE",
                    "urgencia": "ALTA",
                    "descripcion": "Cachorro abandonado",
                    "direccionReferencia": "Parque Los Fundadores",
                    "barrioId": null,
                    "animal": {
                        "nombre": "Toby",
                        "especie": "PERRO",
                        "raza": "Beagle",
                        "color": "Tricolor",
                        "sexo": "MACHO",
                        "edadAproximada": "Cachorro",
                        "senasParticulares": "Orejas caídas"
                    }
                }
                """;

        mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isUnprocessableEntity()); // 422 para validaciones Jakarta
    }

    @Test
    @DisplayName("[RNF-07] Debe rechazar DTO sin datos del animal (422)")
    void debeRechazarDTOSinAnimal() throws Exception {
        // DTO sin objeto animal (viola @NotNull @Valid)
        String jsonInvalido = String.format("""
                {
                    "tipoCaso": "EXTRAVIADO",
                    "urgencia": "BAJA",
                    "descripcion": "Mascota perdida",
                    "direccionReferencia": "Barrio Centro",
                    "barrioId": %d,
                    "animal": null
                }
                """, barrioId);

        mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isUnprocessableEntity()); // 422 para validaciones Jakarta
    }

    @Test
    @DisplayName("[RNF-07] Debe rechazar animal sin especie (422)")
    void debeRechazarAnimalSinEspecie() throws Exception {
        // Animal sin especie (viola @NotNull en AnimalReporteDTO)
        String jsonInvalido = String.format("""
                {
                    "tipoCaso": "VULNERABLE",
                    "urgencia": "MEDIA",
                    "descripcion": "Animal herido",
                    "direccionReferencia": "Calle 20",
                    "barrioId": %d,
                    "animal": {
                        "nombre": "Sin nombre",
                        "especie": null,
                        "raza": "Desconocida",
                        "color": "Marrón"
                    }
                }
                """, barrioId);

        mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isUnprocessableEntity()); // 422 para validaciones Jakarta
    }

    @Test
    @DisplayName("[RF-05] Debe rechazar barrioId inexistente (404)")
    void debeRechazarBarrioIdInexistente() throws Exception {
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Pelusa",
                Especie.GATO,
                "Criollo",
                "Gris",
                Sexo.HEMBRA,
                "3 años",
                null
        );

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.VULNERABLE,
                Urgencia.BAJA,
                "Gata herida",
                "Calle 5",
                999999L, // barrioId inexistente
                animalDTO
        );

        mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
