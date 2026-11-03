package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.ubicacion.BarrioDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.DepartamentoDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.MunicipioDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UbicacionMapper {

    public DepartamentoDTO toDepartamentoDTO(Departamento departamento) {
        if (departamento == null) {
            return null;
        }
        return new DepartamentoDTO(departamento.getId(), departamento.getNombre());
    }

    public List<DepartamentoDTO> toDepartamentoDTOList(List<Departamento> departamentos) {
        if (departamentos == null) {
            return List.of();
        }
        return departamentos.stream().map(this::toDepartamentoDTO).toList();
    }

    public MunicipioDTO toMunicipioDTO(Municipio municipio) {
        if (municipio == null) {
            return null;
        }
        Long deptoId = (municipio.getDepartamento() != null) ? municipio.getDepartamento().getId() : null;
        return new MunicipioDTO(municipio.getId(), municipio.getNombre(), deptoId);
    }

    public List<MunicipioDTO> toMunicipioDTOList(List<Municipio> municipios) {
        if (municipios == null) {
            return List.of();
        }
        return municipios.stream().map(this::toMunicipioDTO).toList();
    }

    public BarrioDTO toBarrioDTO(Barrio barrio) {
        if (barrio == null) {
            return null;
        }
        Long munId = (barrio.getMunicipio() != null) ? barrio.getMunicipio().getId() : null;
        return new BarrioDTO(barrio.getId(), barrio.getNombre(), munId);
    }

    public List<BarrioDTO> toBarrioDTOList(List<Barrio> barrios) {
        if (barrios == null) {
            return List.of();
        }
        return barrios.stream().map(this::toBarrioDTO).toList();
    }
}

