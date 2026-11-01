package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.ubicacion.BarrioDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.DepartamentoDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.MunicipioDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UbicacionMapper - Pruebas Unitarias")
class UbicacionMapperTest {

    private final UbicacionMapper mapper = new UbicacionMapper();

    @Test
    @DisplayName("toDepartamentoDTO mapea correctamente y maneja nulo")
    void testToDepartamentoDTO() {
        assertNull(mapper.toDepartamentoDTO(null));

        Departamento depto = Departamento.builder().id(10L).nombre("Quindío").build();
        DepartamentoDTO dto = mapper.toDepartamentoDTO(depto);

        assertNotNull(dto);
        assertEquals(10L, dto.id());
        assertEquals("Quindío", dto.nombre());
    }

    @Test
    @DisplayName("toMunicipioDTO mapea id de departamento y maneja departamento nulo")
    void testToMunicipioDTO() {
        assertNull(mapper.toMunicipioDTO(null));

        Departamento depto = Departamento.builder().id(10L).nombre("Quindío").build();
        Municipio mun = Municipio.builder().id(20L).nombre("Armenia").departamento(depto).build();

        MunicipioDTO dto = mapper.toMunicipioDTO(mun);
        assertNotNull(dto);
        assertEquals(20L, dto.id());
        assertEquals("Armenia", dto.nombre());
        assertEquals(10L, dto.departamentoId());

        Municipio munSinDepto = Municipio.builder().id(21L).nombre("Calarcá").build();
        MunicipioDTO dtoSinDepto = mapper.toMunicipioDTO(munSinDepto);
        assertNotNull(dtoSinDepto);
        assertNull(dtoSinDepto.departamentoId());
    }

    @Test
    @DisplayName("toBarrioDTO mapea id de municipio correctamente")
    void testToBarrioDTO() {
        assertNull(mapper.toBarrioDTO(null));

        Municipio mun = Municipio.builder().id(20L).nombre("Armenia").build();
        Barrio barrio = Barrio.builder().id(30L).nombre("Granada").municipio(mun).build();

        BarrioDTO dto = mapper.toBarrioDTO(barrio);
        assertNotNull(dto);
        assertEquals(30L, dto.id());
        assertEquals("Granada", dto.nombre());
        assertEquals(20L, dto.municipioId());
    }

    @Test
    @DisplayName("Listas vacías o nulas se manejan sin lanzar excepción")
    void testListas() {
        assertTrue(mapper.toDepartamentoDTOList(null).isEmpty());
        assertTrue(mapper.toMunicipioDTOList(null).isEmpty());
        assertTrue(mapper.toBarrioDTOList(null).isEmpty());

        Departamento depto = Departamento.builder().id(1L).nombre("Quindío").build();
        List<DepartamentoDTO> dtoList = mapper.toDepartamentoDTOList(List.of(depto));
        assertEquals(1, dtoList.size());
        assertEquals("Quindío", dtoList.get(0).nombre());
    }
}

