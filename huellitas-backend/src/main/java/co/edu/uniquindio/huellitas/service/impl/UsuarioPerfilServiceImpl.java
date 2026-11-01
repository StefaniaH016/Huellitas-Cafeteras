package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.usuario.ActualizarPerfilDTO;
import co.edu.uniquindio.huellitas.dto.usuario.UsuarioPerfilDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.UsuarioMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.UsuarioPerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioPerfilServiceImpl implements UsuarioPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final BarrioRepository barrioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional(readOnly = true)
    public UsuarioPerfilDTO obtenerPerfil(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        return usuarioMapper.toPerfilDTO(usuario);
    }

    @Override
    public UsuarioPerfilDTO actualizarPerfil(String correo, ActualizarPerfilDTO dto) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            usuario.setNombre(dto.nombre().trim());
        }

        if (dto.telefono() != null) {
            usuario.setTelefono(dto.telefono().trim());
        }

        if (dto.barrioId() != null) {
            Barrio barrio = barrioRepository.findById(dto.barrioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barrio", dto.barrioId()));
            usuario.setBarrio(barrio);
            usuario.setMunicipioInteres(barrio.getMunicipio());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toPerfilDTO(actualizado);
    }
}
