package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.auth.AuthResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.auth.UsuarioDTO;
import co.edu.uniquindio.huellitas.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponseDTO> registro(
            @Valid @RequestBody RegistroUsuarioDTO dto) {
        RegistroResponseDTO response = authService.registro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioDTO usuario = authService.obtenerUsuarioActual(userDetails.getUsername());
        return ResponseEntity.ok(usuario);
    }
}
