package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.aporte.CrearAporteDTO;
import co.edu.uniquindio.huellitas.service.AporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/casos/{id}/aportes")
@RequiredArgsConstructor
public class AporteController {

    private final AporteService aporteService;

    @GetMapping
    public ResponseEntity<List<AporteResponseDTO>> listarAportes(@PathVariable Long id) {
        return ResponseEntity.ok(aporteService.listarAportes(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AporteResponseDTO> crearAporte(
            @PathVariable Long id,
            @Valid @RequestBody CrearAporteDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        AporteResponseDTO response = aporteService.crearAporte(id, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
