package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class VerificacionMapper {

    public VerificacionPendienteDTO toPendienteDTO(Usuario u) {
        if (u == null) {
            return null;
        }
        String nombreEntidad = (u.getOrganizacion() != null && u.getOrganizacion().getNombre() != null)
                ? u.getOrganizacion().getNombre()
                : u.getNombre();
        String estado = (u.getOrganizacion() != null)
                ? u.getOrganizacion().getEstadoVerificacion().name()
                : "PENDIENTE";
        return new VerificacionPendienteDTO(u.getId(), nombreEntidad, u.getRol().name(), estado);
    }

    public VerificacionResponseDTO toResponseDTO(Usuario usuario, boolean aprobada) {
        if (usuario == null) {
            return null;
        }
        String estadoRespuesta = aprobada ? "APROBADA" : "RECHAZADA";
        return new VerificacionResponseDTO(usuario.getId(), estadoRespuesta, usuario.isVerificado());
    }
}

