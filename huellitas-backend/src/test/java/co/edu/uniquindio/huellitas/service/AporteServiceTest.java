package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.aporte.CrearAporteDTO;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.AporteMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.impl.AporteServiceImpl;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AporteService - Subtarea 13.1
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AporteService - Pruebas Unitarias")
class AporteServiceTest {

    @Mock
    private CasoRepository casoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SeguimientoRepository seguimientoRepository;
    @Mock
    private FotoRepository fotoRepository;
    @Mock
    private BarrioRepository barrioRepository;
    @Mock
    private NotificacionWorker notificacionWorker;

    @Spy
    private AporteMapper aporteMapper = new AporteMapper();

    @InjectMocks
    private AporteServiceImpl aporteService;

    private Usuario usuarioMock;
    private Caso casoMock;
    private Barrio barrioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .correo("test@huellitas.com")
                .nombre("Test User")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();

        Departamento depto = Departamento.builder().id(1L).nombre("Quindío").build();
        Municipio municipio = Municipio.builder().id(1L).nombre("Armenia").departamento(depto).build();
        barrioMock = Barrio.builder().id(1L).nombre("Centro").municipio(municipio).build();

        Animal animal = Animal.builder().id(1L).nombre("Firulais").especie(Especie.PERRO).build();
        casoMock = Caso.builder()
                .id(1L)
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .urgencia(Urgencia.MEDIA)
                .estado(EstadoCaso.REGISTRADO)
                .descripcion("Perro perdido")
                .reportante(usuarioMock)
                .animal(animal)
                .barrio(barrioMock)
                .build();
    }

    @Test
    @DisplayName("Debe rechazar aporte cuando caso está ARCHIVADO")
    void debeRechazarAporteCasoArchivado() {
        casoMock.setEstado(EstadoCaso.ARCHIVADO);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));

        CrearAporteDTO dto = new CrearAporteDTO("Vi al perro", null, null, null);

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            aporteService.crearAporte(1L, dto, "test@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("archivado"));
    }

    @Test
    @DisplayName("Debe rechazar aporte cuando el caso ya está resuelto")
    void debeRechazarAporteCasoResuelto() {
        casoMock.setEstado(EstadoCaso.RESUELTO);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));

        assertThrows(ConflictException.class, () -> aporteService.crearAporte(
                1L, new CrearAporteDTO("Información suficiente", null, null, null), "test@huellitas.com"));

        verify(seguimientoRepository, never()).save(any(Seguimiento.class));
    }

    @Test
    @DisplayName("Debe rechazar descripción muy corta")
    void debeRechazarDescripcionMuyCorta() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("test@huellitas.com")).thenReturn(Optional.of(usuarioMock));

        CrearAporteDTO dto = new CrearAporteDTO("Corto", null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aporteService.crearAporte(1L, dto, "test@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("al menos"));
    }

    @Test
    @DisplayName("Debe rechazar descripción muy larga")
    void debeRechazarDescripcionMuyLarga() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("test@huellitas.com")).thenReturn(Optional.of(usuarioMock));

        String descripcionLarga = "A".repeat(1001);
        CrearAporteDTO dto = new CrearAporteDTO(descripcionLarga, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aporteService.crearAporte(1L, dto, "test@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("no puede exceder"));
    }

    @Test
    @DisplayName("Debe permitir aportes de ciudadanos externos al caso")
    void debePermitirAporteDeOtroCiudadano() {
        Usuario otroCiudadano = Usuario.builder()
                .id(2L)
                .correo("otro@huellitas.com")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("otro@huellitas.com")).thenReturn(Optional.of(otroCiudadano));
        when(seguimientoRepository.save(any(Seguimiento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AporteResponseDTO response = aporteService.crearAporte(
                1L, new CrearAporteDTO("Información suficiente", null, null, null), "otro@huellitas.com");

        assertNotNull(response);
        verify(seguimientoRepository).save(any(Seguimiento.class));
    }

    @Test
    @DisplayName("Debe permitir aportes de ciudadanos externos incluso cuando el caso tiene custodio")
    void debePermitirAporteExternoConCustodio() {
        Usuario otroCiudadano = Usuario.builder()
                .id(2L)
                .correo("otro@huellitas.com")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();
        casoMock.setFundacionCustodio(Fundacion.builder().id(5L).build());
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("otro@huellitas.com")).thenReturn(Optional.of(otroCiudadano));
        when(seguimientoRepository.save(any(Seguimiento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AporteResponseDTO response = aporteService.crearAporte(
                1L, new CrearAporteDTO("Información suficiente", null, null, null), "otro@huellitas.com");

        assertNotNull(response);
        verify(seguimientoRepository).save(any(Seguimiento.class));
    }

    @Test
    @DisplayName("Debe rechazar aporte del reportante después de asignar custodia")
    void debeRechazarAporteDelReportanteConCustodio() {
        casoMock.setFundacionCustodio(Fundacion.builder().id(5L).build());
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("test@huellitas.com")).thenReturn(Optional.of(usuarioMock));

        assertThrows(AccessDeniedException.class, () -> aporteService.crearAporte(
                1L, new CrearAporteDTO("Información suficiente", null, null, null), "test@huellitas.com"));

        verify(seguimientoRepository, never()).save(any(Seguimiento.class));
    }

    @Test
    @DisplayName("Debe rechazar barrio inexistente")
    void debeRechazarBarrioInexistente() {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("test@huellitas.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(999L)).thenReturn(Optional.empty());

        CrearAporteDTO dto = new CrearAporteDTO("Vi al perro en este barrio", null, 999L, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            aporteService.crearAporte(1L, dto, "test@huellitas.com");
        });

        assertTrue(exception.getMessage().contains("Barrio"));
    }

    @Test
    @DisplayName("Debe mover las fotos cargadas al seguimiento sin duplicarlas en el caso")
    void debeAdjuntarFotosAlSeguimiento() {
        String url = "https://example.test/aporte.jpg";
        Foto fotoSubida = Foto.builder()
                .id(10L)
                .url(url)
                .publicId("aporte")
                .principal(true)
                .caso(casoMock)
                .build();
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoMock));
        when(usuarioRepository.findByCorreo("test@huellitas.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(seguimientoRepository.save(any(Seguimiento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fotoRepository.findFirstByCasoIdAndUrlAndSeguimientoIsNull(1L, url))
                .thenReturn(Optional.of(fotoSubida));

        AporteResponseDTO response = aporteService.crearAporte(
                1L,
                new CrearAporteDTO(
                        "Información suficiente",
                        "Parque Sucre, junto a la fuente",
                        1L,
                        List.of(url)),
                "test@huellitas.com");

        assertEquals(List.of(url), response.fotografias());
        assertEquals("Parque Sucre, junto a la fuente", response.ubicacionTexto());
        assertEquals(
                "Centro · Armenia",
                aporteMapper.toDetalleDTO(fotoSubida.getSeguimiento()).zona());
        assertEquals(
                "Parque Sucre, junto a la fuente",
                aporteMapper.toDetalleDTO(fotoSubida.getSeguimiento()).ubicacionTexto());
        assertNotNull(fotoSubida.getSeguimiento());
        assertFalse(fotoSubida.isPrincipal());
        assertTrue(fotoSubida.getSeguimiento().getFotografias().contains(fotoSubida));
        assertEquals(
                List.of(url),
                aporteMapper.toDetalleDTO(fotoSubida.getSeguimiento()).fotografias());
        verify(fotoRepository).save(fotoSubida);
    }

    @Test
    @DisplayName("Debe listar aportes de un caso")
    void debeListarAportesDeUnCaso() {
        when(casoRepository.existsById(1L)).thenReturn(true);
        when(seguimientoRepository.findByCasoIdAndVisibleTrueOrderByFechaAsc(1L)).thenReturn(java.util.List.of());

        var result = aporteService.listarAportes(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar excepción si caso no existe al listar")
    void debeLanzarExcepcionSiCasoNoExiste() {
        when(casoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            aporteService.listarAportes(999L);
        });
    }
}