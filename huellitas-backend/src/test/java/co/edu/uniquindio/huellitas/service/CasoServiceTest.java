package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.reporte.AnimalReporteDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CasoResponseDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.impl.CasoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CasoService - Creación de reportes.
 * HU-10: Captura y Registro de Reportes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CasoService - Pruebas Unitarias Creación de Reportes")
class CasoServiceTest {

    @Mock
    private CasoRepository casoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private BarrioRepository barrioRepository;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @Mock
    private SeguimientoRepository seguimientoRepository;

    @Mock
    private FotoRepository fotoRepository;

    @InjectMocks
    private CasoServiceImpl casoService;

    private Usuario usuarioMock;
    private Barrio barrioMock;
    private Municipio municipioMock;
    private Departamento departamentoMock;
    private CrearReporteDTO dtoValido;

    @BeforeEach
    void setUp() {
        // Usuario mock
        usuarioMock = Usuario.builder()
                .id(1L)
                .correo("usuario@test.com")
                .nombre("Usuario Test")
                .rol(Rol.CIUDADANO_VOLUNTARIO)
                .build();

        // Ubicación mock
        departamentoMock = Departamento.builder()
                .id(1L)
                .nombre("Quindío")
                .build();

        municipioMock = Municipio.builder()
                .id(1L)
                .nombre("Armenia")
                .departamento(departamentoMock)
                .build();

        barrioMock = Barrio.builder()
                .id(1L)
                .nombre("Centro")
                .municipio(municipioMock)
                .build();

        // DTO válido
        AnimalReporteDTO animalDTO = new AnimalReporteDTO(
                "Firulais",
                Especie.PERRO,
                "Mestizo",
                "Café",
                Sexo.MACHO,
                "Adulto",
                "Collar rojo, cojea pata trasera"
        );

        dtoValido = new CrearReporteDTO(
                TipoCaso.EXTRAVIADO,
                Urgencia.MEDIA,
                "Perro perdido cerca del parque",
                "Frente al parque principal",
                1L,
                animalDTO
        );
    }

    @Test
    @DisplayName("Debe crear caso exitosamente con datos válidos")
    void debeCrearCasoExitosamente() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        when(casoRepository.save(any(Caso.class))).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CasoResponseDTO resultado = casoService.crearCaso(dtoValido, "usuario@test.com");

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.id());
        assertEquals(TipoCaso.EXTRAVIADO, resultado.tipoCaso());
        assertEquals(EstadoCaso.REGISTRADO, resultado.estado());
        assertEquals(Urgencia.MEDIA, resultado.urgencia());

        // Verificar que se guardaron las entidades
        verify(animalRepository, times(1)).save(any(Animal.class));
        verify(casoRepository, times(1)).save(any(Caso.class));
        verify(historialEstadoRepository, times(1)).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("Debe crear animal con los datos correctos del DTO")
    void debeCrearAnimalConDatosCorrectos() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
        when(animalRepository.save(animalCaptor.capture())).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        when(casoRepository.save(any(Caso.class))).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        casoService.crearCaso(dtoValido, "usuario@test.com");

        // Assert
        Animal animalGuardado = animalCaptor.getValue();
        assertEquals("Firulais", animalGuardado.getNombre());
        assertEquals(Especie.PERRO, animalGuardado.getEspecie());
        assertEquals("Mestizo", animalGuardado.getRaza());
        assertEquals("Café", animalGuardado.getColor());
        assertEquals(Sexo.MACHO, animalGuardado.getSexo());
        assertEquals("Adulto", animalGuardado.getEdadAproximada());
        assertEquals("Collar rojo, cojea pata trasera", animalGuardado.getSenasParticulares());
        assertEquals(usuarioMock, animalGuardado.getPropietario());
    }

    @Test
    @DisplayName("Debe crear caso con estado inicial REGISTRADO")
    void debeCrearCasoConEstadoRegistrado() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        ArgumentCaptor<Caso> casoCaptor = ArgumentCaptor.forClass(Caso.class);
        when(casoRepository.save(casoCaptor.capture())).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        casoService.crearCaso(dtoValido, "usuario@test.com");

        // Assert
        Caso casoGuardado = casoCaptor.getValue();
        assertEquals(EstadoCaso.REGISTRADO, casoGuardado.getEstado());
        assertTrue(casoGuardado.isVisible());
        assertNotNull(casoGuardado.getFechaRegistro());
        assertNotNull(casoGuardado.getFechaUltimaActividad());
    }

    @Test
    @DisplayName("Debe crear historial de estado inicial")
    void debeCrearHistorialEstadoInicial() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        when(casoRepository.save(any(Caso.class))).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        ArgumentCaptor<HistorialEstado> historialCaptor = ArgumentCaptor.forClass(HistorialEstado.class);
        when(historialEstadoRepository.save(historialCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        casoService.crearCaso(dtoValido, "usuario@test.com");

        // Assert
        HistorialEstado historialGuardado = historialCaptor.getValue();
        assertEquals(EstadoCaso.REGISTRADO, historialGuardado.getEstadoAnterior());
        assertEquals(EstadoCaso.REGISTRADO, historialGuardado.getEstadoNuevo());
        assertEquals("Registro inicial del caso", historialGuardado.getObservacion());
        assertEquals(usuarioMock, historialGuardado.getUsuario());
        assertNotNull(historialGuardado.getFecha());
    }

    @Test
    @DisplayName("Debe asociar caso con ubicación correcta")
    void debeAsociarCasoConUbicacion() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        ArgumentCaptor<Caso> casoCaptor = ArgumentCaptor.forClass(Caso.class);
        when(casoRepository.save(casoCaptor.capture())).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        casoService.crearCaso(dtoValido, "usuario@test.com");

        // Assert
        Caso casoGuardado = casoCaptor.getValue();
        assertEquals(barrioMock, casoGuardado.getBarrio());
        assertEquals("Frente al parque principal", casoGuardado.getDireccionReferencia());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void debeLanzarExcepcionCuandoUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            casoService.crearCaso(dtoValido, "noexiste@test.com");
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(casoRepository, never()).save(any(Caso.class));
        verify(animalRepository, never()).save(any(Animal.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando barrio no existe")
    void debeLanzarExcepcionCuandoBarrioNoExiste() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(999L)).thenReturn(Optional.empty());

        CrearReporteDTO dtoConBarrioInvalido = new CrearReporteDTO(
                TipoCaso.EXTRAVIADO,
                Urgencia.MEDIA,
                "Descripción",
                "Dirección",
                999L,
                dtoValido.animal()
        );

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            casoService.crearCaso(dtoConBarrioInvalido, "usuario@test.com");
        });

        assertTrue(exception.getMessage().contains("Barrio"));
        verify(casoRepository, never()).save(any(Caso.class));
        verify(animalRepository, never()).save(any(Animal.class));
    }

    @Test
    @DisplayName("Debe normalizar correo a minúsculas y sin espacios")
    void debeNormalizarCorreo() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        when(casoRepository.save(any(Caso.class))).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        casoService.crearCaso(dtoValido, "  USUARIO@TEST.COM  ");

        // Assert
        verify(usuarioRepository, times(1)).findByCorreo("usuario@test.com");
    }

    @Test
    @DisplayName("Debe crear caso con urgencia ALTA correctamente")
    void debeCrearCasoConUrgenciaAlta() {
        // Arrange
        CrearReporteDTO dtoUrgente = new CrearReporteDTO(
                TipoCaso.VULNERABLE,
                Urgencia.ALTA,
                "Animal herido en la calle",
                "Calle 15 con 20",
                1L,
                dtoValido.animal()
        );

        when(usuarioRepository.findByCorreo("usuario@test.com")).thenReturn(Optional.of(usuarioMock));
        when(barrioRepository.findById(1L)).thenReturn(Optional.of(barrioMock));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(10L);
            return animal;
        });
        when(casoRepository.save(any(Caso.class))).thenAnswer(invocation -> {
            Caso caso = invocation.getArgument(0);
            caso.setId(100L);
            return caso;
        });
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CasoResponseDTO resultado = casoService.crearCaso(dtoUrgente, "usuario@test.com");

        // Assert
        assertEquals(Urgencia.ALTA, resultado.urgencia());
        assertEquals(TipoCaso.VULNERABLE, resultado.tipoCaso());
    }
}
