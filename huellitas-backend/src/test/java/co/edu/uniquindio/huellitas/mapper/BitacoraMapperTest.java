package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.bitacora.AltaMedicaResponseDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.BitacoraDetalleDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.BitacoraResponseDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.EvolucionDetalleDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.PacienteResponseDTO;
import co.edu.uniquindio.huellitas.dto.bitacora.RegistroMedicoResponseDTO;
import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Sexo;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BitacoraMapperTest {

    private final BitacoraMapper mapper = new BitacoraMapper();

    @Test
    void mapeaBitacoraRegistroYAltaConNulls() {
        assertThat(mapper.toResponseDTO(null, 1L, 2L, "ABIERTA")).isNull();
        assertThat(mapper.toEvolucionDetalleDTO(null)).isNull();
        assertThat(mapper.toEvolucionDetalleDTOList(null)).isEmpty();
        assertThat(mapper.toDetalleDTO(null, 1L, List.of())).isNull();
        assertThat(mapper.toRegistroMedicoResponseDTO(null, 1L)).isNull();
        assertThat(mapper.toAltaMedicaResponseDTO(null, 1L)).isNull();

        Instant apertura = Instant.parse("2026-05-01T10:00:00Z");
        Instant alta = Instant.parse("2026-05-05T12:00:00Z");

        BitacoraMedica abierta = BitacoraMedica.builder()
                .id(10L)
                .fechaApertura(apertura)
                .altaMedica(false)
                .build();
        BitacoraResponseDTO resp = mapper.toResponseDTO(abierta, 100L, 20L, "ABIERTA");
        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.casoId()).isEqualTo(100L);
        assertThat(resp.veterinariaId()).isEqualTo(20L);
        assertThat(resp.estado()).isEqualTo("ABIERTA");

        BitacoraDetalleDTO detalleAbierta = mapper.toDetalleDTO(abierta, 100L, List.of());
        assertThat(detalleAbierta.estado()).isEqualTo("ABIERTA");
        assertThat(detalleAbierta.alta()).isNull();

        BitacoraMedica conAlta = BitacoraMedica.builder()
                .id(11L)
                .fechaApertura(apertura)
                .altaMedica(true)
                .fechaAlta(alta)
                .build();
        BitacoraDetalleDTO detalleAlta = mapper.toDetalleDTO(conAlta, 100L, List.of());
        assertThat(detalleAlta.estado()).isEqualTo("ALTA_REGISTRADA");
        assertThat(detalleAlta.alta()).isInstanceOf(Map.class);

        AltaMedicaResponseDTO altaDto = mapper.toAltaMedicaResponseDTO(conAlta, 100L);
        assertThat(altaDto.casoId()).isEqualTo(100L);
        assertThat(altaDto.estadoBitacora()).isEqualTo("ALTA_REGISTRADA");
        assertThat(altaDto.fechaAlta()).isNotBlank();

        RegistroMedico regCompleto = RegistroMedico.builder()
                .id(30L)
                .fecha(apertura)
                .fechaFirma(alta)
                .diagnostico("Otitis")
                .procedimiento("Limpieza")
                .tratamiento("Gotas")
                .medicamentos("Otomax")
                .evolucion("Mejoría notable")
                .firmante(Usuario.builder().nombre("Dra. Sofía").build())
                .build();
        RegistroMedico regMin = RegistroMedico.builder()
                .id(31L)
                .fecha(apertura)
                .diagnostico("Control")
                .build();

        List<EvolucionDetalleDTO> evoluciones = mapper.toEvolucionDetalleDTOList(List.of(regCompleto, regMin));
        assertThat(evoluciones).hasSize(2);
        assertThat(evoluciones.get(0).firmanteNombre()).isEqualTo("Dra. Sofía");
        assertThat(evoluciones.get(0).fechaFirma()).isNotBlank();
        assertThat(evoluciones.get(1).firmanteNombre()).isNull();
        assertThat(evoluciones.get(1).fechaFirma()).isNull();

        RegistroMedicoResponseDTO regResp = mapper.toRegistroMedicoResponseDTO(regCompleto, 10L);
        assertThat(regResp.id()).isEqualTo(30L);
        assertThat(regResp.bitacoraId()).isEqualTo(10L);
    }

    @Test
    void toPacienteResponseDTOYListaCubrenFotosUbicacionYEstados() {
        assertThat(mapper.toPacienteResponseDTO(null)).isNull();
        assertThat(mapper.toPacienteResponseDTOList(null)).isEmpty();

        Instant apertura = Instant.parse("2026-05-01T10:00:00Z");
        Instant alta = Instant.parse("2026-05-04T15:00:00Z");

        Caso casoPrincipal = Caso.builder()
                .id(50L)
                .descripcion("Rescatado en vía")
                .urgencia(Urgencia.ALTA)
                .estado(EstadoCaso.EN_ATENCION)
                .animal(Animal.builder()
                        .nombre("Firulais")
                        .especie(Especie.PERRO)
                        .raza("Criollo")
                        .sexo(Sexo.MACHO)
                        .build())
                .barrio(Barrio.builder()
                        .nombre("Centro")
                        .municipio(Municipio.builder().nombre("Armenia").build())
                        .build())
                .fotos(List.of(
                        Foto.builder().url("https://img.test/sec.jpg").principal(false).build(),
                        Foto.builder().url("https://img.test/main.jpg").principal(true).build()))
                .build();

        BitacoraMedica bmActiva = BitacoraMedica.builder()
                .id(1L)
                .caso(casoPrincipal)
                .fechaApertura(apertura)
                .altaMedica(false)
                .build();

        Caso casoFallback = Caso.builder()
                .id(51L)
                .animal(Animal.builder().nombre("Michi").build())
                .barrio(Barrio.builder().nombre("Norte").build())
                .fotos(List.of(Foto.builder().url("https://img.test/fallback.jpg").principal(false).build()))
                .build();

        BitacoraMedica bmAltaSinCampos = BitacoraMedica.builder()
                .id(2L)
                .caso(casoFallback)
                .fechaApertura(apertura)
                .altaMedica(true)
                .fechaAlta(alta)
                .build();

        BitacoraMedica bmSinCaso = BitacoraMedica.builder()
                .id(3L)
                .fechaApertura(apertura)
                .altaMedica(false)
                .build();

        List<PacienteResponseDTO> pacientes = mapper.toPacienteResponseDTOList(
                List.of(bmActiva, bmAltaSinCampos, bmSinCaso));

        assertThat(pacientes).hasSize(3);
        assertThat(pacientes.get(0).foto()).isEqualTo("https://img.test/main.jpg");
        assertThat(pacientes.get(0).municipio()).isEqualTo("Armenia");
        assertThat(pacientes.get(0).estado()).isEqualTo("EN_ATENCION");

        assertThat(pacientes.get(1).foto()).isEqualTo("https://img.test/fallback.jpg");
        assertThat(pacientes.get(1).municipio()).isNull();
        assertThat(pacientes.get(1).estado()).isEqualTo("ALTA_REGISTRADA");

        assertThat(pacientes.get(2).casoId()).isNull();
        assertThat(pacientes.get(2).estado()).isEqualTo("ABIERTA");
        assertThat(pacientes.get(2).foto()).isNull();
    }
}
