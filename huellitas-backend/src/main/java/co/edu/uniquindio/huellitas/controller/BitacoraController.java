package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.service.BitacoraService;
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

@RestController
@RequestMapping("/v1/casos/{id}/bitacora")
@RequiredArgsConstructor
public class BitacoraController {

    private final BitacoraService bitacoraService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VETERINARIA', 'ADMINISTRADOR')")
    public ResponseEntity<BitacoraResponseDTO> abrirBitacora(
            @PathVariable Long id,
            @RequestBody(required = false) AbrirBitacoraDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        BitacoraResponseDTO response = bitacoraService.abrirBitacora(id, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<BitacoraDetalleDTO> obtenerBitacora(@PathVariable Long id) {
        return ResponseEntity.ok(bitacoraService.obtenerBitacora(id));
    }

    @PostMapping("/evoluciones")
    @PreAuthorize("hasAnyRole('VETERINARIA', 'ADMINISTRADOR')")
    public ResponseEntity<RegistroMedicoResponseDTO> agregarEvolucion(
            @PathVariable Long id,
            @Valid @RequestBody EvolucionDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        RegistroMedicoResponseDTO response = bitacoraService.agregarEvolucion(id, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/alta")
    @PreAuthorize("hasAnyRole('VETERINARIA', 'ADMINISTRADOR')")
    public ResponseEntity<AltaMedicaResponseDTO> registrarAlta(
            @PathVariable Long id,
            @Valid @RequestBody AltaMedicaDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bitacoraService.registrarAlta(id, dto, userDetails.getUsername()));
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        byte[] pdf = bitacoraService.generarPdfResumen(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "resumen-clinico-caso-" + id + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
