package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.ubicacion.BarrioDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.DepartamentoDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.MunicipioDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.mapper.UbicacionMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.DepartamentoRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UbicacionServiceImpl implements UbicacionService {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final BarrioRepository barrioRepository;
    private final UbicacionMapper ubicacionMapper;

    @Override
    public List<DepartamentoDTO> listarDepartamentos() {
        List<Departamento> list = departamentoRepository.findAll();
        return ubicacionMapper.toDepartamentoDTOList(list);
    }

    @Override
    public List<MunicipioDTO> listarMunicipios(Long departamentoId) {
        List<Municipio> list = (departamentoId != null)
                ? municipioRepository.findByDepartamentoId(departamentoId)
                : municipioRepository.findAll();
        return ubicacionMapper.toMunicipioDTOList(list);
    }

    @Override
    public List<MunicipioDTO> listarMunicipiosPorDepartamento(Long departamentoId) {
        List<Municipio> list = municipioRepository.findByDepartamentoId(departamentoId);
        return ubicacionMapper.toMunicipioDTOList(list);
    }

    @Override
    public List<BarrioDTO> listarBarrios(Long municipioId) {
        List<Barrio> list = (municipioId != null)
                ? barrioRepository.findByMunicipioId(municipioId)
                : barrioRepository.findAll();
        return ubicacionMapper.toBarrioDTOList(list);
    }

    @Override
    public List<BarrioDTO> listarBarriosPorMunicipio(Long municipioId) {
        List<Barrio> list = barrioRepository.findByMunicipioId(municipioId);
        return ubicacionMapper.toBarrioDTOList(list);
    }
}

