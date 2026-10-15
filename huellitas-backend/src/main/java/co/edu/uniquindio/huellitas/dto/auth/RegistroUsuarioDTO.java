package co.edu.uniquindio.huellitas.dto.auth;

import co.edu.uniquindio.huellitas.entity.enums.Rol;
import jakarta.validation.constraints.*;

public record RegistroUsuarioDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @Email(message = "Debe ser un correo válido")
        @NotBlank(message = "El correo es obligatorio")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,

        @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Teléfono inválido")
        String telefono,

        Long barrioId,

        Rol rol
) {}
