package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Notificacion;
import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoNotificacion;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.NotificacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final MunicipioRepository municipioRepository;
    private final EmailService emailService;

    /**
     * Notifica al reportante cuando registra un caso urgente (ALTA o CRÍTICA).
     */
    @Async
    @Transactional
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

    /**
     * Notifica al reportante cuando una fundación asume la custodia de su caso.
     * Subtarea 13.3.
     * 13.7: También envía correo electrónico.
     */
    @Async
    public void notificarAsuncionCustodia(Caso caso, Fundacion fundacion) {
        if (caso.getReportante() == null) return;

        String mensaje = String.format(
                "🏠 La fundación '%s' ha asumido la custodia de tu caso #%d. ¡Gracias por dar visibilidad a este animal!",
                fundacion.getNombre(),
                caso.getId()
        );
        guardar(caso.getReportante(), caso, TipoNotificacion.CUSTODIA, mensaje);

        // 13.7: Enviar correo al reportante
        try {
            emailService.enviarCorreoCustodia(caso, fundacion, caso.getReportante().getCorreo());
        } catch (Exception e) {
            log.warn("Error al enviar correo de custodia: {}", e.getMessage());
        }
    }

    @Async
    @Transactional
    public void notificarVerificacionOrganizacion(Usuario usuario, boolean aprobada) {
        if (usuario == null) {
            return;
        }
        String mensaje = aprobada
                ? "Tu organización fue verificada. La fundación ya puede asumir casos hasta su capacidad habilitada."
                : "La verificación de tu organización fue rechazada. Comunícate con soporte para conocer los pasos a seguir.";
        guardar(usuario, null, TipoNotificacion.VERIFICACION, mensaje);
    }

    /**
     * 13.5: Notifica a los involucrados (excepto el autor) cuando se crea un aporte.
     * Incluye al reportante y a la fundación si tiene custodia.
     */
    @Async
    public void notificarAporte(Seguimiento aporte) {
        if (aporte.getAutor() == null || aporte.getCaso() == null) return;

        Caso caso = aporte.getCaso();
        Long autorId = aporte.getAutor().getId();

        // Notificar al reportante (si no es el autor)
        if (caso.getReportante() != null && !caso.getReportante().getId().equals(autorId)) {
            String mensaje = String.format(
                    "💬 Nuevo comentario en tu caso #%d: \"%s\"",
                    caso.getId(),
                    aporte.getComentario() != null && aporte.getComentario().length() > 50
                        ? aporte.getComentario().substring(0, 50) + "..." : aporte.getComentario()
            );
            guardar(caso.getReportante(), caso, TipoNotificacion.CAMBIO_ESTADO, mensaje);
        }

        // Notificar a la fundación custodia (si no es el autor)
        if (caso.getFundacionCustodio() != null) {
            // Obtener el usuario de la fundación
            usuarioRepository.findByOrganizacionId(caso.getFundacionCustodio().getId())
                    .ifPresent(fundacionUsuario -> {
                        if (!fundacionUsuario.getId().equals(autorId)) {
                            String mensaje = String.format(
                                    "💬 Nuevo aporte en el caso #%d bajo tu custodia",
                                    caso.getId()
                            );
                            guardar(fundacionUsuario, caso, TipoNotificacion.CAMBIO_ESTADO, mensaje);
                        }
                    });
        }
    }

    /**
     * 13.6: Notifica emergencias (ALTA o CRITICA) a usuarios del mismo municipio
     * que tengan disponibleRescate = true.
     * 13.7: También envía correo electrónico a los usuarios encontrados.
     */
    @Async
    @Transactional
    public void notificarEmergenciaARescuatistas(Caso caso) {
        if (caso.getUrgencia() != Urgencia.ALTA && caso.getUrgencia() != Urgencia.CRITICA) {
            return;
        }
        if (caso.getBarrio() == null || caso.getBarrio().getMunicipio() == null) {
            log.warn("El caso {} no tiene municipio definido para notificar emergencias", caso.getId());
            return;
        }

        Long municipioId = caso.getBarrio().getMunicipio().getId();

        // Buscar usuarios del mismo municipio con disponibleRescate = true
        List<Usuario> rescuistas = usuarioRepository
                .findByMunicipioInteresIdAndDisponibleRescateTrue(municipioId);

        for (Usuario rescuista : rescuistas) {
            String mensaje = String.format(
                    "🚨 EMERGENCIA en tu zona: Caso #%d - %s. ¿Puedes ayudar?",
                    caso.getId(),
                    caso.getUrgencia()
            );
            guardar(rescuista, caso, TipoNotificacion.EMERGENCIA, mensaje);

            // 13.7: Enviar correo de emergencia
            try {
                emailService.enviarCorreoEmergencia(caso, rescuista.getCorreo());
            } catch (Exception e) {
                log.warn("Error al enviar correo de emergencia a {}: {}", rescuista.getCorreo(), e.getMessage());
            }
        }

    }

    /**
     * 14.6: Disparar notificación ante una nueva evolución médica registrada.
     * Notifica al reportante y a la fundación custodia (si no son el firmante).
     */
    @Async
    public void notificarEvolucionMedica(BitacoraMedica bitacora, RegistroMedico evolucion, Usuario firmante) {
        if (bitacora == null || bitacora.getCaso() == null) return;
        Caso caso = bitacora.getCaso();
        Long firmanteId = firmante != null ? firmante.getId() : null;

        String diagnostico = (evolucion != null && evolucion.getDiagnostico() != null)
                ? (evolucion.getDiagnostico().length() > 50
                    ? evolucion.getDiagnostico().substring(0, 50) + "..."
                    : evolucion.getDiagnostico())
                : "Nueva evolución clínica";

        String mensajeReportante = String.format(
                "🩺 Nueva evolución médica registrada en tu caso #%d: %s",
                caso.getId(), diagnostico
        );

        if (caso.getReportante() != null && !caso.getReportante().getId().equals(firmanteId)) {
            guardar(caso.getReportante(), caso, TipoNotificacion.EVOLUCION_MEDICA, mensajeReportante);
        }

        if (caso.getFundacionCustodio() != null) {
            usuarioRepository.findByOrganizacionId(caso.getFundacionCustodio().getId())
                    .ifPresent(fundacionUsuario -> {
                        if (!fundacionUsuario.getId().equals(firmanteId)) {
                            String mensajeFundacion = String.format(
                                    "🩺 Nueva evolución médica en el caso #%d bajo tu custodia: %s",
                                    caso.getId(), diagnostico
                            );
                            guardar(fundacionUsuario, caso, TipoNotificacion.EVOLUCION_MEDICA, mensajeFundacion);
                        }
                    });
        }
    }

    /**
     * 14.6: Disparar notificación ante el registro de un alta médica.
     * Notifica al reportante y a la fundación custodia (si no son el firmante).
     */
    @Async
    public void notificarAltaMedica(BitacoraMedica bitacora, Usuario firmante) {
        if (bitacora == null || bitacora.getCaso() == null) return;
        Caso caso = bitacora.getCaso();
        Long firmanteId = firmante != null ? firmante.getId() : null;

        String mensaje = String.format(
                "🎉 ¡Alta médica emitida para el caso #%d (%s)! El animal ha completado su atención clínica.",
                caso.getId(),
                caso.getAnimal() != null ? caso.getAnimal().getNombre() : "Sin nombre"
        );

        if (caso.getReportante() != null && !caso.getReportante().getId().equals(firmanteId)) {
            guardar(caso.getReportante(), caso, TipoNotificacion.ALTA_MEDICA, mensaje);
        }

        if (caso.getFundacionCustodio() != null) {
            usuarioRepository.findByOrganizacionId(caso.getFundacionCustodio().getId())
                    .ifPresent(fundacionUsuario -> {
                        if (!fundacionUsuario.getId().equals(firmanteId)) {
                            guardar(fundacionUsuario, caso, TipoNotificacion.ALTA_MEDICA, mensaje);
                        }
                    });
        }
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
                    tipo, destinatario.getCorreo(), caso != null ? caso.getId() : "sin caso");
        } catch (Exception e) {
            log.error("Error al guardar notificación [{}] para el usuario {}: {}",
                    tipo, destinatario.getCorreo(), e.getMessage());
        }
    }
}
