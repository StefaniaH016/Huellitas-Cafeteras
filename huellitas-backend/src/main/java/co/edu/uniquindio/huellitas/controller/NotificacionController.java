package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import co.edu.uniquindio.huellitas.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/v1/notificaciones", "/api/v1/notificaciones"})
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "API para gestión de notificaciones del usuario")
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

    /**
     * 13.8: Contador de notificaciones no leídas del usuario autenticado.
     */
    @GetMapping("/no-leidas/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Contar notificaciones no leídas", description = "Retorna la cantidad de notificaciones no leídas del usuario autenticado")
    public ResponseEntity<Long> contarNoLeidas(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificacionService.contarNoLeidas(userDetails.getUsername());
        return ResponseEntity.ok(count);
    }
}
