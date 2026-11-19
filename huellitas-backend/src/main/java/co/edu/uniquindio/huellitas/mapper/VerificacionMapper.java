package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class VerificacionMapper {

    public VerificacionPendienteDTO toPendienteDTO(Usuario u) {
        if (u == null) {
            return null;
        }
        Organizacion organizacion = u.getOrganizacion() != null
                ? (Organizacion) Hibernate.unproxy(u.getOrganizacion())
                : null;
        String nombreEntidad = (organizacion != null && organizacion.getNombre() != null)
                ? organizacion.getNombre()
                : u.getNombre();
        String estado = (organizacion != null)
                ? organizacion.getEstadoVerificacion().name()
                : "PENDIENTE";
        return new VerificacionPendienteDTO(
                u.getId(),
                nombreEntidad,
                u.getRol().name(),
                estado,
                organizacion != null ? organizacion.getNit() : null,
                organizacion != null && organizacion.getMunicipio() != null
                        ? organizacion.getMunicipio().getNombre()
                        : null,
                organizacion != null ? organizacion.getDocumentoSoporteNombre() : null,
                organizacion != null && organizacion.getDocumentoSoporteNombre() != null
        );
    }

    public VerificacionResponseDTO toResponseDTO(Usuario usuario, boolean aprobada) {
        if (usuario == null) {
            return null;
        }
        String estadoRespuesta = aprobada ? "VERIFICADA" : "RECHAZADA";
        if (usuario.getOrganizacion() != null) {
            estadoRespuesta = ((Organizacion) Hibernate.unproxy(usuario.getOrganizacion()))
                    .getEstadoVerificacion().name();
        }
        return new VerificacionResponseDTO(usuario.getId(), estadoRespuesta, usuario.isVerificado());
    }
}
