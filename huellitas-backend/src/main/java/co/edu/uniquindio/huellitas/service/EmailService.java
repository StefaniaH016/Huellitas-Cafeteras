package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Fundacion;

/**
 * Interfaz para el servicio de envío de correos electrónicos.
 * Subtarea 13.7
 */
public interface EmailService {

    /**
     * Envía correo cuando una fundación asume la custodia de un caso.
     * Si falla, la operación principal no se ve afectada.
     */
    void enviarCorreoCustodia(Caso caso, Fundacion fundacion, String correoReportante);

    /**
     * Envía correo cuando un caso es cerrado/resuelto.
     * Si falla, la operación principal no se ve afectada.
     */
    void enviarCorreoCierre(Caso caso, String correoReportante, String resultado);

    /**
     * Envía correo cuando se genera una emergencia (ALTA o CRÍTICA) en la zona.
     * Si falla, la operación principal no se ve afectada.
     */
    void enviarCorreoEmergencia(Caso caso, String correoDestino);

    /**
     * Envía correo de recordatorio al reportante por inactividad del caso.
     * Subtarea 17.3
     */
    void enviarCorreoRecordatorio(Caso caso, String correoReportante, long diasInactividad);
}