package co.edu.uniquindio.huellitas.dto.auth;

public record AuthResponseDTO(
        String token,
        String tipo,
        UsuarioDTO usuario
) {
    public static AuthResponseDTO bearer(String token, UsuarioDTO usuario) {
        return new AuthResponseDTO(token, "Bearer", usuario);
    }
}
