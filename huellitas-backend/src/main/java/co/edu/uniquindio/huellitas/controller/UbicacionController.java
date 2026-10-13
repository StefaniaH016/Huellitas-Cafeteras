package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.DepartamentoRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final BarrioRepository barrioRepository;

    @GetMapping("/departamentos")
    public ResponseEntity<List<Departamento>> listarDepartamentos() {
        return ResponseEntity.ok(departamentoRepository.findAll());
    }

    @GetMapping("/departamentos/{id}/municipios")
    public ResponseEntity<List<Municipio>> listarMunicipiosPorDepartamento(
            @PathVariable Long id) {
        return ResponseEntity.ok(municipioRepository.findByDepartamentoId(id));
    }

    @GetMapping("/municipios/{id}/barrios")
    public ResponseEntity<List<Barrio>> listarBarriosPorMunicipio(
            @PathVariable Long id) {
        return ResponseEntity.ok(barrioRepository.findByMunicipioId(id));
    }
}
