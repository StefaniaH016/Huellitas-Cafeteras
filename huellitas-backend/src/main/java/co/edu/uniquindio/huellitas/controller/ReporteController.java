package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.service.CasoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/v1/casos")
@RequiredArgsConstructor
@Tag(name = "Casos", description = "API para gestión de casos de animales")
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

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CasoResponseDTO> actualizarReporte(
            @PathVariable Long id,
            @Valid @RequestBody CrearReporteDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CasoResponseDTO response = casoService.actualizarReporte(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarCaso(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        casoService.eliminarCaso(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarFoto(
            @PathVariable Long id,
            @PathVariable Long fotoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        casoService.eliminarFoto(id, fotoId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
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

    @GetMapping("/feed")
    public ResponseEntity<Page<CasoListItemDTO>> getFeed(Pageable pageable) {
        Page<CasoListItemDTO> feed = casoService.getFeed(pageable);
        return ResponseEntity.ok(feed);
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

    // 13.3: Gestionar custodia (asumir o liberar)
    @PatchMapping("/{id}/custodia")
    @PreAuthorize("hasAnyRole('FUNDACION_REFUGIO', 'ADMINISTRADOR')")
    @Operation(summary = "Gestionar custodia de un caso", description = "Asume o libera la custodia de un caso. Solo fundaciones verificadas con aceptaCustodia=true pueden asumir custodia.")
    public ResponseEntity<CasoResponseDTO> gestionarCustodia(
            @Parameter(description = "ID del caso") @PathVariable Long id,
            @Valid @RequestBody CustodiaDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CasoResponseDTO response = casoService.gestionarCustodia(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 13.4: Casos en custodia de la fundación autenticada
    @GetMapping("/en-custodia")
    @PreAuthorize("hasAnyRole('FUNDACION_REFUGIO', 'ADMINISTRADOR')")
    @Operation(summary = "Casos en custodia", description = "Lista los casos bajo custodia de la fundación autenticada")
    public ResponseEntity<Page<CasoListItemDTO>> listarCasosEnCustodia(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<CasoListItemDTO> list = casoService.listarCasosEnCustodia(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(list);
    }

    // 13.4: Casos sin custodia en un municipio
    @GetMapping("/sin-custodia")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Casos sin custodia", description = "Lista los casos sin custodia en un municipio específico")
    public ResponseEntity<Page<CasoListItemDTO>> listarCasosSinCustodia(
            @Parameter(description = "ID del municipio") @RequestParam Long municipioId,
            Pageable pageable) {
        Page<CasoListItemDTO> list = casoService.listarCasosSinCustodia(municipioId, pageable);
        return ResponseEntity.ok(list);
    }
}
