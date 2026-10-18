package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.notificacion.NotificacionDTO;
import co.edu.uniquindio.huellitas.entity.Notificacion;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.NotificacionRepository;
import co.edu.uniquindio.huellitas.service.NotificacionService;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionDTO> listarMisNotificaciones(String correo, Boolean soloNoLeidas, Pageable pageable) {
        String correoLimpio = correo.trim().toLowerCase();
        Page<Notificacion> page;
        if (Boolean.TRUE.equals(soloNoLeidas)) {
            page = notificacionRepository.findByUsuarioCorreoAndLeidaOrderByFechaDesc(correoLimpio, false, pageable);
        } else {
            page = notificacionRepository.findByUsuarioCorreoOrderByFechaDesc(correoLimpio, pageable);
        }

        return page.map(n -> new NotificacionDTO(
                n.getId(),
                n.getTipo() != null ? n.getTipo().name() : "GENERAL",
                n.getMensaje(),
                n.isLeida(),
                FechaUtils.formatear(n.getFecha()),
                n.getCaso() != null ? n.getCaso().getId() : null
        ));
    }

    @Override
    public NotificacionDTO marcarLeida(Long notificacionId, String correo) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", notificacionId));

        if (!notificacion.getUsuario().getCorreo().equalsIgnoreCase(correo.trim())) {
            throw new AccessDeniedException("No tiene permisos para modificar esta notificación");
        }

        notificacion.setLeida(true);
        Notificacion guardada = notificacionRepository.save(notificacion);

        return new NotificacionDTO(
                guardada.getId(),
                guardada.getTipo() != null ? guardada.getTipo().name() : "GENERAL",
                guardada.getMensaje(),
                guardada.isLeida(),
                FechaUtils.formatear(guardada.getFecha()),
                guardada.getCaso() != null ? guardada.getCaso().getId() : null
        );
    }
}
