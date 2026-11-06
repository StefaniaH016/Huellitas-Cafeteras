package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.VerificacionMapper;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.VerificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificacionServiceImpl implements VerificacionService {

    private final UsuarioRepository usuarioRepository;
    private final VerificacionMapper verificacionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VerificacionPendienteDTO> listarPendientes() {
        List<Rol> rolesInstitucionales = List.of(Rol.FUNDACION_REFUGIO, Rol.VETERINARIA);
        List<Usuario> pendientes = usuarioRepository.findByVerificadoFalseAndRolIn(rolesInstitucionales);

        return pendientes.stream()
                .filter(u -> u.getOrganizacion() == null
                        || u.getOrganizacion().getEstadoVerificacion() == EstadoVerificacion.PENDIENTE)
                .map(verificacionMapper::toPendienteDTO)
                .toList();
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
        if (organizacion == null) {
            if (usuario.getRol() == Rol.FUNDACION_REFUGIO) {
                organizacion = Fundacion.builder()
                        .nombre(usuario.getNombre())
                        .nit("NIT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                        .estadoVerificacion(aprobada ? EstadoVerificacion.VERIFICADA : EstadoVerificacion.RECHAZADA)
                        .aceptaCustodia(true)
                        .capacidadAlbergue(50)
                        .fechaVerificacion(Instant.now())
                        .verificador(admin)
                        .municipio(usuario.getMunicipioInteres())
                        .build();
                usuario.setOrganizacion(organizacion);
            } else if (usuario.getRol() == Rol.VETERINARIA) {
                organizacion = Veterinaria.builder()
                        .nombre(usuario.getNombre())
                        .nit("NIT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                        .estadoVerificacion(aprobada ? EstadoVerificacion.VERIFICADA : EstadoVerificacion.RECHAZADA)
                        .fechaVerificacion(Instant.now())
                        .verificador(admin)
                        .municipio(usuario.getMunicipioInteres())
                        .build();
                usuario.setOrganizacion(organizacion);
            }
        } else {
            organizacion.setEstadoVerificacion(aprobada ? EstadoVerificacion.VERIFICADA : EstadoVerificacion.RECHAZADA);
            organizacion.setFechaVerificacion(Instant.now());
            organizacion.setVerificador(admin);
        }

        usuarioRepository.save(usuario);

        return verificacionMapper.toResponseDTO(usuario, aprobada);
    }
}
