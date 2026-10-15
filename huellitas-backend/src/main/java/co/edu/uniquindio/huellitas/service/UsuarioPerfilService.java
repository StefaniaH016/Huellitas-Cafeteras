package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.usuario.ActualizarPerfilDTO;
import co.edu.uniquindio.huellitas.dto.usuario.UsuarioPerfilDTO;

public interface UsuarioPerfilService {
    UsuarioPerfilDTO obtenerPerfil(String correo);
    UsuarioPerfilDTO actualizarPerfil(String correo, ActualizarPerfilDTO dto);
}
