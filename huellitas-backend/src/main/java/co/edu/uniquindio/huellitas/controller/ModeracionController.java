package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.service.ModeracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/moderacion")
@RequiredArgsConstructor
public class ModeracionController {

    private final ModeracionService moderacionService;

    @GetMapping("/casos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CasoModeracionDTO>> listarCasosReportados() {
        return ResponseEntity.ok(moderacionService.listarCasosReportados());
    }

    @PatchMapping("/casos/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ModeracionResponseDTO> moderarCaso(
            @PathVariable Long id,
            @Valid @RequestBody DecisionModeracionDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(moderacionService.moderarCaso(id, dto, userDetails.getUsername()));
    }
}
