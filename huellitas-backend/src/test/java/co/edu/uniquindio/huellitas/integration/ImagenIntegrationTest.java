package co.edu.uniquindio.huellitas.integration;

import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.reporte.AnimalReporteDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CasoResponseDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.FotoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para Worker W-06 y reglas de negocio de fotografías (HU-10).
 * 
 * Cubre escenarios específicos NO cubiertos por Fase1IntegrationTests ni Fase2IntegrationTests:
 * - Regla de negocio: primera fotografía = principal
 * - Regla de negocio: múltiples fotos, solo una principal
 * - Consulta detalle con fotografías asociadas
 * - Validaciones específicas del Worker W-06
 * 
 * Las pruebas genéricas de creación de casos e imágenes están en Fase1/Fase2.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Imagen - Worker W-06 y Reglas de Fotografías")
class ImagenIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private BarrioRepository barrioRepository;

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private FotoRepository fotoRepository;

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
        String correoUnico = "imagen-test-" + System.currentTimeMillis() + "@huellitas.co";
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO(
                "Test Imagen Worker",
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
    @DisplayName("[RF-10] Primera imagen subida debe marcarse como principal")
    void primeraImagenDebeSerPrincipal() throws Exception {
        // Crear caso
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Luna",
                Especie.GATO,
                "Siamés",
                "Blanco",
                Sexo.HEMBRA,
                "Adulto",
                null
        );

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.EXTRAVIADO,
                Urgencia.BAJA,
                "Gata perdida",
                "Barrio Centro",
                barrioId,
                animalDTO
        );

        MvcResult result = mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        CasoResponseDTO caso = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CasoResponseDTO.class
        );

        // Subir primera imagen
        MockMultipartFile archivo1 = new MockMultipartFile(
                "archivo",
                "foto1.jpg",
                "image/jpeg",
                "contenido1".getBytes()
        );

        mockMvc.perform(multipart("/v1/imagenes")
                        .file(archivo1)
                        .header("Authorization", "Bearer " + token)
                        .param("casoId", String.valueOf(caso.id())))
                .andExpect(status().isCreated());

        // Verificar en BD que la foto es principal (usando repositorio para evitar LazyInitializationException)
        List<Foto> fotos = fotoRepository.findByCasoId(caso.id());
        assertEquals(1, fotos.size());
        assertTrue(fotos.get(0).isPrincipal(),
                "La primera fotografía debe marcarse como principal");
    }

    @Test
    @DisplayName("[RF-10] Múltiples imágenes: solo la primera debe ser principal")
    void multipleImagenesSoloPrimeraEsPrincipal() throws Exception {
        // Crear caso
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Rocky",
                Especie.PERRO,
                "Bulldog",
                "Blanco",
                Sexo.MACHO,
                "Adulto",
                "Cicatriz en oreja"
        );

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.VULNERABLE,
                Urgencia.MEDIA,
                "Perro herido",
                "Avenida Bolivar",
                barrioId,
                animalDTO
        );

        MvcResult result = mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        CasoResponseDTO caso = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CasoResponseDTO.class
        );

        // Subir 3 imágenes
        for (int i = 1; i <= 3; i++) {
            MockMultipartFile archivo = new MockMultipartFile(
                    "archivo",
                    "foto" + i + ".jpg",
                    "image/jpeg",
                    ("contenido" + i).getBytes()
            );

            mockMvc.perform(multipart("/v1/imagenes")
                            .file(archivo)
                            .header("Authorization", "Bearer " + token)
                            .param("casoId", String.valueOf(caso.id())))
                    .andExpect(status().isCreated());
        }

        // Verificar en BD: solo 1 foto principal (usando repositorio para evitar LazyInitializationException)
        List<Foto> fotos = fotoRepository.findByCasoId(caso.id());
        assertEquals(3, fotos.size());

        long fotosPrincipales = fotos.stream()
                .filter(Foto::isPrincipal)
                .count();

        assertEquals(1, fotosPrincipales,
                "Solo debe haber una foto principal, independientemente del número total de fotos");
    }

    @Test
    @DisplayName("[RF-10] Consulta detalle incluye fotografías con flag principal")
    void consultaDetalleIncluyeFotografias() throws Exception {
        // Crear caso
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Toby",
                Especie.PERRO,
                "Beagle",
                "Tricolor",
                Sexo.MACHO,
                "Cachorro",
                "Orejas caídas"
        );

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.VULNERABLE,
                Urgencia.ALTA,
                "Cachorro abandonado en parque",
                "Parque Los Fundadores",
                barrioId,
                animalDTO
        );

        MvcResult result = mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        CasoResponseDTO caso = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CasoResponseDTO.class
        );

        // Subir imagen
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "toby.jpg",
                "image/jpeg",
                "foto-toby".getBytes()
        );

        mockMvc.perform(multipart("/v1/imagenes")
                        .file(archivo)
                        .header("Authorization", "Bearer " + token)
                        .param("casoId", String.valueOf(caso.id())))
                .andExpect(status().isCreated());

        // Consultar detalle: debe incluir array fotografías
        mockMvc.perform(get("/v1/casos/{id}", caso.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caso.id()))
                .andExpect(jsonPath("$.animal.nombre").value("Toby"))
                .andExpect(jsonPath("$.animal.especie").value("PERRO"))
                .andExpect(jsonPath("$.fotografias").isArray())
                .andExpect(jsonPath("$.fotografias", hasSize(1)))
                .andExpect(jsonPath("$.fotografias[0].url").exists())
                .andExpect(jsonPath("$.fotografias[0].principal").value(true))
                .andExpect(jsonPath("$.historial").isArray());
    }

    @Test
    @DisplayName("[W-06] Flujo completo: crear caso → subir imagen → verificar asociación en detalle")
    void flujoCompletoWorkerW06() throws Exception {
        // Paso 1: Crear caso
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Max",
                Especie.PERRO,
                "Labrador",
                "Dorado",
                Sexo.MACHO,
                "Cachorro",
                "Muy juguetón"
        );

        CrearReporteDTO dto = new CrearReporteDTO(
                TipoCaso.VULNERABLE,
                Urgencia.ALTA,
                "Cachorro abandonado",
                "Calle 15 con 20",
                barrioId,
                animalDTO
        );

        MvcResult resultCreacion = mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        CasoResponseDTO casoCreado = objectMapper.readValue(
                resultCreacion.getResponse().getContentAsString(),
                CasoResponseDTO.class
        );
        Long casoId = casoCreado.id();

        // Paso 2: Subir imagen mediante Worker W-06
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "foto-perro.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/v1/imagenes")
                        .file(archivo)
                        .header("Authorization", "Bearer " + token)
                        .param("casoId", String.valueOf(casoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.publicId").exists());

        // Paso 3: Verificar asociación en consulta de detalle
        mockMvc.perform(get("/v1/casos/{id}", casoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotografias").isArray())
                .andExpect(jsonPath("$.fotografias", hasSize(1)))
                .andExpect(jsonPath("$.fotografias[0].url").exists())
                .andExpect(jsonPath("$.fotografias[0].principal").value(true));
    }

    @Test
    @DisplayName("[W-06] Debe subir imagen sin asociar a caso (casoId omitido)")
    void debeSubirImagenSinAsociarCaso() throws Exception {
        // Contar fotos ANTES de subir
        long fotosAntes = fotoRepository.count();

        // Subir imagen sin parámetro casoId
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "foto-standalone.jpg",
                "image/jpeg",
                "imagen-sin-caso".getBytes()
        );

        mockMvc.perform(multipart("/v1/imagenes")
                        .file(archivo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.publicId").exists());

        // Verificar que NO se persistió en tabla fotos (solo URL devuelta)
        long fotosDespues = fotoRepository.count();
        assertEquals(fotosAntes, fotosDespues,
                "Sin casoId, no debe persistirse en BD, solo generar URL. Fotos antes: " + fotosAntes + ", después: " + fotosDespues);
    }

    @Test
    @DisplayName("[W-06] Debe rechazar subida con casoId inexistente (404)")
    void debeRechazarImagenConCasoInexistente() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                "image/jpeg",
                "contenido".getBytes()
        );

        mockMvc.perform(multipart("/v1/imagenes")
                        .file(archivo)
                        .header("Authorization", "Bearer " + token)
                        .param("casoId", "999999"))
                .andExpect(status().isNotFound());
    }
}
