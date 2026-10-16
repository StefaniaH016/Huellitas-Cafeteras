package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.service.CasoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/casos")
@RequiredArgsConstructor
public class ReporteController {

    private final CasoService casoService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CasoResponseDTO> crearReporte(
            @Valid @RequestBody CrearReporteDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CasoResponseDTO response = casoService.crearCaso(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CasoListItemDTO>> listarCasos(
            @ModelAttribute FiltrosCasoDTO filtros,
            Pageable pageable) {
        Page<CasoListItemDTO> list = casoService.listarCasos(filtros, pageable);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/mis-reportes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CasoListItemDTO>> misReportes(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<CasoListItemDTO> list = casoService.misReportes(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasoDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        CasoDetalleDTO detalle = casoService.obtenerDetalle(id);
        return ResponseEntity.ok(detalle);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CasoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CasoResponseDTO response = casoService.actualizarEstado(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEstadoDTO>> obtenerHistorial(@PathVariable Long id) {
        List<HistorialEstadoDTO> historial = casoService.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }

    @PostMapping("/{id}/cierre")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CasoResponseDTO> cerrarCaso(
            @PathVariable Long id,
            @Valid @RequestBody CierreCasoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CasoResponseDTO response = casoService.cerrarCaso(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archivar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CasoResponseDTO> archivarCaso(
            @PathVariable Long id,
            @Valid @RequestBody ArchivarCasoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CasoResponseDTO response = casoService.archivarCaso(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
