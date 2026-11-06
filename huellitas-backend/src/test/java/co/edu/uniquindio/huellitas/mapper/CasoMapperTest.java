package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Sexo;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CasoMapper - Pruebas Unitarias")
class CasoMapperTest {

    private final CasoMapper mapper = new CasoMapper();

    @Test
    @DisplayName("toResponseDTO mapea atributos y maneja relaciones nulas")
    void testToResponseDTO() {
        assertNull(mapper.toResponseDTO(null));

        Caso caso = Caso.builder()
                .id(1L)
                .tipoCaso(TipoCaso.EXTRAVIADO)
                .urgencia(Urgencia.MEDIA)
                .estado(EstadoCaso.REGISTRADO)
                .descripcion("Perro perdido cerca al parque")
                .direccionReferencia("Cerca a la iglesia")
                .fechaRegistro(Instant.parse("2026-10-31T15:30:00Z"))
                .build();

        CasoResponseDTO dto = mapper.toResponseDTO(caso);
        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals(TipoCaso.EXTRAVIADO, dto.tipoCaso());
        assertEquals("Perro perdido cerca al parque", dto.descripcion());
        assertNull(dto.autorId());
        assertNull(dto.barrioId());
        assertNotNull(dto.fechaCreacion());
    }

    @Test
    @DisplayName("toListItemDTO extrae jerarquía de ubicación y foto principal")
    void testToListItemDTO() {
        assertNull(mapper.toListItemDTO(null));

        Departamento dep = Departamento.builder().id(1L).nombre("Quindío").build();
        Municipio mun = Municipio.builder().id(2L).nombre("Armenia").departamento(dep).build();
        Barrio bar = Barrio.builder().id(3L).nombre("Centro").municipio(mun).build();

        Animal animal = Animal.builder().id(5L).nombre("Max").especie(Especie.PERRO).build();

        Foto f1 = Foto.builder().id(11L).url("http://foto1.jpg").principal(false).build();
        Foto f2 = Foto.builder().id(12L).url("http://foto2.jpg").principal(true).build();

        Caso caso = Caso.builder()
                .id(100L)
                .tipoCaso(TipoCaso.VULNERABLE)
                .estado(EstadoCaso.REGISTRADO)
                .urgencia(Urgencia.ALTA)
                .descripcion("Caso urgente")
                .animal(animal)
                .barrio(bar)
                .fotos(List.of(f1, f2))
                .fechaRegistro(Instant.now())
                .build();

        CasoListItemDTO item = mapper.toListItemDTO(caso);
        assertNotNull(item);
        assertEquals(100L, item.id());
        assertEquals("Quindío", item.departamento());
        assertEquals("Armenia", item.municipio());
        assertEquals("Centro", item.barrio());
        assertEquals(Especie.PERRO, item.especie());
        assertEquals("http://foto2.jpg", item.fotoPrincipal());
    }

    @Test
    @DisplayName("Las fotos del aporte no aparecen en la galería del caso")
    void testFiltraFotosDeAportesDeLaGaleriaDelCaso() {
        Seguimiento seguimiento = Seguimiento.builder().id(15L).build();
        Foto fotoCaso = Foto.builder().id(1L).url("caso.jpg").principal(true).build();
        Foto fotoAporte = Foto.builder()
                .id(2L)
                .url("aporte.jpg")
                .principal(false)
                .seguimiento(seguimiento)
                .build();

        assertEquals(
                List.of("caso.jpg"),
                mapper.toFotoDetalleDTOList(List.of(fotoCaso, fotoAporte)).stream()
                        .map(FotoDetalleDTO::url)
                        .toList());
        assertNull(mapper.extraerFotoPrincipal(
                Caso.builder().fotos(List.of(fotoAporte)).build()));
    }

    @Test
    @DisplayName("toCasoDetalleDTO incluye reportante y fundación custodio")
    void testToCasoDetalleDTOIncluyeResponsables() {
        Usuario reportante = Usuario.builder().id(7L).nombre("Ana").build();
        Fundacion fundacion = Fundacion.builder().id(12L).nombre("Huellitas").build();
        Caso caso = Caso.builder()
                .id(100L)
                .tipoCaso(TipoCaso.HERIDO)
                .estado(EstadoCaso.EN_ATENCION)
                .urgencia(Urgencia.ALTA)
                .descripcion("Caso de prueba")
                .reportante(reportante)
                .fundacionCustodio(fundacion)
                .fechaRegistro(Instant.parse("2026-10-31T15:30:00Z"))
                .build();

        CasoDetalleDTO detalle = mapper.toCasoDetalleDTO(
                caso, null, null, List.of(), List.of(), List.of(), null);

        assertEquals(7L, detalle.autorId());
        assertEquals("Ana", detalle.autorNombre());
        assertEquals(12L, detalle.fundacionCustodioId());
        assertEquals("Huellitas", detalle.fundacionCustodioNombre());
    }

    @Test
    @DisplayName("El resumen médico muestra el profesional aunque no haya organización asociada")
    void testToResumenMedicoDTOUsaElVeterinarioResponsableComoAlternativa() {
        Usuario veterinario = Usuario.builder().id(8L).nombre("Dra. Tefa").build();
        BitacoraMedica bitacora = BitacoraMedica.builder()
                .id(20L)
                .fechaApertura(Instant.parse("2026-11-05T15:00:00Z"))
                .veterinario(veterinario)
                .build();

        ResumenMedicoDTO resumen = mapper.toResumenMedicoDTO(bitacora);

        assertEquals("Dra. Tefa", resumen.veterinariaNombre());
        assertEquals(8L, resumen.responsableId());
    }

    @Test
    @DisplayName("El resumen médico prioriza el nombre de la organización veterinaria")
    void testToResumenMedicoDTOPriorizaOrganizacionVeterinaria() {
        Veterinaria organizacion = Veterinaria.builder().id(13L).nombre("Clínica Huellitas").build();
        Usuario veterinario = Usuario.builder()
                .id(8L)
                .nombre("Dra. Tefa")
                .organizacion(organizacion)
                .build();
        BitacoraMedica bitacora = BitacoraMedica.builder()
                .veterinario(veterinario)
                .build();

        ResumenMedicoDTO resumen = mapper.toResumenMedicoDTO(bitacora);

        assertEquals("Clínica Huellitas", resumen.veterinariaNombre());
        assertEquals(8L, resumen.responsableId());
    }

    @Test
    @DisplayName("toAnimalEntity mapea desde AnimalReporteDTO")
    void testToAnimalEntity() {
        assertNull(mapper.toAnimalEntity(null, null));

        AnimalReporteDTO dto = new AnimalReporteDTO(
                "Rocky",
                Especie.PERRO,
                "Criollo",
                "Negro",
                Sexo.MACHO,
                "2 años",
                "Mancha blanca en el pecho"
        );
        Usuario prop = Usuario.builder().id(9L).build();

        Animal entity = mapper.toAnimalEntity(dto, prop);
        assertNotNull(entity);
        assertEquals("Rocky", entity.getNombre());
        assertEquals(Especie.PERRO, entity.getEspecie());
        assertEquals("Criollo", entity.getRaza());
        assertEquals(prop, entity.getPropietario());
    }
}
