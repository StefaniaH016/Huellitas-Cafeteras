package co.edu.uniquindio.huellitas;

import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.dto.usuario.ActualizarPerfilDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class Fase1IntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private BarrioRepository barrioRepository;

    private Long barrioPruebaId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        Barrio barrio = barrioRepository.findAll().stream().findFirst().orElseThrow();
        barrioPruebaId = barrio.getId();
    }

    private String obtenerTokenAuth(String correo, String contrasena) throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO(correo, contrasena);
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    @DisplayName("1. Auth - Registro exitoso, rechazo por correo duplicado y login con JWT")
    void testAuthFlujoCompleto() throws Exception {
        String correo = "voluntario_test@huellitas.co";
        RegistroUsuarioDTO registro = new RegistroUsuarioDTO(
                "Carlos Voluntario",
                correo,
                "Password123!",
                "3115551234",
                barrioPruebaId,
                Rol.CIUDADANO_VOLUNTARIO
        );

        // Registro exitoso
        mockMvc.perform(post("/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Carlos Voluntario"))
                .andExpect(jsonPath("$.correo").value(correo))
                .andExpect(jsonPath("$.rol").value("CIUDADANO_VOLUNTARIO"));

        // Registro duplicado debe fallar con 409 Conflict
        mockMvc.perform(post("/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isConflict());

        // Login exitoso
        String token = obtenerTokenAuth(correo, "Password123!");
        assertThat(token).isNotBlank();

        // Consulta /v1/auth/me con token
        mockMvc.perform(get("/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value(correo))
                .andExpect(jsonPath("$.nombre").value("Carlos Voluntario"));
    }

    @Test
    @DisplayName("2. Usuarios - Perfil consulta y actualización")
    void testUsuarioPerfil() throws Exception {
        String token = obtenerTokenAuth("admin@huellitas.co", "Admin12345!");

        // GET /v1/usuarios/me
        mockMvc.perform(get("/v1/usuarios/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("admin@huellitas.co"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));

        // PUT /v1/usuarios/me
        ActualizarPerfilDTO actualizarDTO = new ActualizarPerfilDTO(
                "Super Admin Huellitas",
                "3129998877",
                barrioPruebaId
        );

        mockMvc.perform(put("/v1/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Super Admin Huellitas"))
                .andExpect(jsonPath("$.telefono").value("3129998877"))
                .andExpect(jsonPath("$.barrioId").value(barrioPruebaId));
    }

    @Test
    @DisplayName("3. Ubicaciones - Consulta de departamentos, municipios y barrios con DTOs")
    void testUbicacionesEndpoints() throws Exception {
        // Departamentos
        MvcResult depResult = mockMvc.perform(get("/v1/ubicaciones/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").isNotEmpty())
                .andReturn();

        JsonNode depJson = objectMapper.readTree(depResult.getResponse().getContentAsString());
        long depId = depJson.get(0).get("id").asLong();

        // Municipios por query param
        MvcResult munResult = mockMvc.perform(get("/v1/ubicaciones/municipios?departamentoId=" + depId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].departamentoId").value(depId))
                .andReturn();

        JsonNode munJson = objectMapper.readTree(munResult.getResponse().getContentAsString());
        long munId = munJson.get(0).get("id").asLong();

        // Municipios por path var (retrocompatibilidad)
        mockMvc.perform(get("/v1/ubicaciones/departamentos/" + depId + "/municipios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Barrios por query param
        mockMvc.perform(get("/v1/ubicaciones/barrios?municipioId=" + munId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].municipioId").value(munId));

        // Barrios por path var (retrocompatibilidad)
        mockMvc.perform(get("/v1/ubicaciones/municipios/" + munId + "/barrios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("4. Casos - Flujo completo CRUD, feed, historial, estado, cierre y archivado")
    void testCasosFlujoCompleto() throws Exception {
        String token = obtenerTokenAuth("admin@huellitas.co", "Admin12345!");

        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Firulais",
                Especie.PERRO,
                "Criollo",
                "Marrón",
                Sexo.MACHO,
                "2 años",
                "Mancha blanca en el pecho"
        );

        CrearReporteDTO casoDTO = new CrearReporteDTO(
                TipoCaso.HERIDO,
                Urgencia.ALTA,
                "Perrito con pata lastimada cerca al parque",
                "Frente a la panadería central",
                barrioPruebaId,
                animalDTO
        );

        // 1. Crear Caso (POST /v1/casos)
        MvcResult createResult = mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(casoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.tipoCaso").value("HERIDO"))
                .andExpect(jsonPath("$.urgencia").value("ALTA"))
                .andExpect(jsonPath("$.estado").value("REGISTRADO"))
                .andExpect(jsonPath("$.barrioId").value(barrioPruebaId))
                .andExpect(jsonPath("$.fechaCreacion").isNotEmpty())
                .andReturn();

        JsonNode creadoJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long casoId = creadoJson.get("id").asLong();

        // 2. Feed de Casos (GET /v1/casos con filtros)
        mockMvc.perform(get("/v1/casos?especie=PERRO&urgencia=ALTA&estado=REGISTRADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].especie").value("PERRO"));

        // 3. Detalle de Caso (GET /v1/casos/{id})
        mockMvc.perform(get("/v1/casos/" + casoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(casoId))
                .andExpect(jsonPath("$.animal.nombre").value("Firulais"))
                .andExpect(jsonPath("$.ubicacion.barrioId").value(barrioPruebaId))
                .andExpect(jsonPath("$.historial").isArray());

        // 4. Historial del caso (GET /v1/casos/{id}/historial)
        mockMvc.perform(get("/v1/casos/" + casoId + "/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].estadoNuevo").value("REGISTRADO"));

        // 5. Mis reportes (GET /v1/casos/mis-reportes)
        mockMvc.perform(get("/v1/casos/mis-reportes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(casoId));

        // 6. Actualizar estado (PATCH /v1/casos/{id}/estado)
        ActualizarEstadoDTO cambioEstadoDTO = new ActualizarEstadoDTO(
                EstadoCaso.EN_ATENCION,
                "Animal remitido a veterinaria de turno"
        );

        mockMvc.perform(patch("/v1/casos/" + casoId + "/estado")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambioEstadoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_ATENCION"));

        // 7. Cerrar caso (POST /v1/casos/{id}/cierre)
        CierreCasoRequestDTO cierreDTO = new CierreCasoRequestDTO(
                ResultadoCierre.RECUPERADO,
                "Perrito dado de alta y adoptado por vecino",
                "Juan Pérez"
        );

        mockMvc.perform(post("/v1/casos/" + casoId + "/cierre")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cierreDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));

        // 8. Archivar caso (PATCH /v1/casos/{id}/archivar)
        ArchivarCasoDTO archivarDTO = new ArchivarCasoDTO("Caso concluido satisfactoriamente");

        mockMvc.perform(patch("/v1/casos/" + casoId + "/archivar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(archivarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ARCHIVADO"));
    }
}
