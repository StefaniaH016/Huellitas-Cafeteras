package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Departamento;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.DepartamentoRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones", description = "Consulta de departamentos, municipios y barrios")
public class UbicacionController {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final BarrioRepository barrioRepository;

    @GetMapping("/departamentos")
    @Operation(summary = "Listar todos los departamentos")
    public ResponseEntity<List<Departamento>> listarDepartamentos() {
        return ResponseEntity.ok(departamentoRepository.findAll());
    }

    @GetMapping("/departamentos/{id}/municipios")
    @Operation(summary = "Listar municipios de un departamento")
    public ResponseEntity<List<Municipio>> listarMunicipiosPorDepartamento(
            @PathVariable Long id) {
        return ResponseEntity.ok(municipioRepository.findByDepartamentoId(id));
    }

    @GetMapping("/municipios/{id}/barrios")
    @Operation(summary = "Listar barrios de un municipio")
    public ResponseEntity<List<Barrio>> listarBarriosPorMunicipio(
            @PathVariable Long id) {
        return ResponseEntity.ok(barrioRepository.findByMunicipioId(id));
    }
}
