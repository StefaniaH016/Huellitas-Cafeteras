package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/casos")
@Tag(name = "Casos / Reportes", description = "Gestión de casos de animales vulnerables o extraviados")
public class ReporteController {

    // TODO: Inyectar CasoService cuando Dev B lo implemente

    @PostMapping
    @Operation(
            summary = "Crear nuevo reporte de caso (HU-10)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Caso> crearReporte(
            @Valid @RequestBody CrearReporteDTO dto) {
        // TODO: implementar lógica de creación de reporte
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
