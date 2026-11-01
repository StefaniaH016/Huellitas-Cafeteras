package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import co.edu.uniquindio.huellitas.entity.Notificacion;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {

    public NotificacionDTO toDTO(Notificacion n) {
        if (n == null) {
            return null;
        }
        return new NotificacionDTO(
                n.getId(),
                n.getTipo() != null ? n.getTipo().name() : "GENERAL",
                n.getMensaje(),
                n.isLeida(),
                FechaUtils.formatear(n.getFecha()),
                n.getCaso() != null ? n.getCaso().getId() : null
        );
    }
}

