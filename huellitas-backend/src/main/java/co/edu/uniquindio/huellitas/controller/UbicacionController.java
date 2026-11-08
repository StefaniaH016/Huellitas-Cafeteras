package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.ubicacion.BarrioDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.DepartamentoDTO;
import co.edu.uniquindio.huellitas.dto.ubicacion.MunicipioDTO;
import co.edu.uniquindio.huellitas.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/v1/ubicaciones", "/api/v1/ubicaciones"})
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping("/departamentos")
    public ResponseEntity<List<DepartamentoDTO>> listarDepartamentos() {
        return ResponseEntity.ok(ubicacionService.listarDepartamentos());
    }

    @GetMapping("/municipios")
    public ResponseEntity<List<MunicipioDTO>> listarMunicipios(
            @RequestParam(required = false) Long departamentoId) {
        return ResponseEntity.ok(ubicacionService.listarMunicipios(departamentoId));
    }

    @GetMapping("/departamentos/{id}/municipios")
    public ResponseEntity<List<MunicipioDTO>> listarMunicipiosPorDepartamento(
            @PathVariable Long id) {
        return ResponseEntity.ok(ubicacionService.listarMunicipiosPorDepartamento(id));
    }

    @GetMapping("/barrios")
    public ResponseEntity<List<BarrioDTO>> listarBarrios(
            @RequestParam(required = false) Long municipioId) {
        return ResponseEntity.ok(ubicacionService.listarBarrios(municipioId));
    }

    @GetMapping("/municipios/{id}/barrios")
    public ResponseEntity<List<BarrioDTO>> listarBarriosPorMunicipio(
            @PathVariable Long id) {
        return ResponseEntity.ok(ubicacionService.listarBarriosPorMunicipio(id));
    }
}
