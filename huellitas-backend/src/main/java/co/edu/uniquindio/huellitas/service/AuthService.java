package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.auth.AuthResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.auth.UsuarioDTO;

public interface AuthService {
    RegistroResponseDTO registro(RegistroUsuarioDTO dto);
    AuthResponseDTO login(LoginRequestDTO dto);
    UsuarioDTO obtenerUsuarioActual(String correo);
}
