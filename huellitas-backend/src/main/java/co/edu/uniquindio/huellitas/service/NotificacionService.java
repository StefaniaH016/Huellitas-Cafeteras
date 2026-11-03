package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificacionService {
    Page<NotificacionDTO> listarMisNotificaciones(String correo, Boolean soloNoLeidas, Pageable pageable);
    NotificacionDTO marcarLeida(Long notificacionId, String correo);

    /**
     * 13.8: Cuenta las notificaciones no leídas del usuario.
     */
    long contarNoLeidas(String correo);
}
