package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import co.edu.uniquindio.huellitas.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificacionDTO>> listarNotificaciones(
            @RequestParam(required = false) Boolean leidas,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Boolean soloNoLeidas = (leidas != null && !leidas) ? Boolean.TRUE : null;
        Page<NotificacionDTO> list = notificacionService.listarMisNotificaciones(userDetails.getUsername(), soloNoLeidas, pageable);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/leer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionDTO> marcarLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id, userDetails.getUsername()));
    }
}
