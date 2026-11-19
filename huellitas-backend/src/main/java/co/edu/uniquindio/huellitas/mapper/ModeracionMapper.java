package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ModeracionMapper {

    public CasoModeracionDTO toCasoModeracionDTO(Caso c) {
        if (c == null) {
            return null;
        }
        return new CasoModeracionDTO(
                c.getId(),
                c.getMotivoModeracion() != null ? c.getMotivoModeracion() : "Reportado para revisión",
                c.isModeracionPendiente() ? "PENDIENTE" : "OCULTADO",
                FechaUtils.formatear(c.getFechaRegistro())
        );
    }

    public ModeracionResponseDTO toModeracionResponseDTO(Long casoId, boolean mantener) {
        return new ModeracionResponseDTO(
                casoId,
                mantener ? "MANTENIDO" : "OCULTADO",
                FechaUtils.formatear(Instant.now())
        );
    }
}
