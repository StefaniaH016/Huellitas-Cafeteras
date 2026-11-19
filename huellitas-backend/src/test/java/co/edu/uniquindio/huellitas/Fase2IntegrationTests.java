package co.edu.uniquindio.huellitas;

import co.edu.uniquindio.huellitas.config.TestCloudinaryConfig;
import co.edu.uniquindio.huellitas.dto.aporte.CrearAporteDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.AbrirBitacoraDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.AltaMedicaDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.EvolucionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.reporte.AnimalReporteDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestCloudinaryConfig.class)
class Fase2IntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BarrioRepository barrioRepository;

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private MunicipioRepository municipioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Long barrioId;
    private Long casoCreadoId;
    private String municipioNombre;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        Barrio barrio = barrioRepository.findAll().stream().findFirst().orElseThrow();
        barrioId = barrio.getId();
        municipioNombre = municipioRepository.findById(barrio.getMunicipio().getId())
                .orElseThrow()
                .getNombre();

        // Registrar usuario voluntario para reportar caso base si no existe
        String correoVoluntario = "voluntario_fase2@huellitas.co";
        RegistroUsuarioDTO regDTO = new RegistroUsuarioDTO(
                "Voluntario Fase 2",
                correoVoluntario,
                "Password123!",
                "3115559988",
                barrioId,
                Rol.CIUDADANO_VOLUNTARIO
        );
        mockMvc.perform(post("/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regDTO)));

        String token = loginYObtenerToken(correoVoluntario, "Password123!");

        // Crear caso base
        AnimalReporteDTO animalDTO = new AnimalReporteDTO("Pelusa", Especie.GATO, "Criollo", "Blanco", null, "1 año", null);
        CrearReporteDTO casoDTO = new CrearReporteDTO(TipoCaso.VULNERABLE, Urgencia.ALTA, "Gato herido para pruebas de Fase 2", "Calle 10", barrioId, animalDTO);

        MvcResult casoRes = mockMvc.perform(post("/v1/casos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(casoDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode casoNode = objectMapper.readTree(casoRes.getResponse().getContentAsString());
        casoCreadoId = casoNode.get("id").asLong();
    }

    private String loginYObtenerToken(String correo, String contrasena) throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO(correo, contrasena);
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    @DisplayName("Flujo Módulo 3: Verificación de entidades institucionales por Administrador")
    void testVerificaciones() throws Exception {
        // 1. Registrar fundación pendiente de verificación
        String correoFundacion = "fundacion_fase2@huellitas.co";
        RegistroUsuarioDTO registroFundacion = new RegistroUsuarioDTO(
                "Contacto Patitas",
                correoFundacion,
                "FundPassword123!",
                "3101234567",
                barrioId,
                Rol.FUNDACION_REFUGIO,
                "Fundación Patitas Fase 2",
                "900111",
                null,
                municipioNombre
        );
        mockMvc.perform(post("/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroFundacion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificado").value(false));

        // 2. Login como admin
        String adminToken = loginYObtenerToken("admin@huellitas.co", "Admin12345!");

        // 3. Listar verificaciones pendientes
        MvcResult pendientesRes = mockMvc.perform(get("/v1/verificaciones/pendientes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode listNode = objectMapper.readTree(pendientesRes.getResponse().getContentAsString());
        assertThat(listNode.isArray()).isTrue();

        Long vetId = null;
        for (JsonNode n : listNode) {
            if ("Fundación Patitas Fase 2".equals(n.get("nombreEntidad").asText())) {
                vetId = n.get("usuarioId").asLong();
                break;
            }
        }
        assertThat(vetId).isNotNull();

        // 4. Aprobar verificación
        DecisionVerificacionDTO decisionDTO = new DecisionVerificacionDTO("APROBAR", "Documentos en regla y habilitación confirmada");
        mockMvc.perform(patch("/v1/verificaciones/" + vetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VERIFICADA"))
                .andExpect(jsonPath("$.verificado").value(true));

        String fundacionToken = loginYObtenerToken(correoFundacion, "FundPassword123!");
        mockMvc.perform(get("/v1/usuarios/me")
                        .header("Authorization", "Bearer " + fundacionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoVerificacionOrganizacion").value("VERIFICADA"))
                .andExpect(jsonPath("$.aceptaCustodia").value(true))
                .andExpect(jsonPath("$.capacidadAlbergue").value(10));

        mockMvc.perform(patch("/v1/casos/" + casoCreadoId + "/custodia")
                        .header("Authorization", "Bearer " + fundacionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"ASUMIR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_ATENCION"));
    }

    @Test
    @DisplayName("Una entidad puede adjuntar soporte y solicitar una nueva revisión")
    void testSolicitudNuevaRevisionConDocumento() throws Exception {
        String correo = "fundacion_revision_fase2@huellitas.co";
        RegistroUsuarioDTO registro = new RegistroUsuarioDTO(
                "Contacto Revisión",
                correo,
                "FundPassword123!",
                "3101234567",
                barrioId,
                Rol.FUNDACION_REFUGIO,
                "Fundación Revisión Fase 2",
                "900222",
                null,
                municipioNombre);
        MockMultipartFile soporteInicial = new MockMultipartFile(
                "documentoSoporte",
                "soporte-inicial.pdf",
                "application/pdf",
                "%PDF-1.7 soporte inicial".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockPart datosRegistro = new MockPart(
                "datos",
                objectMapper.writeValueAsBytes(registro));
        datosRegistro.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MvcResult registroRes = mockMvc.perform(multipart("/v1/auth/registro/institucional")
                        .file(soporteInicial)
                        .part(datosRegistro))
                .andExpect(status().isCreated())
                .andReturn();
        long usuarioId = objectMapper.readTree(registroRes.getResponse().getContentAsString())
                .get("id").asLong();

        String adminToken = loginYObtenerToken("admin@huellitas.co", "Admin12345!");
        DecisionVerificacionDTO rechazo = new DecisionVerificacionDTO("RECHAZAR", "Actualizar documento");
        mockMvc.perform(patch("/v1/verificaciones/" + usuarioId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rechazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        String tokenFundacion = loginYObtenerToken(correo, "FundPassword123!");
        MockMultipartFile soporteActualizado = new MockMultipartFile(
                "documentoSoporte",
                "soporte-actualizado.pdf",
                "application/pdf",
                "%PDF-1.7 soporte actualizado".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/v1/verificaciones/solicitar-revision")
                        .file(soporteActualizado)
                        .header("Authorization", "Bearer " + tokenFundacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.verificado").value(false));

        MvcResult pendientesRes = mockMvc.perform(get("/v1/verificaciones/pendientes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode pendientes = objectMapper.readTree(pendientesRes.getResponse().getContentAsString());
        JsonNode solicitud = java.util.stream.StreamSupport.stream(pendientes.spliterator(), false)
                .filter(item -> item.get("usuarioId").asLong() == usuarioId)
                .findFirst()
                .orElseThrow();
        assertThat(solicitud.get("documentoSoporteDisponible").asBoolean()).isTrue();
        assertThat(solicitud.get("documentoSoporteNombre").asText()).isEqualTo("soporte-actualizado.pdf");

        mockMvc.perform(get("/v1/verificaciones/" + usuarioId + "/documento-soporte")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(
                        "%PDF-1.7 soporte actualizado".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Flujo Módulo 8: Creación y consulta de aportes comunitarios a un caso")
    void testAportesComunitarios() throws Exception {
        String token = loginYObtenerToken("voluntario_fase2@huellitas.co", "Password123!");

        CrearAporteDTO aporteDTO = new CrearAporteDTO(
                "Lo vi comiendo cerca del parque principal",
                "Parque central de Armenia",
                barrioId,
                Collections.singletonList("https://res.cloudinary.com/huellitas-test/image/upload/v1/avistamiento1.jpg")
        );

        mockMvc.perform(post("/v1/casos/" + casoCreadoId + "/aportes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aporteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Lo vi comiendo cerca del parque principal"))
                .andExpect(jsonPath("$.casoId").value(casoCreadoId));

        mockMvc.perform(get("/v1/casos/" + casoCreadoId + "/aportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].descripcion").exists());
    }

    @Test
    @DisplayName("Flujo Módulos 9 y 10: Bitácora médica, evoluciones, alta y generación de PDF")
    void testBitacoraMedicaYPdf() throws Exception {
        String adminToken = loginYObtenerToken("admin@huellitas.co", "Admin12345!");
        String correoVeterinaria = "veterinaria_bitacora@huellitas.co";
        RegistroUsuarioDTO registroVeterinaria = new RegistroUsuarioDTO(
                "Veterinaria Bitacora",
                correoVeterinaria,
                "VetPassword123!",
                "3101234567",
                barrioId,
                Rol.VETERINARIA,
                "Veterinaria Bitacora",
                "900112",
                "TP124",
                municipioNombre
        );
        mockMvc.perform(post("/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroVeterinaria)))
                .andExpect(status().isCreated());

        MvcResult pendientesResult = mockMvc.perform(get("/v1/verificaciones/pendientes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode pendientes = objectMapper.readTree(pendientesResult.getResponse().getContentAsString());
        Long veterinariaId = null;
        for (JsonNode pendiente : pendientes) {
            if ("Veterinaria Bitacora".equals(pendiente.get("nombreEntidad").asText())) {
                veterinariaId = pendiente.get("usuarioId").asLong();
                break;
            }
        }
        assertThat(veterinariaId).isNotNull();

        DecisionVerificacionDTO decision = new DecisionVerificacionDTO(
                "APROBAR", "Credenciales profesionales verificadas");
        mockMvc.perform(patch("/v1/verificaciones/" + veterinariaId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decision)))
                .andExpect(status().isOk());

        String veterinariaToken = loginYObtenerToken(correoVeterinaria, "VetPassword123!");

        // HU 14.1: Caso debe estar en estado EN_ATENCION para permitir apertura de bitácora
        Caso casoActual = casoRepository.findById(casoCreadoId).orElseThrow();
        casoActual.setEstado(EstadoCaso.EN_ATENCION);
        casoRepository.save(casoActual);

        // 1. Abrir bitácora
        AbrirBitacoraDTO abrirDTO = new AbrirBitacoraDTO("Ingreso por posible deshidratación y laceraciones");
        String ciudadanoToken = loginYObtenerToken(
                "voluntario_fase2@huellitas.co", "Password123!");
        mockMvc.perform(post("/v1/casos/" + casoCreadoId + "/bitacora")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abrirDTO)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/v1/casos/" + casoCreadoId + "/bitacora")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abrirDTO)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/v1/casos/" + casoCreadoId + "/bitacora")
                        .header("Authorization", "Bearer " + veterinariaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abrirDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));

        // 2. Agregar evolución
        EvolucionDTO evoDTO = new EvolucionDTO(
                "Heridas superficiales tratadas con antiséptico",
                "Limpieza y curación",
                "Antibiótico tópico por 3 días",
                "Amoxicilina 250mg",
                "Paciente estable y comiendo adecuadamente"
        );
        mockMvc.perform(post("/v1/casos/" + casoCreadoId + "/bitacora/evoluciones")
                        .header("Authorization", "Bearer " + veterinariaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        // 3. Consultar bitácora (14.4: Acceso restringido por confidencialidad clínica)
        mockMvc.perform(get("/v1/casos/" + casoCreadoId + "/bitacora")
                        .header("Authorization", "Bearer " + veterinariaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evoluciones").isArray());

        // 3b. HU 14.5: Consultar mis-pacientes para panel de veterinaria
        mockMvc.perform(get("/v1/bitacoras/mis-pacientes")
                        .header("Authorization", "Bearer " + veterinariaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].casoId").value(casoCreadoId));

        // 4. Registrar alta médica
        AltaMedicaDTO altaDTO = new AltaMedicaDTO("Totalmente recuperado", "Mantener en reposo por 2 días");
        mockMvc.perform(patch("/v1/casos/" + casoCreadoId + "/bitacora/alta")
                        .header("Authorization", "Bearer " + veterinariaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(altaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoBitacora").value("ALTA_REGISTRADA"));

        // 5. Descargar PDF clínico (autenticado)
        MvcResult pdfResult = mockMvc.perform(get("/v1/casos/" + casoCreadoId + "/bitacora/pdf")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        byte[] pdfBytes = pdfResult.getResponse().getContentAsByteArray();
        assertThat(pdfBytes).isNotEmpty();
        // Verificar firma mágica de archivo PDF (%PDF)
        String pdfHeader = new String(pdfBytes, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Flujo Módulo 13: Moderación administrativa de casos")
    void testModeracionCasos() throws Exception {
        String adminToken = loginYObtenerToken("admin@huellitas.co", "Admin12345!");

        DecisionModeracionDTO decisionDTO = new DecisionModeracionDTO("OCULTAR", "Contenido duplicado reportado por la comunidad");
        mockMvc.perform(patch("/v1/moderacion/casos/" + casoCreadoId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("OCULTADO"));

        mockMvc.perform(get("/v1/moderacion/casos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Flujo Módulo 5 (Imágenes): Subida multipart de fotografía")
    void testSubidaImagenMultipart() throws Exception {
        String token = loginYObtenerToken("voluntario_fase2@huellitas.co", "Password123!");

        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "mascota_prueba.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "contenido-de-prueba-imagen".getBytes()
        );

        mockMvc.perform(multipart("/v1/imagenes")
                        .file(file)
                        .param("casoId", casoCreadoId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.publicId").exists());
    }
}
