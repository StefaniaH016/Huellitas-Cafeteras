package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Worker W-04: Limpieza y Cierre de Casos.
 *
 * <p>Tarea programada que se ejecuta automáticamente cada noche a las 02:00 AM.
 * Su objetivo es mantener el listado principal limpio y sin información obsoleta:
 *
 * <ul>
 *   <li>Archiva automáticamente los casos en estado RESUELTO que lleven más de
 *       30 días sin actividad, retirándolos del feed activo.</li>
 *   <li>Archiva los casos REGISTRADO o EN_ATENCION que llevan más de 90 días sin
 *       ninguna actualización, considerándolos inactivos.</li>
 *   <li>Envía recordatorios (vía {@link NotificacionWorker}) al reportante de
 *       casos que llevan entre 14 y 29 días sin actividad para que confirmen
 *       si aún requieren atención.</li>
 * </ul>
 *
 * <p>Usa {@code @Scheduled} habilitado por {@code @EnableScheduling} en
 * {@code HuellitasApplication}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LimpiezaWorker {

    /** Días sin actividad para archivar casos RESUELTOS. */
    private static final long DIAS_RESUELTO_PARA_ARCHIVAR = 30;

    /** Días sin actividad para archivar casos activos abandonados. */
    private static final long DIAS_ACTIVO_PARA_ARCHIVAR = 90;

    /** Días sin actividad para enviar recordatorio al reportante. */
    private static final long DIAS_PARA_RECORDATORIO = 14;

    private final CasoRepository casoRepository;
    private final NotificacionWorker notificacionWorker;

    /**
     * Ejecución diaria a las 02:00 AM.
     * Revisa todos los casos y aplica las reglas de limpieza.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void ejecutar() {
        log.info("W-04 LimpiezaWorker: iniciando revisión nocturna de casos...");

        Instant ahora = Instant.now();
        Instant limiteResuelto = ahora.minus(DIAS_RESUELTO_PARA_ARCHIVAR, ChronoUnit.DAYS);
        Instant limiteAbandono = ahora.minus(DIAS_ACTIVO_PARA_ARCHIVAR, ChronoUnit.DAYS);
        Instant limiteRecordatorio = ahora.minus(DIAS_PARA_RECORDATORIO, ChronoUnit.DAYS);

        List<Caso> todos = casoRepository.findAll();

        int archivados = 0;
        int recordatorios = 0;

        for (Caso caso : todos) {
            Instant ultimaActividad = caso.getFechaUltimaActividad() != null
                    ? caso.getFechaUltimaActividad()
                    : caso.getFechaRegistro();

            EstadoCaso estado = caso.getEstado();

            // ── Archivar casos RESUELTOS sin actividad reciente ───────
            if (estado == EstadoCaso.RESUELTO
                    && ultimaActividad.isBefore(limiteResuelto)) {
                archivar(caso, ahora,
                        "Archivado automáticamente tras " + DIAS_RESUELTO_PARA_ARCHIVAR
                                + " días en estado RESUELTO sin actividad.");
                archivados++;
                continue;
            }

            // ── Archivar casos activos abandonados (sin actividad >90d) ─
            if ((estado == EstadoCaso.REGISTRADO || estado == EstadoCaso.EN_ATENCION)
                    && ultimaActividad.isBefore(limiteAbandono)) {
                archivar(caso, ahora,
                        "Archivado automáticamente por inactividad superior a "
                                + DIAS_ACTIVO_PARA_ARCHIVAR + " días.");
                archivados++;
                continue;
            }

            // ── Recordatorio a los 14 días sin actividad ────────────────
            if ((estado == EstadoCaso.REGISTRADO || estado == EstadoCaso.EN_ATENCION)
                    && ultimaActividad.isBefore(limiteRecordatorio)
                    && caso.getReportante() != null) {
                notificacionWorker.notificarRecordatorio(caso);
                recordatorios++;
            }
        }

        log.info("W-04 LimpiezaWorker: {} casos archivados, {} recordatorios enviados.",
                archivados, recordatorios);
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
