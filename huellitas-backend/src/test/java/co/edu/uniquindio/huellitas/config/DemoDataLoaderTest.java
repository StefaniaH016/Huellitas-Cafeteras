package co.edu.uniquindio.huellitas.config;

import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.CierreCaso;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.HistorialEstado;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.repository.AnimalRepository;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.BitacoraMedicaRepository;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.CierreCasoRepository;
import co.edu.uniquindio.huellitas.repository.HistorialEstadoRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.RegistroMedicoRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataLoaderTest {

    @Mock
    private DataLoader dataLoader;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OrganizacionRepository organizacionRepository;
    @Mock
    private BarrioRepository barrioRepository;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private CasoRepository casoRepository;
    @Mock
    private BitacoraMedicaRepository bitacoraMedicaRepository;
    @Mock
    private RegistroMedicoRepository registroMedicoRepository;
    @Mock
    private CierreCasoRepository cierreCasoRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DemoDataLoader demoDataLoader;
    private Barrio barrio;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        demoDataLoader = new DemoDataLoader(
                dataLoader,
                usuarioRepository,
                organizacionRepository,
                barrioRepository,
                animalRepository,
                casoRepository,
                bitacoraMedicaRepository,
                registroMedicoRepository,
                cierreCasoRepository,
                historialEstadoRepository,
                passwordEncoder);

        Departamento quindio = Departamento.builder().id(1L).nombre("Quindío").build();
        Municipio armenia = Municipio.builder().id(1L).nombre("Armenia").departamento(quindio).build();
        barrio = Barrio.builder().id(1L).nombre("Centro").municipio(armenia).build();
        admin = Usuario.builder().id(1L).correo(DemoDataLoader.CORREO_ADMIN).rol(Rol.ADMINISTRADOR).build();
    }

    @Test
    void demoDataLoaderExcluyePerfilesDeProduccionYTest() {
        Profile profile = DemoDataLoader.class.getAnnotation(Profile.class);

        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactly("!prod & !docker & !test");
    }

    @Test
    void creaUsuariosPorRolCasosEnCadaEstadoYBitacoraConAlta() {
        when(barrioRepository.findAll()).thenReturn(List.of(barrio));
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_ADMIN)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_CIUDADANO)).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_FUNDACION)).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_VETERINARIA)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(organizacionRepository.existsByNit(any())).thenReturn(false);
        when(organizacionRepository.save(any(Organizacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(casoRepository.findByReportanteCorreoOrderByFechaRegistroDesc(DemoDataLoader.CORREO_CIUDADANO, Pageable.unpaged()))
                .thenReturn(Page.empty());
        when(animalRepository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bitacoraMedicaRepository.save(any(BitacoraMedica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(registroMedicoRepository.save(any(RegistroMedico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cierreCasoRepository.save(any(CierreCaso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(inv -> inv.getArgument(0));

        demoDataLoader.run(new DefaultApplicationArguments());

        ArgumentCaptor<Usuario> usuariosCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(3)).save(usuariosCaptor.capture());
        assertThat(usuariosCaptor.getAllValues())
                .extracting(Usuario::getRol)
                .containsExactlyInAnyOrder(Rol.CIUDADANO_VOLUNTARIO, Rol.FUNDACION_REFUGIO, Rol.VETERINARIA);

        ArgumentCaptor<Organizacion> orgCaptor = ArgumentCaptor.forClass(Organizacion.class);
        verify(organizacionRepository, times(2)).save(orgCaptor.capture());
        assertThat(orgCaptor.getAllValues())
                .allSatisfy(org -> assertThat(org.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADA));
        assertThat(orgCaptor.getAllValues()).anyMatch(org -> org instanceof Fundacion f && f.isAceptaCustodia());
        assertThat(orgCaptor.getAllValues()).anyMatch(org -> org instanceof Veterinaria);

        ArgumentCaptor<Caso> casosCaptor = ArgumentCaptor.forClass(Caso.class);
        verify(casoRepository, times(4)).save(casosCaptor.capture());
        assertThat(casosCaptor.getAllValues())
                .extracting(Caso::getEstado)
                .containsExactlyInAnyOrder(
                        EstadoCaso.REGISTRADO,
                        EstadoCaso.EN_ATENCION,
                        EstadoCaso.RESUELTO,
                        EstadoCaso.ARCHIVADO);

        ArgumentCaptor<BitacoraMedica> bitacorasCaptor = ArgumentCaptor.forClass(BitacoraMedica.class);
        verify(bitacoraMedicaRepository, times(2)).save(bitacorasCaptor.capture());
        assertThat(bitacorasCaptor.getAllValues()).anyMatch(b -> !b.isAltaMedica());
        assertThat(bitacorasCaptor.getAllValues()).anyMatch(b -> b.isAltaMedica() && b.getFechaAlta() != null);
        verify(registroMedicoRepository, times(3)).save(any(RegistroMedico.class));
        verify(cierreCasoRepository, times(2)).save(any(CierreCaso.class));
    }

    @Test
    void noDuplicaDatosDemoSiUsuariosYCasosYaExisten() {
        Usuario ciudadano = Usuario.builder().id(2L).correo(DemoDataLoader.CORREO_CIUDADANO).rol(Rol.CIUDADANO_VOLUNTARIO).build();
        Usuario fundacion = Usuario.builder().id(3L).correo(DemoDataLoader.CORREO_FUNDACION).rol(Rol.FUNDACION_REFUGIO).build();
        Usuario veterinaria = Usuario.builder().id(4L).correo(DemoDataLoader.CORREO_VETERINARIA).rol(Rol.VETERINARIA).build();
        when(barrioRepository.findAll()).thenReturn(List.of(barrio));
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_ADMIN)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_CIUDADANO)).thenReturn(Optional.of(ciudadano));
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_FUNDACION)).thenReturn(Optional.of(fundacion));
        when(usuarioRepository.findByCorreo(DemoDataLoader.CORREO_VETERINARIA)).thenReturn(Optional.of(veterinaria));
        when(casoRepository.findByReportanteCorreoOrderByFechaRegistroDesc(DemoDataLoader.CORREO_CIUDADANO, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(Caso.builder().id(1L).build())));

        demoDataLoader.run(new DefaultApplicationArguments());

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(organizacionRepository, animalRepository, bitacoraMedicaRepository,
                registroMedicoRepository, cierreCasoRepository);
    }

    @Test
    void omiteCargaCuandoNoExistenBarrios() {
        when(barrioRepository.findAll()).thenReturn(List.of());

        demoDataLoader.run(new DefaultApplicationArguments());

        verifyNoInteractions(usuarioRepository, casoRepository, bitacoraMedicaRepository);
    }
}
