package co.edu.uniquindio.huellitas.config;

import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.CierreCaso;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.HistorialEstado;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.ResultadoCierre;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.entity.enums.Sexo;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.repository.AnimalRepository;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.BitacoraMedicaRepository;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.CierreCasoRepository;
import co.edu.uniquindio.huellitas.repository.HistorialEstadoRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.RegistroMedicoRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Carga de datos semilla de demostración para desarrollo local.
 * No se ejecuta en producción ({@code prod} / {@code docker}) ni en pruebas ({@code test}).
 */
@Slf4j
@Component
@Profile("!prod & !docker & !test")
@RequiredArgsConstructor
public class DemoDataLoader implements ApplicationRunner {

    public static final String CORREO_ADMIN = "admin@huellitas.co";
    public static final String CORREO_CIUDADANO = "ciudadano.demo@huellitas.co";
    public static final String CORREO_FUNDACION = "fundacion.demo@huellitas.co";
    public static final String CORREO_VETERINARIA = "veterinaria.demo@huellitas.co";
    private static final String CONTRASENA_DEMO = "Demo12345!";

    private final DataLoader dataLoader;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final BarrioRepository barrioRepository;
    private final AnimalRepository animalRepository;
    private final CasoRepository casoRepository;
    private final BitacoraMedicaRepository bitacoraMedicaRepository;
    private final RegistroMedicoRepository registroMedicoRepository;
    private final CierreCasoRepository cierreCasoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        dataLoader.run(args);

        Barrio barrioBase = barrioRepository.findAll().stream().findFirst().orElse(null);
        if (barrioBase == null) {
            log.warn("No se encontraron barrios semilla; se omite la carga de datos demo.");
            return;
        }

        Usuario admin = usuarioRepository.findByCorreo(CORREO_ADMIN).orElse(null);
        Usuario ciudadano = asegurarCiudadano(barrioBase);
        Usuario usuarioFundacion = asegurarFundacion(barrioBase, admin);
        Usuario usuarioVeterinaria = asegurarVeterinaria(barrioBase, admin);

        if (!casoRepository.findByReportanteCorreoOrderByFechaRegistroDesc(CORREO_CIUDADANO, Pageable.unpaged()).isEmpty()) {
            log.info("Casos demo ya presentes en la base de datos.");
            return;
        }

        Object orgFundacion = usuarioFundacion.getOrganizacion() != null
                ? Hibernate.unproxy(usuarioFundacion.getOrganizacion())
                : null;
        Object orgVeterinaria = usuarioVeterinaria.getOrganizacion() != null
                ? Hibernate.unproxy(usuarioVeterinaria.getOrganizacion())
                : null;
        Fundacion fundacion = orgFundacion instanceof Fundacion f ? f : null;
        Veterinaria veterinaria = orgVeterinaria instanceof Veterinaria v ? v : null;

        crearCasoRegistrado(ciudadano, barrioBase);
        crearCasoEnAtencion(ciudadano, barrioBase, fundacion, veterinaria, usuarioVeterinaria);
        crearCasoResueltoConAlta(ciudadano, barrioBase, fundacion, veterinaria, usuarioVeterinaria, usuarioFundacion);
        crearCasoArchivado(ciudadano, barrioBase, admin);

        log.info("Datos demo cargados: usuarios por rol, casos en los 4 estados y bitácora con alta médica.");
    }

    private Usuario asegurarCiudadano(Barrio barrio) {
        return usuarioRepository.findByCorreo(CORREO_CIUDADANO)
                .orElseGet(() -> usuarioRepository.save(construirUsuario(
                        "Camila Ciudadana",
                        CORREO_CIUDADANO,
                        "3101112233",
                        Rol.CIUDADANO_VOLUNTARIO,
                        true,
                        true,
                        barrio,
                        barrio.getMunicipio(),
                        null)));
    }

    private Usuario asegurarFundacion(Barrio barrio, Usuario verificador) {
        return usuarioRepository.findByCorreo(CORREO_FUNDACION)
                .orElseGet(() -> {
                    Fundacion fundacion = Fundacion.builder()
                            .nombre("Fundación Huellas del Quindío")
                            .nit("900999001-9")
                            .direccion("Cra 14 #21-10, Armenia")
                            .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                            .fechaVerificacion(Instant.now())
                            .verificador(verificador)
                            .municipio(barrio.getMunicipio())
                            .capacidadAlbergue(15)
                            .aceptaCustodia(true)
                            .build();
                    Organizacion guardada = guardarOrganizacionSiNoExiste(fundacion);
                    return usuarioRepository.save(construirUsuario(
                            "Laura Fundación",
                            CORREO_FUNDACION,
                            "3112223344",
                            Rol.FUNDACION_REFUGIO,
                            true,
                            false,
                            barrio,
                            barrio.getMunicipio(),
                            guardada));
                });
    }

    private Usuario asegurarVeterinaria(Barrio barrio, Usuario verificador) {
        return usuarioRepository.findByCorreo(CORREO_VETERINARIA)
                .orElseGet(() -> {
                    Veterinaria veterinaria = Veterinaria.builder()
                            .nombre("Clínica Veterinaria San Francisco")
                            .nit("900999002-9")
                            .direccion("Av. Bolívar #18-45, Armenia")
                            .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                            .fechaVerificacion(Instant.now())
                            .verificador(verificador)
                            .municipio(barrio.getMunicipio())
                            .registroProfesional("TP-DEMO-001")
                            .especialidad("Medicina Interna y Cirugía")
                            .horarioAtencion("Lunes a Sábado 08:00 - 18:00")
                            .build();
                    Organizacion guardada = guardarOrganizacionSiNoExiste(veterinaria);
                    return usuarioRepository.save(construirUsuario(
                            "Dr. Andrés Veterinario",
                            CORREO_VETERINARIA,
                            "3123334455",
                            Rol.VETERINARIA,
                            true,
                            false,
                            barrio,
                            barrio.getMunicipio(),
                            guardada));
                });
    }

    private Organizacion guardarOrganizacionSiNoExiste(Organizacion organizacion) {
        if (organizacionRepository.existsByNit(organizacion.getNit())) {
            organizacion.setNit(organizacion.getNit() + "-D");
        }
        return organizacionRepository.save(organizacion);
    }

    private Usuario construirUsuario(
            String nombre,
            String correo,
            String telefono,
            Rol rol,
            boolean verificado,
            boolean disponibleRescate,
            Barrio barrio,
            Municipio municipio,
            Organizacion organizacion) {
        return Usuario.builder()
                .nombre(nombre)
                .correo(correo)
                .contrasena(passwordEncoder.encode(CONTRASENA_DEMO))
                .telefono(telefono)
                .rol(rol)
                .verificado(verificado)
                .activo(true)
                .disponibleRescate(disponibleRescate)
                .aceptoTratamientoDatos(true)
                .fechaAceptacionDatos(Instant.now())
                .barrio(barrio)
                .municipioInteres(municipio)
                .organizacion(organizacion)
                .build();
    }

    private void crearCasoRegistrado(Usuario ciudadano, Barrio barrio) {
        Instant ahora = Instant.now();
        Animal animal = guardarAnimal("Max", Especie.PERRO, "Criollo", "Dorado", Sexo.MACHO, "3 años",
                "Collar azul desgastado", ciudadano);
        Caso caso = casoRepository.save(Caso.builder()
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .descripcion("Perro extraviado visto cerca al parque principal, responde por Max.")
                .urgencia(Urgencia.ALTA)
                .estado(EstadoCaso.REGISTRADO)
                .direccionReferencia("Parque Sucre, Armenia")
                .fechaRegistro(ahora)
                .fechaUltimaActividad(ahora)
                .visible(true)
                .reportante(ciudadano)
                .animal(animal)
                .barrio(barrio)
                .build());
        guardarHistorial(caso, EstadoCaso.REGISTRADO, EstadoCaso.REGISTRADO, "Registro inicial demo", ahora, ciudadano);
    }

    private void crearCasoEnAtencion(
            Usuario ciudadano, Barrio barrio, Fundacion fundacion, Veterinaria veterinaria, Usuario usuarioVet) {
        Instant ahora = Instant.now();
        Instant hace2Dias = ahora.minus(2, ChronoUnit.DAYS);
        Animal animal = guardarAnimal("Luna", Especie.GATO, "Mestizo", "Blanco y gris", Sexo.HEMBRA, "1 año",
                "Cojera leve en pata trasera", null);
        Caso caso = casoRepository.save(Caso.builder()
                .tipoCaso(TipoCaso.HERIDO)
                .descripcion("Gata encontrada con herida superficial, actualmente bajo custodia y observación médica.")
                .urgencia(Urgencia.CRITICA)
                .estado(EstadoCaso.EN_ATENCION)
                .direccionReferencia("Av. Centenario #12-30")
                .fechaRegistro(hace2Dias)
                .fechaUltimaActividad(ahora)
                .fechaCustodia(ahora.minus(1, ChronoUnit.DAYS))
                .visible(true)
                .reportante(ciudadano)
                .animal(animal)
                .barrio(barrio)
                .fundacionCustodio(fundacion)
                .build());
        guardarHistorial(caso, EstadoCaso.REGISTRADO, EstadoCaso.EN_ATENCION,
                "Fundación asumió custodia para atención clínica", ahora.minus(1, ChronoUnit.DAYS), ciudadano);

        BitacoraMedica bitacora = bitacoraMedicaRepository.save(BitacoraMedica.builder()
                .caso(caso)
                .veterinaria(veterinaria)
                .veterinario(usuarioVet)
                .fechaApertura(ahora.minus(1, ChronoUnit.DAYS))
                .altaMedica(false)
                .build());
        registroMedicoRepository.save(RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico("Laceración leve en extremidad posterior derecha")
                .procedimiento("Limpieza antiséptica y vendaje")
                .tratamiento("Analgésico y curación cada 24h")
                .medicamentos("Meloxicam 0.5mg")
                .evolucion("Paciente estable, responde bien al manejo inicial")
                .fecha(ahora)
                .fechaFirma(ahora)
                .firmante(usuarioVet)
                .build());
    }

    private void crearCasoResueltoConAlta(
            Usuario ciudadano,
            Barrio barrio,
            Fundacion fundacion,
            Veterinaria veterinaria,
            Usuario usuarioVet,
            Usuario usuarioFundacion) {
        Instant ahora = Instant.now();
        Instant hace6Dias = ahora.minus(6, ChronoUnit.DAYS);
        Instant fechaAlta = ahora.minus(1, ChronoUnit.DAYS);
        Animal animal = guardarAnimal("Toby", Especie.PERRO, "Labrador mestizo", "Negro", Sexo.MACHO, "4 años",
                "Mancha blanca en el pecho", null);
        Caso caso = casoRepository.save(Caso.builder()
                .tipoCaso(TipoCaso.VULNERABLE)
                .descripcion("Perro rescatado con deshidratación leve; completó tratamiento con alta médica.")
                .urgencia(Urgencia.MEDIA)
                .estado(EstadoCaso.RESUELTO)
                .direccionReferencia("Barrio Granada, Calle 8")
                .fechaRegistro(hace6Dias)
                .fechaUltimaActividad(fechaAlta)
                .fechaCustodia(hace6Dias.plus(1, ChronoUnit.DAYS))
                .visible(true)
                .reportante(ciudadano)
                .animal(animal)
                .barrio(barrio)
                .fundacionCustodio(fundacion)
                .build());
        guardarHistorial(caso, EstadoCaso.EN_ATENCION, EstadoCaso.RESUELTO,
                "Caso resuelto tras emisión de alta médica", fechaAlta, usuarioFundacion);

        BitacoraMedica bitacora = bitacoraMedicaRepository.save(BitacoraMedica.builder()
                .caso(caso)
                .veterinaria(veterinaria)
                .veterinario(usuarioVet)
                .fechaApertura(hace6Dias.plus(1, ChronoUnit.DAYS))
                .altaMedica(true)
                .fechaAlta(fechaAlta)
                .build());
        registroMedicoRepository.save(RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico("Deshidratación moderada por exposición prolongada")
                .procedimiento("Fluidoterapia intravenosa y control hemático")
                .tratamiento("Hidratación oral y dieta blanda por 5 días")
                .medicamentos("Suero fisiológico y complejo B")
                .evolucion("Recuperación completa de hidratación y apetito normal")
                .fecha(hace6Dias.plus(2, ChronoUnit.DAYS))
                .fechaFirma(hace6Dias.plus(2, ChronoUnit.DAYS))
                .firmante(usuarioVet)
                .build());
        registroMedicoRepository.save(RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico("Alta médica definitiva")
                .procedimiento("Evaluación clínica de egreso")
                .tratamiento("Controles preventivos de rutina")
                .medicamentos("Sin medicación activa")
                .evolucion("Animal dado de alta en óptimas condiciones clínicas")
                .fecha(fechaAlta)
                .fechaFirma(fechaAlta)
                .firmante(usuarioVet)
                .build());

        cierreCasoRepository.save(CierreCaso.builder()
                .caso(caso)
                .resultado(ResultadoCierre.RECUPERADO)
                .entregadoA("Hogar de paso Fundación Huellas del Quindío")
                .observaciones("Alta veterinaria emitida y recuperación confirmada.")
                .fecha(fechaAlta)
                .confirmador(usuarioFundacion)
                .build());
    }

    private void crearCasoArchivado(Usuario ciudadano, Barrio barrio, Usuario admin) {
        Instant ahora = Instant.now();
        Instant hace10Dias = ahora.minus(10, ChronoUnit.DAYS);
        Instant fechaArchivo = ahora.minus(3, ChronoUnit.DAYS);
        Animal animal = guardarAnimal("Milo", Especie.GATO, "Atigrado", "Naranja", Sexo.MACHO, "8 meses",
                "Muy dócil", null);
        Caso caso = casoRepository.save(Caso.builder()
                .tipoCaso(TipoCaso.EN_ADOPCION)
                .descripcion("Gatito adoptado definitivamente y archivado para historial de plataforma.")
                .urgencia(Urgencia.BAJA)
                .estado(EstadoCaso.ARCHIVADO)
                .direccionReferencia("Centro de Armenia")
                .fechaRegistro(hace10Dias)
                .fechaUltimaActividad(fechaArchivo)
                .fechaArchivado(fechaArchivo)
                .visible(true)
                .reportante(ciudadano)
                .animal(animal)
                .barrio(barrio)
                .build());
        guardarHistorial(caso, EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO,
                "Caso archivado en historial", fechaArchivo, admin != null ? admin : ciudadano);
        cierreCasoRepository.save(CierreCaso.builder()
                .caso(caso)
                .resultado(ResultadoCierre.ENTREGADO_A_CUIDADOR)
                .entregadoA("Familia adoptante Gómez")
                .observaciones("Adopción formalizada y seguimiento cerrado.")
                .fecha(fechaArchivo)
                .confirmador(admin != null ? admin : ciudadano)
                .build());
    }

    private Animal guardarAnimal(
            String nombre, Especie especie, String raza, String color, Sexo sexo,
            String edad, String senas, Usuario propietario) {
        return animalRepository.save(Animal.builder()
                .nombre(nombre)
                .especie(especie)
                .raza(raza)
                .color(color)
                .sexo(sexo)
                .edadAproximada(edad)
                .senasParticulares(senas)
                .propietario(propietario)
                .build());
    }

    private void guardarHistorial(
            Caso caso, EstadoCaso anterior, EstadoCaso nuevo, String observacion, Instant fecha, Usuario usuario) {
        historialEstadoRepository.save(HistorialEstado.builder()
                .caso(caso)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .observacion(observacion)
                .fecha(fecha)
                .usuario(usuario)
                .build());
    }
}
