package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.service.BitacoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bitácora Médica", description = "Endpoints de atención veterinaria, bitácora clínica y pacientes")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    @PostMapping({"/v1/casos/{id}/bitacora", "/api/v1/casos/{id}/bitacora"})
    @PreAuthorize("hasRole('VETERINARIA')")
    @Operation(summary = "Abrir bitácora médica", description = "Abre la bitácora médica de un caso. Exclusivo para veterinarias verificadas y casos en estado EN_ATENCION.")
    public ResponseEntity<BitacoraResponseDTO> abrirBitacora(
            @PathVariable Long id,
            @RequestBody(required = false) AbrirBitacoraDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        BitacoraResponseDTO response = bitacoraService.abrirBitacora(id, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"/v1/casos/{id}/bitacora", "/api/v1/casos/{id}/bitacora"})
    @Operation(summary = "Consultar bitácora médica", description = "Consulta la bitácora médica completa. Restringido a Veterinaria, Fundación custodia y Reportante (Ley 1581 y Ley 576).")
    public ResponseEntity<BitacoraDetalleDTO> obtenerBitacora(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String correoUsuario = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(bitacoraService.obtenerBitacora(id, correoUsuario));
    }

    @PostMapping({"/v1/casos/{id}/bitacora/evoluciones", "/api/v1/casos/{id}/bitacora/evoluciones"})
    @PreAuthorize("hasRole('VETERINARIA')")
    @Operation(summary = "Registrar evolución médica", description = "Agrega una nueva evolución médica inmutable a la bitácora.")
    public ResponseEntity<RegistroMedicoResponseDTO> agregarEvolucion(
            @PathVariable Long id,
            @Valid @RequestBody EvolucionDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        RegistroMedicoResponseDTO response = bitacoraService.agregarEvolucion(id, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping({"/v1/casos/{id}/bitacora/alta", "/api/v1/casos/{id}/bitacora/alta"})
    @PreAuthorize("hasRole('VETERINARIA')")
    @Operation(summary = "Registrar alta médica", description = "Registra el alta médica del paciente y bloquea evoluciones futuras.")
    public ResponseEntity<AltaMedicaResponseDTO> registrarAlta(
            @PathVariable Long id,
            @Valid @RequestBody AltaMedicaDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bitacoraService.registrarAlta(id, dto, userDetails.getUsername()));
    }

    @GetMapping({"/v1/bitacoras/mis-pacientes", "/api/v1/bitacoras/mis-pacientes"})
    @PreAuthorize("hasRole('VETERINARIA')")
    @Operation(summary = "Listar mis pacientes", description = "Lista los pacientes atendidos por la veterinaria autenticada (Panel de veterinaria HU 15).")
    public ResponseEntity<List<PacienteResponseDTO>> listarMisPacientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bitacoraService.listarMisPacientes(userDetails.getUsername()));
    }

    @GetMapping({"/v1/casos/{id}/bitacora/pdf", "/api/v1/casos/{id}/bitacora/pdf"})
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Descargar resumen médico en PDF", description = "Genera y descarga el resumen clínico en PDF de la bitácora.")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String correo = userDetails != null ? userDetails.getUsername() : null;
        byte[] pdf = bitacoraService.generarPdfResumen(id, correo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "resumen-clinico-caso-" + id + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
