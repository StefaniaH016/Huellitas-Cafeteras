package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ModeracionMapperTest {

    private final ModeracionMapper mapper = new ModeracionMapper();

    @Test
    void toCasoModeracionDTOCubreNullPendienteYOcultado() {
        assertThat(mapper.toCasoModeracionDTO(null)).isNull();

        Instant fecha = Instant.parse("2026-04-10T09:00:00Z");
        Caso pendiente = Caso.builder()
                .id(12L)
                .motivoModeracion("Posible caso repetido")
                .moderacionPendiente(true)
                .fechaRegistro(fecha)
                .build();
        CasoModeracionDTO dtoPendiente = mapper.toCasoModeracionDTO(pendiente);
        assertThat(dtoPendiente.casoId()).isEqualTo(12L);
        assertThat(dtoPendiente.motivo()).isEqualTo("Posible caso repetido");
        assertThat(dtoPendiente.estadoRevision()).isEqualTo("PENDIENTE");
        assertThat(dtoPendiente.fechaCreacion()).isNotBlank();

        Caso ocultadoSinMotivo = Caso.builder()
                .id(13L)
                .motivoModeracion(null)
                .moderacionPendiente(false)
                .fechaRegistro(fecha)
                .build();
        CasoModeracionDTO dtoOcultado = mapper.toCasoModeracionDTO(ocultadoSinMotivo);
        assertThat(dtoOcultado.motivo()).isEqualTo("Reportado para revisión");
        assertThat(dtoOcultado.estadoRevision()).isEqualTo("OCULTADO");
    }

    @Test
    void toModeracionResponseDTOMapeaMantenidoYOcultado() {
        ModeracionResponseDTO mantenido = mapper.toModeracionResponseDTO(5L, true);
        assertThat(mantenido.casoId()).isEqualTo(5L);
        assertThat(mantenido.decision()).isEqualTo("MANTENIDO");
        assertThat(mantenido.fechaRevision()).isNotBlank();

        ModeracionResponseDTO ocultado = mapper.toModeracionResponseDTO(6L, false);
        assertThat(ocultado.casoId()).isEqualTo(6L);
        assertThat(ocultado.decision()).isEqualTo("OCULTADO");
        assertThat(ocultado.fechaRevision()).isNotBlank();
    }
}
