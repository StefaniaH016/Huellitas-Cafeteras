package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de correo electrónico.
 * Subtarea 13.7: Envía correos cuando se asume custodia, se cierra un caso o hay emergencia.
 * Los fallos en el envío de correo no afectan la operación principal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${MAIL_FROM:${spring.mail.username:${mail.username:noreply@huellitas.org}}}}")
    private String correoRemitente;

    private String getRemitente() {
        return (correoRemitente != null && !correoRemitente.isBlank()) ? correoRemitente : "noreply@huellitas.org";
    }

    @Override
    @Async
    public void enviarCorreoCustodia(Caso caso, Fundacion fundacion, String correoReportante) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(getRemitente());
            mensaje.setTo(correoReportante);
            mensaje.setSubject("🏠 Custodia asumida - Caso #" + caso.getId());
            mensaje.setText(String.format(
                    "Hola,%n%nNos complace informarte que la fundación '%s' ha asumido la custodia de tu caso #%d.%n%n" +
                    "Detalles del caso:%n" +
                    "- Animal: %s%n" +
                    "- Ubicación: %s%n%n" +
                    "Gracias por dar visibilidad a este animal. Te mantendremos informado sobre su evolución.%n%n" +
                    "Saludos,%nEquipo Huellitas",
                    fundacion.getNombre(),
                    caso.getId(),
                    caso.getAnimal() != null ? caso.getAnimal().getNombre() : "Sin nombre",
                    caso.getBarrio() != null ? caso.getBarrio().getNombre() : "No especificada"
            ));

            mailSender.send(mensaje);
            log.info("Correo de custodia enviado al reportante del caso {}", caso.getId());
        } catch (MailException e) {
            log.warn("Error al enviar correo de custodia para caso {}: {}", caso.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo de custodia para caso {}: {}", caso.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarCorreoCierre(Caso caso, String correoReportante, String resultado) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(getRemitente());
            mensaje.setTo(correoReportante);
            mensaje.setSubject("✅ Caso cerrado - #" + caso.getId());
            mensaje.setText(String.format(
                    "Hola,%n%nTu caso #%d ha sido cerrado.%n%n" +
                    "Resultado: %s%n%n" +
                    "Gracias por usar Huellitas. Esperamos que el animal haya encontrado un hogar seguro.%n%n" +
                    "Saludos,%nEquipo Huellitas",
                    caso.getId(),
                    resultado
            ));

            mailSender.send(mensaje);
            log.info("Correo de cierre enviado al reportante del caso {}", caso.getId());
        } catch (MailException e) {
            log.warn("Error al enviar correo de cierre para caso {}: {}", caso.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo de cierre para caso {}: {}", caso.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarCorreoEmergencia(Caso caso, String correoDestino) {
        try {
            String urgencia = caso.getUrgencia() != null ? caso.getUrgencia().name() : "ALTA";
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(getRemitente());
            mensaje.setTo(correoDestino);
            mensaje.setSubject("🚨 EMERGENCIA: Caso #" + caso.getId() + " - " + urgencia);
            mensaje.setText(String.format(
                    "Hola,%n%nSe ha registrado una emergencia en tu zona que requiere atención inmediata.%n%n" +
                    "Detalles del caso:%n" +
                    "- ID: %d%n" +
                    "- Urgencia: %s%n" +
                    "- Animal: %s%n" +
                    "- Especie: %s%n" +
                    "- Ubicación: %s%n" +
                    "- Descripción: %s%n%n" +
                    "Si puedes ayudar, por favor contacta al reportante o acercate al lugar.%n%n" +
                    "Gracias por tu disposición,%nEquipo Huellitas",
                    caso.getId(),
                    urgencia,
                    caso.getAnimal() != null ? caso.getAnimal().getNombre() : "Sin nombre",
                    caso.getAnimal() != null && caso.getAnimal().getEspecie() != null 
                        ? caso.getAnimal().getEspecie().name() : "No especificada",
                    caso.getBarrio() != null ? caso.getBarrio().getNombre() : "No especificada",
                    caso.getDescripcion() != null && caso.getDescripcion().length() > 100
                        ? caso.getDescripcion().substring(0, 100) + "..." : caso.getDescripcion()
            ));

            mailSender.send(mensaje);
            log.info("Correo de emergencia enviado a {} para caso {}", correoDestino, caso.getId());
        } catch (MailException e) {
            log.warn("Error al enviar correo de emergencia para caso {} a {}: {}", caso.getId(), correoDestino, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo de emergencia para caso {} a {}: {}", caso.getId(), correoDestino, e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarCorreoRecordatorio(Caso caso, String correoReportante, long diasInactividad) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(getRemitente());
            mensaje.setTo(correoReportante);
            mensaje.setSubject("🔔 Recordatorio - Caso #" + caso.getId() + " sin actividad");
            mensaje.setText(String.format(
                    "Hola,%n%nTu caso #%d lleva %d días sin actividad.%n%n" +
                    "Detalles del caso:%n" +
                    "- Animal: %s%n" +
                    "- Estado actual: %s%n" +
                    "- Ubicación: %s%n%n" +
                    "¿El caso aún requiere atención? Si la situación cambió o el animal ya fue rescatado, " +
                    "por favor actualiza el estado del caso en la plataforma.%n%n" +
                    "Si no recibes respuesta en los próximos días, el caso será revisado por un administrador.%n%n" +
                    "Saludos,%nEquipo Huellitas",
                    caso.getId(),
                    diasInactividad,
                    caso.getAnimal() != null ? caso.getAnimal().getNombre() : "Sin nombre",
                    caso.getEstado() != null ? caso.getEstado().name() : "REGISTRADO",
                    caso.getBarrio() != null ? caso.getBarrio().getNombre() : "No especificada"
            ));

            mailSender.send(mensaje);
            log.info("Correo de recordatorio enviado al reportante del caso {} ({} días de inactividad)", 
                    caso.getId(), diasInactividad);
        } catch (MailException e) {
            log.warn("Error al enviar correo de recordatorio para caso {}: {}", caso.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo de recordatorio para caso {}: {}", caso.getId(), e.getMessage());
        }
    }
}