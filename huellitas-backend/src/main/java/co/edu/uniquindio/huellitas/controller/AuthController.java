package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.auth.AuthResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    // TODO: Inyectar AuthService cuando Dev A lo implemente

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDTO> registro(
            @Valid @RequestBody RegistroUsuarioDTO dto) {
        // TODO: implementar lógica de registro
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        // TODO: implementar lógica de login
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
