package co.edu.uniquindio.huellitas.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Teléfono inválido")
        String telefono,

        Long barrioId
) {}
