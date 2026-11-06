package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.auth.RegistroResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.UsuarioDTO;
import co.edu.uniquindio.huellitas.dto.usuario.UsuarioPerfilDTO;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public RegistroResponseDTO toRegistroResponseDTO(Usuario guardado) {
        if (guardado == null) {
            return null;
        }
        return new RegistroResponseDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getCorreo(),
                guardado.getRol(),
                guardado.isVerificado()
        );
    }

    public UsuarioDTO toUsuarioDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isVerificado()
        );
    }

    public UsuarioPerfilDTO toPerfilDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        Long barrioId = null;
        Long municipioId = null;
        Long departamentoId = null;
        Organizacion organizacion = usuario.getOrganizacion() != null
                ? (Organizacion) Hibernate.unproxy(usuario.getOrganizacion())
                : null;

        if (usuario.getBarrio() != null) {
            barrioId = usuario.getBarrio().getId();
            if (usuario.getBarrio().getMunicipio() != null) {
                municipioId = usuario.getBarrio().getMunicipio().getId();
                if (usuario.getBarrio().getMunicipio().getDepartamento() != null) {
                    departamentoId = usuario.getBarrio().getMunicipio().getDepartamento().getId();
                }
            }
        } else if (usuario.getMunicipioInteres() != null) {
            municipioId = usuario.getMunicipioInteres().getId();
            if (usuario.getMunicipioInteres().getDepartamento() != null) {
                departamentoId = usuario.getMunicipioInteres().getDepartamento().getId();
            }
        } else if (organizacion != null && organizacion.getMunicipio() != null) {
            municipioId = organizacion.getMunicipio().getId();
            if (organizacion.getMunicipio().getDepartamento() != null) {
                departamentoId = organizacion.getMunicipio().getDepartamento().getId();
            }
        }

        return UsuarioPerfilDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .verificado(usuario.isVerificado())
                .departamentoId(departamentoId)
                .municipioId(municipioId)
                .barrioId(barrioId)
                .organizacionId(organizacion != null ? organizacion.getId() : null)
                .organizacionNombre(organizacion != null ? organizacion.getNombre() : null)
                .estadoVerificacionOrganizacion(organizacion != null
                        ? organizacion.getEstadoVerificacion().name()
                        : null)
                .aceptaCustodia(organizacion instanceof Fundacion fundacion
                        ? fundacion.isAceptaCustodia()
                        : null)
                .capacidadAlbergue(organizacion instanceof Fundacion fundacion
                        ? fundacion.getCapacidadAlbergue()
                        : null)
                .build();
    }
}
