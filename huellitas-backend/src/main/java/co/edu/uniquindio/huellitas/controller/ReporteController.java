package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/casos")
public class ReporteController {

    // TODO: Inyectar CasoService cuando Dev B lo implemente

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Caso> crearReporte(
            @Valid @RequestBody CrearReporteDTO dto) {
        // TODO: implementar lógica de creación de reporte
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
