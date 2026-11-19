package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.reporte.AporteDetalleDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AporteMapperTest {

    private final AporteMapper mapper = new AporteMapper();

    @Test
    void toResponseDTOMapeaEntidadCompletaYNulls() {
        assertThat(mapper.toResponseDTO(null, 1L)).isNull();
        assertThat(mapper.toResponseDTO(null, 1L, 2L, "Centro", List.of())).isNull();

        Instant fecha = Instant.parse("2026-06-01T15:00:00Z");
        Seguimiento seguimiento = Seguimiento.builder()
                .id(10L)
                .autor(Usuario.builder().id(5L).nombre("Ana").build())
                .comentario("Avistado frente al parque")
                .ubicacionTexto("Parque Sucre")
                .fecha(fecha)
                .fotografias(List.of(Foto.builder().url("https://img.test/1.jpg").build()))
                .build();

        AporteResponseDTO dto = mapper.toResponseDTO(seguimiento, 99L);
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.casoId()).isEqualTo(99L);
        assertThat(dto.autorId()).isEqualTo(5L);
        assertThat(dto.descripcion()).isEqualTo("Avistado frente al parque");
        assertThat(dto.ubicacionTexto()).isEqualTo("Parque Sucre");
        assertThat(dto.fotografias()).containsExactly("https://img.test/1.jpg");
        assertThat(dto.fecha()).isNotBlank();

        Seguimiento sinAutorNiFotos = Seguimiento.builder()
                .id(11L)
                .comentario("Sin fotos")
                .fecha(fecha)
                .fotografias(null)
                .build();
        AporteResponseDTO dtoMin = mapper.toResponseDTO(sinAutorNiFotos, 99L);
        assertThat(dtoMin.autorId()).isNull();
        assertThat(dtoMin.fotografias()).isEmpty();

        AporteResponseDTO dtoExplicit = mapper.toResponseDTO(sinAutorNiFotos, 99L, 7L, "Norte", null);
        assertThat(dtoExplicit.autorId()).isEqualTo(7L);
        assertThat(dtoExplicit.ubicacionTexto()).isEqualTo("Norte");
        assertThat(dtoExplicit.fotografias()).isEmpty();
    }

    @Test
    void toDetalleDTOYListaCubrenZonaConYSinMunicipio() {
        assertThat(mapper.toDetalleDTO(null)).isNull();
        assertThat(mapper.toDetalleDTOList(null)).isEmpty();

        Municipio armenia = Municipio.builder().id(1L).nombre("Armenia").build();
        Barrio conMunicipio = Barrio.builder().id(2L).nombre("Granada").municipio(armenia).build();
        Barrio sinMunicipio = Barrio.builder().id(3L).nombre("Centro").build();
        Instant fecha = Instant.parse("2026-06-02T10:00:00Z");

        Seguimiento completo = Seguimiento.builder()
                .id(1L)
                .tipo(TipoAporte.AVISTAMIENTO)
                .comentario("Visto caminando hacia la esquina")
                .fecha(fecha)
                .autor(Usuario.builder().nombre("Carlos").build())
                .barrio(conMunicipio)
                .ubicacionTexto("Esquina calle 10")
                .fotografias(List.of(Foto.builder().url("https://img.test/a.jpg").build()))
                .build();

        Seguimiento soloBarrio = Seguimiento.builder()
                .id(2L)
                .tipo(TipoAporte.COMENTARIO)
                .comentario("Sigue por el sector")
                .fecha(fecha)
                .barrio(sinMunicipio)
                .build();

        Seguimiento sinBarrioNiAutor = Seguimiento.builder()
                .id(3L)
                .tipo(TipoAporte.ACTUALIZACION)
                .comentario("Sin zona")
                .fecha(fecha)
                .build();

        List<AporteDetalleDTO> detalles = mapper.toDetalleDTOList(List.of(completo, soloBarrio, sinBarrioNiAutor));
        assertThat(detalles).hasSize(3);
        assertThat(detalles.get(0).zona()).isEqualTo("Granada · Armenia");
        assertThat(detalles.get(0).autorNombre()).isEqualTo("Carlos");
        assertThat(detalles.get(1).zona()).isEqualTo("Centro");
        assertThat(detalles.get(2).zona()).isNull();
        assertThat(detalles.get(2).autorNombre()).isNull();
    }
}
