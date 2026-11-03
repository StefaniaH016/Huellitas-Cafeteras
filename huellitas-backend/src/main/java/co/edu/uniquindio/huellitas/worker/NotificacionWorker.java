package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Notificacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoNotificacion;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.repository.NotificacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Worker W-01: Notificaciones y Alertas.
 *
 * <p>Envía avisos automáticos ante eventos clave del sistema:
 * <ul>
 *   <li>Registro de caso con urgencia ALTA o CRÍTICA → notifica al reportante.</li>
 *   <li>Cambio de estado del caso → notifica al reportante.</li>
 *   <li>Cierre de caso → notifica al reportante.</li>
 * </ul>
 * Todos los métodos son {@code @Async} para no bloquear la petición HTTP principal.
 * Las notificaciones se almacenan en PostgreSQL y se entregan por la API REST.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionWorker {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Notifica al reportante cuando registra un caso urgente (ALTA o CRÍTICA).
     */
    @Async
    public void notificarCasoUrgente(Caso caso) {
        if (caso.getUrgencia() != Urgencia.ALTA && caso.getUrgencia() != Urgencia.CRITICA) {
            return;
        }
        String mensaje = String.format(
                "⚠️ Tu reporte #%d (%s) fue registrado con urgencia %s y ya está visible para rescatistas.",
                caso.getId(),
                caso.getAnimal() != null ? caso.getAnimal().getNombre() : "Sin nombre",
                caso.getUrgencia()
        );
        guardar(caso.getReportante(), caso, TipoNotificacion.EMERGENCIA, mensaje);
    }

    /**
     * Notifica al reportante cuando el estado de su caso cambia.
     */
    @Async
    public void notificarCambioEstado(Caso caso, EstadoCaso estadoAnterior, EstadoCaso estadoNuevo, Usuario actor) {
        if (caso.getReportante() == null) return;

        String mensaje = String.format(
                "📋 Tu caso #%d cambió de estado: %s → %s.",
                caso.getId(), estadoAnterior, estadoNuevo
        );
        guardar(caso.getReportante(), caso, TipoNotificacion.CAMBIO_ESTADO, mensaje);

        // Si el actor es distinto del reportante, también notifica al actor
        if (actor != null && !actor.getId().equals(caso.getReportante().getId())) {
            String mensajeActor = String.format(
                    "📋 El estado del caso #%d fue actualizado a %s.",
                    caso.getId(), estadoNuevo
            );
            guardar(actor, caso, TipoNotificacion.CAMBIO_ESTADO, mensajeActor);
        }
    }

    /**
     * Notifica al reportante cuando su caso es cerrado/resuelto.
     */
    @Async
    public void notificarCierreCaso(Caso caso, String resultado) {
        if (caso.getReportante() == null) return;

        String mensaje = String.format(
                "✅ Tu caso #%d ha sido cerrado. Resultado: %s.",
                caso.getId(), resultado
        );
        guardar(caso.getReportante(), caso, TipoNotificacion.CIERRE, mensaje);
    }

    /**
     * Notifica al reportante que su caso lleva tiempo sin actividad.
     */
    @Async
    public void notificarRecordatorio(Caso caso) {
        if (caso.getReportante() == null) return;

        String mensaje = String.format(
                "🔔 Tu caso #%d lleva varios días sin actividad. ¿Aún requiere atención?",
                caso.getId()
        );
        guardar(caso.getReportante(), caso, TipoNotificacion.RECORDATORIO, mensaje);
    }

    // ── helper interno ────────────────────────────────────────────

    private void guardar(Usuario destinatario, Caso caso, TipoNotificacion tipo, String mensaje) {
        try {
            Notificacion notificacion = Notificacion.builder()
                    .usuario(destinatario)
                    .caso(caso)
                    .tipo(tipo)
                    .mensaje(mensaje)
                    .leida(false)
                    .fecha(Instant.now())
                    .build();
            notificacionRepository.save(notificacion);
            log.info("Notificación [{}] enviada al usuario {} para el caso {}",
                    tipo, destinatario.getCorreo(), caso.getId());
        } catch (Exception e) {
            log.error("Error al guardar notificación [{}] para el usuario {}: {}",
                    tipo, destinatario.getCorreo(), e.getMessage());
        }
    }
}
