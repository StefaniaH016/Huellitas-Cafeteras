package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.DocumentoSoporteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.service.VerificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/v1/verificaciones")
@RequiredArgsConstructor
public class VerificacionController {

    private final VerificacionService verificacionService;

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VerificacionPendienteDTO>> listarPendientes() {
        return ResponseEntity.ok(verificacionService.listarPendientes());
    }

    @PatchMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VerificacionResponseDTO> resolverVerificacion(
            @PathVariable Long usuarioId,
            @Valid @RequestBody DecisionVerificacionDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(verificacionService.resolverVerificacion(usuarioId, dto, userDetails.getUsername()));
    }

    @PostMapping(value = "/solicitar-revision", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('FUNDACION_REFUGIO', 'VETERINARIA')")
    public ResponseEntity<VerificacionResponseDTO> solicitarRevision(
            @RequestPart("documentoSoporte") MultipartFile documentoSoporte,
            @RequestParam(required = false) String nombreEntidad,
            @RequestParam(required = false) String nit,
            @RequestParam(required = false) String municipioOperacion,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(verificacionService.solicitarRevision(
                userDetails.getUsername(),
                documentoSoporte,
                nombreEntidad,
                nit,
                municipioOperacion));
    }

    @GetMapping("/{usuarioId}/documento-soporte")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> obtenerDocumentoSoporte(@PathVariable Long usuarioId) {
        DocumentoSoporteDTO documento = verificacionService.obtenerDocumentoSoporte(usuarioId);
        MediaType tipoContenido = MediaType.parseMediaType(documento.tipoContenido());
        return ResponseEntity.ok()
                .contentType(tipoContenido)
                .contentLength(documento.contenido().length)
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(documento.nombreArchivo(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(documento.contenido());
    }
}
