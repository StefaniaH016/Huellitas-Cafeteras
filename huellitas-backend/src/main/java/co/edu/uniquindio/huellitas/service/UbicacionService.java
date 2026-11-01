package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.ubicacion.BarrioDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.DepartamentoDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.MunicipioDTO;

import java.util.List;

public interface UbicacionService {

    List<DepartamentoDTO> listarDepartamentos();

    List<MunicipioDTO> listarMunicipios(Long departamentoId);

    List<MunicipioDTO> listarMunicipiosPorDepartamento(Long departamentoId);

    List<BarrioDTO> listarBarrios(Long municipioId);

    List<BarrioDTO> listarBarriosPorMunicipio(Long municipioId);
}

