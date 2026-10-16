package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.ubicacion.BarrioDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.DepartamentoDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.MunicipioDTO;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.DepartamentoRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final BarrioRepository barrioRepository;

    @GetMapping("/departamentos")
    public ResponseEntity<List<DepartamentoDTO>> listarDepartamentos() {
        List<DepartamentoDTO> list = departamentoRepository.findAll().stream()
                .map(d -> new DepartamentoDTO(d.getId(), d.getNombre()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/municipios")
    public ResponseEntity<List<MunicipioDTO>> listarMunicipios(
            @RequestParam(required = false) Long departamentoId) {
        List<MunicipioDTO> list;
        if (departamentoId != null) {
            list = municipioRepository.findByDepartamentoId(departamentoId).stream()
                    .map(m -> new MunicipioDTO(m.getId(), m.getNombre(), m.getDepartamento().getId()))
                    .toList();
        } else {
            list = municipioRepository.findAll().stream()
                    .map(m -> new MunicipioDTO(m.getId(), m.getNombre(), m.getDepartamento().getId()))
                    .toList();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/departamentos/{id}/municipios")
    public ResponseEntity<List<MunicipioDTO>> listarMunicipiosPorDepartamento(
            @PathVariable Long id) {
        List<MunicipioDTO> list = municipioRepository.findByDepartamentoId(id).stream()
                .map(m -> new MunicipioDTO(m.getId(), m.getNombre(), m.getDepartamento().getId()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/barrios")
    public ResponseEntity<List<BarrioDTO>> listarBarrios(
            @RequestParam(required = false) Long municipioId) {
        List<BarrioDTO> list;
        if (municipioId != null) {
            list = barrioRepository.findByMunicipioId(municipioId).stream()
                    .map(b -> new BarrioDTO(b.getId(), b.getNombre(), b.getMunicipio().getId()))
                    .toList();
        } else {
            list = barrioRepository.findAll().stream()
                    .map(b -> new BarrioDTO(b.getId(), b.getNombre(), b.getMunicipio().getId()))
                    .toList();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/municipios/{id}/barrios")
    public ResponseEntity<List<BarrioDTO>> listarBarriosPorMunicipio(
            @PathVariable Long id) {
        List<BarrioDTO> list = barrioRepository.findByMunicipioId(id).stream()
                .map(b -> new BarrioDTO(b.getId(), b.getNombre(), b.getMunicipio().getId()))
                .toList();
        return ResponseEntity.ok(list);
    }
}
