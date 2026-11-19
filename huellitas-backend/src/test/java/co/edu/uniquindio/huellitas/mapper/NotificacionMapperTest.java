package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Notificacion;
import co.edu.uniquindio.huellitas.entity.enums.TipoNotificacion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacionMapperTest {

    private final NotificacionMapper mapper = new NotificacionMapper();

    @Test
    void toDTOCubreCompletaConNullsYCasoOpcional() {
        assertThat(mapper.toDTO(null)).isNull();

        Instant fecha = Instant.parse("2026-04-15T14:30:00Z");
        Notificacion completa = Notificacion.builder()
                .id(1L)
                .tipo(TipoNotificacion.VERIFICACION)
                .mensaje("Organización verificada")
                .leida(true)
                .fecha(fecha)
                .caso(Caso.builder().id(88L).build())
                .build();

        NotificacionDTO dtoCompleta = mapper.toDTO(completa);
        assertThat(dtoCompleta.id()).isEqualTo(1L);
        assertThat(dtoCompleta.tipo()).isEqualTo("VERIFICACION");
        assertThat(dtoCompleta.mensaje()).isEqualTo("Organización verificada");
        assertThat(dtoCompleta.leida()).isTrue();
        assertThat(dtoCompleta.casoId()).isEqualTo(88L);
        assertThat(dtoCompleta.fecha()).isNotBlank();

        Notificacion sinTipoNiCaso = Notificacion.builder()
                .id(2L)
                .tipo(null)
                .mensaje("Notificación general")
                .leida(false)
                .fecha(fecha)
                .caso(null)
                .build();

        NotificacionDTO dtoMin = mapper.toDTO(sinTipoNiCaso);
        assertThat(dtoMin.tipo()).isEqualTo("GENERAL");
        assertThat(dtoMin.casoId()).isNull();
        assertThat(dtoMin.leida()).isFalse();
    }
}
