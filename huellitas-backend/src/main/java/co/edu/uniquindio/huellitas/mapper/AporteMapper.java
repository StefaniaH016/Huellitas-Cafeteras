package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.reporte.AporteDetalleDTO;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AporteMapper {

    public AporteResponseDTO toResponseDTO(Seguimiento s, Long casoId) {
        if (s == null) {
            return null;
        }
        return new AporteResponseDTO(
                s.getId(),
                casoId,
                s.getAutor() != null ? s.getAutor().getId() : null,
                s.getComentario(),
                s.getUbicacionTexto(),
                extraerFotografias(s),
                FechaUtils.formatear(s.getFecha())
        );
    }

    public AporteResponseDTO toResponseDTO(
            Seguimiento guardado,
            Long casoId,
            Long autorId,
            String ubicacionTexto,
            List<String> fotosGuardadas) {
        if (guardado == null) {
            return null;
        }
        return new AporteResponseDTO(
                guardado.getId(),
                casoId,
                autorId,
                guardado.getComentario(),
                ubicacionTexto,
                fotosGuardadas != null ? fotosGuardadas : Collections.emptyList(),
                FechaUtils.formatear(guardado.getFecha())
        );
    }

    public AporteDetalleDTO toDetalleDTO(Seguimiento s) {
        if (s == null) {
            return null;
        }
        return AporteDetalleDTO.builder()
                .id(s.getId())
                .tipo(s.getTipo())
                .comentario(s.getComentario())
                .fecha(FechaUtils.formatear(s.getFecha()))
                .autorNombre(s.getAutor() != null ? s.getAutor().getNombre() : null)
                .zona(extraerZona(s))
                .ubicacionTexto(s.getUbicacionTexto())
                .fotografias(extraerFotografias(s))
                .build();
    }

    public List<AporteDetalleDTO> toDetalleDTOList(List<Seguimiento> seguimientos) {
        if (seguimientos == null) {
            return List.of();
        }
        return seguimientos.stream().map(this::toDetalleDTO).toList();
    }

    private List<String> extraerFotografias(Seguimiento seguimiento) {
        if (seguimiento.getFotografias() == null) {
            return Collections.emptyList();
        }
        return seguimiento.getFotografias().stream()
                .map(foto -> foto.getUrl())
                .toList();
    }

    private String extraerZona(Seguimiento seguimiento) {
        if (seguimiento.getBarrio() == null) {
            return null;
        }
        String barrio = seguimiento.getBarrio().getNombre();
        if (seguimiento.getBarrio().getMunicipio() == null) {
            return barrio;
        }
        return barrio + " · " + seguimiento.getBarrio().getMunicipio().getNombre();
    }
}
