package co.edu.uniquindio.huellitas.config;

import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.DepartamentoRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final BarrioRepository barrioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        cargarUbicacionesSemilla();
        cargarAdministradorPorDefecto();
    }

    private void cargarUbicacionesSemilla() {
        if (departamentoRepository.count() > 0) {
            log.info("Datos de departamentos y municipios ya presentes en la base de datos.");
            return;
        }

        log.info("Inicializando datos semilla de departamentos, municipios y barrios del Eje Cafetero...");

        // 1. Quindío
        Departamento quindio = departamentoRepository.save(
                Departamento.builder().nombre("Quindío").build()
        );

        Municipio armenia = municipioRepository.save(
                Municipio.builder().nombre("Armenia").departamento(quindio).build()
        );
        Municipio calarca = municipioRepository.save(
                Municipio.builder().nombre("Calarcá").departamento(quindio).build()
        );
        municipioRepository.saveAll(List.of(
                Municipio.builder().nombre("Circasia").departamento(quindio).build(),
                Municipio.builder().nombre("Montenegro").departamento(quindio).build(),
                Municipio.builder().nombre("Quimbaya").departamento(quindio).build(),
                Municipio.builder().nombre("La Tebaida").departamento(quindio).build(),
                Municipio.builder().nombre("Salento").departamento(quindio).build(),
                Municipio.builder().nombre("Filandia").departamento(quindio).build()
        ));

        barrioRepository.saveAll(List.of(
                Barrio.builder().nombre("Centro").municipio(armenia).build(),
                Barrio.builder().nombre("Granada").municipio(armenia).build(),
                Barrio.builder().nombre("Castellana").municipio(armenia).build(),
                Barrio.builder().nombre("La Isabela").municipio(armenia).build(),
                Barrio.builder().nombre("Los Profesionales").municipio(armenia).build(),
                Barrio.builder().nombre("San José").municipio(armenia).build(),
                Barrio.builder().nombre("Puerto Espejo").municipio(armenia).build(),
                Barrio.builder().nombre("Las Américas").municipio(armenia).build(),
                Barrio.builder().nombre("Centro").municipio(calarca).build(),
                Barrio.builder().nombre("Versalles").municipio(calarca).build(),
                Barrio.builder().nombre("Simón Bolívar").municipio(calarca).build(),
                Barrio.builder().nombre("Balcones de la Villa").municipio(calarca).build()
        ));

        // 2. Risaralda
        Departamento risaralda = departamentoRepository.save(
                Departamento.builder().nombre("Risaralda").build()
        );

        Municipio pereira = municipioRepository.save(
                Municipio.builder().nombre("Pereira").departamento(risaralda).build()
        );
        Municipio dosquebradas = municipioRepository.save(
                Municipio.builder().nombre("Dosquebradas").departamento(risaralda).build()
        );
        municipioRepository.saveAll(List.of(
                Municipio.builder().nombre("Santa Rosa de Cabal").departamento(risaralda).build(),
                Municipio.builder().nombre("La Virginia").departamento(risaralda).build()
        ));

        barrioRepository.saveAll(List.of(
                Barrio.builder().nombre("Centro").municipio(pereira).build(),
                Barrio.builder().nombre("Álamos").municipio(pereira).build(),
                Barrio.builder().nombre("Pinares").municipio(pereira).build(),
                Barrio.builder().nombre("Cuba").municipio(pereira).build(),
                Barrio.builder().nombre("Circunvalar").municipio(pereira).build(),
                Barrio.builder().nombre("Providencia").municipio(pereira).build(),
                Barrio.builder().nombre("Santa Mónica").municipio(dosquebradas).build(),
                Barrio.builder().nombre("El Campestre").municipio(dosquebradas).build(),
                Barrio.builder().nombre("Frailes").municipio(dosquebradas).build()
        ));

        // 3. Caldas
        Departamento caldas = departamentoRepository.save(
                Departamento.builder().nombre("Caldas").build()
        );

        Municipio manizales = municipioRepository.save(
                Municipio.builder().nombre("Manizales").departamento(caldas).build()
        );
        municipioRepository.saveAll(List.of(
                Municipio.builder().nombre("Villamaría").departamento(caldas).build(),
                Municipio.builder().nombre("Chinchiná").departamento(caldas).build(),
                Municipio.builder().nombre("Neira").departamento(caldas).build(),
                Municipio.builder().nombre("Salamina").departamento(caldas).build()
        ));

        barrioRepository.saveAll(List.of(
                Barrio.builder().nombre("Centro").municipio(manizales).build(),
                Barrio.builder().nombre("Palermo").municipio(manizales).build(),
                Barrio.builder().nombre("Chipre").municipio(manizales).build(),
                Barrio.builder().nombre("El Cable").municipio(manizales).build(),
                Barrio.builder().nombre("Milán").municipio(manizales).build(),
                Barrio.builder().nombre("La Sultana").municipio(manizales).build()
        ));

        log.info("Datos semilla de ubicaciones cargados exitosamente.");
    }

    private void cargarAdministradorPorDefecto() {
        final String correoAdmin = "admin@huellitas.co";
        if (usuarioRepository.existsByCorreo(correoAdmin)) {
            log.info("Usuario administrador ya existe en el sistema.");
            return;
        }

        log.info("Creando usuario administrador por defecto ({})", correoAdmin);

        Usuario admin = Usuario.builder()
                .nombre("Administrador Huellitas")
                .correo(correoAdmin)
                .contrasena(passwordEncoder.encode("Admin12345!"))
                .telefono("3001234567")
                .rol(Rol.ADMINISTRADOR)
                .verificado(true)
                .activo(true)
                .disponibleRescate(false)
                .aceptoTratamientoDatos(true)
                .fechaAceptacionDatos(Instant.now())
                .build();

        usuarioRepository.save(admin);
        log.info("Usuario administrador creado exitosamente.");
    }
}
