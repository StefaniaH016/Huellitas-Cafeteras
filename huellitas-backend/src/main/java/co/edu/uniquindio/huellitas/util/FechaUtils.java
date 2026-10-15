package co.edu.uniquindio.huellitas.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class FechaUtils {

    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private FechaUtils() {}

    /**
     * Formatea un Instant en una cadena ISO-8601 con zona horaria de Colombia (-05:00).
     */
    public static String formatear(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONA_COLOMBIA).format(ISO_FORMATTER);
    }
}
