package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.VerificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificacionServiceImpl implements VerificacionService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VerificacionPendienteDTO> listarPendientes() {
        List<Rol> rolesInstitucionales = List.of(Rol.FUNDACION_REFUGIO, Rol.VETERINARIA);
        List<Usuario> pendientes = usuarioRepository.findByVerificadoFalseAndRolIn(rolesInstitucionales);

        return pendientes.stream().map(u -> {
            String nombreEntidad = (u.getOrganizacion() != null && u.getOrganizacion().getNombre() != null)
                    ? u.getOrganizacion().getNombre()
                    : u.getNombre();
            String estado = (u.getOrganizacion() != null)
                    ? u.getOrganizacion().getEstadoVerificacion().name()
                    : "PENDIENTE";
            return new VerificacionPendienteDTO(u.getId(), nombreEntidad, u.getRol().name(), estado);
        }).toList();
    }

    @Override
    public VerificacionResponseDTO resolverVerificacion(Long usuarioId, DecisionVerificacionDTO dto, String correoAdmin) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        Usuario admin = usuarioRepository.findByCorreo(correoAdmin.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado con correo: " + correoAdmin));

        boolean aprobada = "APROBAR".equalsIgnoreCase(dto.decision());
        usuario.setVerificado(aprobada);

        Organizacion organizacion = usuario.getOrganizacion();
        if (organizacion != null) {
            organizacion.setEstadoVerificacion(aprobada ? EstadoVerificacion.VERIFICADA : EstadoVerificacion.RECHAZADA);
            organizacion.setFechaVerificacion(Instant.now());
            organizacion.setVerificador(admin);
        }

        usuarioRepository.save(usuario);

        String estadoRespuesta = aprobada ? "APROBADA" : "RECHAZADA";
        return new VerificacionResponseDTO(usuario.getId(), estadoRespuesta, usuario.isVerificado());
    }
}
