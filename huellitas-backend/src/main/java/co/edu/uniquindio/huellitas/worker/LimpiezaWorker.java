package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Worker W-04: Limpieza y Cierre de Casos.
 *
 * <p>Tarea programada que se ejecuta automáticamente según la expresión cron configurada.
 * Su objetivo es mantener el listado principal limpio y sin información obsoleta:
 *
 * <ul>
 *   <li>Archiva automáticamente los casos en estado RESUELTO que lleven más de
 *       N días sin actividad (configurable), retirándolos del feed activo.</li>
 *   <li>Envía recordatorios (vía {@link NotificacionWorker}) al reportante de
 *       casos que llevan M días sin actividad para que confirmen
 *       si aún requieren atención (configurable).</li>
 * </ul>
 *
 * <p>Usa {@code @Scheduled} habilitado por {@code @EnableScheduling} en
 * {@code HuellitasApplication}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LimpiezaWorker {

    private final CasoRepository casoRepository;
    private final NotificacionWorker notificacionWorker;
    private final Clock clock;

    @Value("${limpieza.dias-archivar-resuelto}")
    private long diasArchivarResuelto;

    @Value("${limpieza.dias-recordatorio-inactividad}")
    private long diasRecordatorioInactividad;

    /**
     * Ejecución programada según cron configurado.
     * Revisa todos los casos y aplica las reglas de limpieza.
     */
    @Scheduled(cron = "${limpieza.cron}")
    @Transactional
    public void ejecutar() {
        log.info("═════════════════════════════════════════════════════════════");
        log.info("W-04 LimpiezaWorker: INICIO de revisión de casos");
        log.info("═════════════════════════════════════════════════════════════");

        Instant inicioEjecucion = clock.instant();
        Instant ahora = clock.instant();
        Instant limiteResuelto = ahora.minus(diasArchivarResuelto, ChronoUnit.DAYS);
        Instant limiteRecordatorio = ahora.minus(diasRecordatorioInactividad, ChronoUnit.DAYS);
        Instant limiteRevisionAdmin = ahora.minus(diasRecordatorioInactividad + 15, ChronoUnit.DAYS);

        log.info("Configuración actual:");
        log.info("  - Días para archivar RESUELTOS: {}", diasArchivarResuelto);
        log.info("  - Días para recordatorio: {}", diasRecordatorioInactividad);
        log.info("  - Días para revisión admin: {}", diasRecordatorioInactividad + 15);

        List<Caso> todos = casoRepository.findAll();
        log.info("Total de casos a revisar: {}", todos.size());

        int archivados = 0;
        int recordatorios = 0;
        int marcadosRevision = 0;
        StringBuilder casosArchivadosLog = new StringBuilder();
        StringBuilder casosRecordatorioLog = new StringBuilder();
        StringBuilder casosRevisionLog = new StringBuilder();

        for (Caso caso : todos) {
            try {
                Instant ultimaActividad = caso.getFechaUltimaActividad() != null
                        ? caso.getFechaUltimaActividad()
                        : caso.getFechaRegistro();

                EstadoCaso estado = caso.getEstado();

                // ── Archivar casos RESUELTOS sin actividad reciente ───────
                if (estado == EstadoCaso.RESUELTO && ultimaActividad.isBefore(limiteResuelto)) {
                    long diasInactivo = ChronoUnit.DAYS.between(ultimaActividad, ahora);
                    archivar(caso, ahora,
                            "Archivado automáticamente tras " + diasArchivarResuelto
                                    + " días en estado RESUELTO sin actividad.");
                    archivados++;
                    casosArchivadosLog.append(String.format("%n    Caso #%d (RESUELTO → ARCHIVADO) - %d días sin actividad",
                            caso.getId(), diasInactivo));
                    continue;
                }

                // ── 17.3: Recordatorio por inactividad en casos activos ────────────────
                if ((estado == EstadoCaso.REGISTRADO || estado == EstadoCaso.EN_ATENCION)
                        && caso.getReportante() != null) {

                    long diasInactividad = ChronoUnit.DAYS.between(ultimaActividad, ahora);

                    // Marcar para revisión administrativa a M+15 días (45 días)
                    if (ultimaActividad.isBefore(limiteRevisionAdmin) && !caso.isModeracionPendiente()) {
                        caso.setModeracionPendiente(true);
                        casoRepository.save(caso);
                        marcadosRevision++;
                        casosRevisionLog.append(String.format("%n    Caso #%d (estado: %s) - %d días sin actividad - Reportante: %s",
                                caso.getId(), estado, diasInactividad, caso.getReportante().getCorreo()));
                        log.info("W-04 Caso #{} marcado para revisión administrativa tras {} días sin actividad",
                                caso.getId(), diasInactividad);
                    }

                    // Enviar recordatorio a los M días (30 días) sin duplicados
                    if (ultimaActividad.isBefore(limiteRecordatorio)) {
                        // Evitar recordatorios duplicados: solo enviar si nunca se envió
                        // o si han pasado al menos 7 días desde el último recordatorio
                        boolean debeEnviarRecordatorio = caso.getFechaUltimoRecordatorio() == null
                                || caso.getFechaUltimoRecordatorio().isBefore(ahora.minus(7, ChronoUnit.DAYS));

                        if (debeEnviarRecordatorio) {
                            notificacionWorker.notificarRecordatorio(caso, diasInactividad);
                            caso.setFechaUltimoRecordatorio(ahora);
                            casoRepository.save(caso);
                            recordatorios++;
                            casosRecordatorioLog.append(String.format("%n    Caso #%d (estado: %s) - %d días sin actividad - Reportante: %s",
                                    caso.getId(), estado, diasInactividad, caso.getReportante().getCorreo()));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("W-04 Error al procesar caso #{}: {}", caso.getId(), e.getMessage(), e);
            }
        }

        // ── Log consolidado de resultados ────────────────────────────────────────
        log.info("─────────────────────────────────────────────────────────────");
        log.info("W-04 RESUMEN DE EJECUCIÓN:");
        log.info("  ✓ Casos archivados: {}", archivados);
        if (archivados > 0) {
            log.info("    Detalle:{}", casosArchivadosLog);
        }

        log.info("  ✓ Recordatorios enviados: {}", recordatorios);
        if (recordatorios > 0) {
            log.info("    Detalle:{}", casosRecordatorioLog);
        }

        log.info("  ✓ Casos marcados para revisión administrativa: {}", marcadosRevision);
        if (marcadosRevision > 0) {
            log.info("    Detalle:{}", casosRevisionLog);
        }

        long duracionMs = ChronoUnit.MILLIS.between(inicioEjecucion, clock.instant());
        log.info("  Duración total: {} ms", duracionMs);
        log.info("═════════════════════════════════════════════════════════════");
        log.info("W-04 LimpiezaWorker: FIN de ejecución exitosa");
        log.info("═════════════════════════════════════════════════════════════");
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private void archivar(Caso caso, Instant ahora, String motivo) {
        caso.setEstado(EstadoCaso.ARCHIVADO);
        caso.setFechaArchivado(ahora);
        caso.setFechaUltimaActividad(ahora);
        caso.setMotivoModeracion(motivo);
        casoRepository.save(caso);
        log.info("W-04 Caso #{} archivado: {}", caso.getId(), motivo);
    }
}
