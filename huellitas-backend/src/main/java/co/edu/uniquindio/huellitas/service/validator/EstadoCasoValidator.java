package co.edu.uniquindio.huellitas.service.validator;

import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Validador único y centralizado de transiciones de ciclo de vida de {@link EstadoCaso}.
 *
 * <p>Flujo base: REGISTRADO -> EN_ATENCION -> RESUELTO -> ARCHIVADO.
 * <p>Reglas:
 * <ul>
 *     <li>REGISTRADO puede pasar a EN_ATENCION, RESUELTO o ARCHIVADO.</li>
 *     <li>EN_ATENCION puede pasar a RESUELTO o ARCHIVADO (no puede retroceder a REGISTRADO).</li>
 *     <li>RESUELTO puede pasar a ARCHIVADO (no puede regresar a EN_ATENCION ni REGISTRADO).</li>
 *     <li>ARCHIVADO es estado terminal; no permite transiciones.</li>
 *     <li>La auto-transición (mismo estado) no es permitida.</li>
 * </ul>
 */
@Component
public class EstadoCasoValidator {

    private static final Map<EstadoCaso, Set<EstadoCaso>> TRANSICIONES_VALIDAS = Map.of(
            EstadoCaso.REGISTRADO, Set.of(EstadoCaso.EN_ATENCION, EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO),
            EstadoCaso.EN_ATENCION, Set.of(EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO),
            EstadoCaso.RESUELTO, Set.of(EstadoCaso.ARCHIVADO),
            EstadoCaso.ARCHIVADO, Set.of()
    );

    /**
     * Valida si la transición entre el estado actual y el nuevo estado es permitida.
     *
     * @param actual estado actual del caso
     * @param nuevo  nuevo estado solicitado
     * @throws ConflictException si la transición no está permitida o si es el mismo estado
     * @throws IllegalArgumentException si alguno de los estados es nulo
     */
    public void validarTransicion(EstadoCaso actual, EstadoCaso nuevo) {
        if (actual == null || nuevo == null) {
            throw new IllegalArgumentException("Los estados actual y nuevo no pueden ser nulos.");
        }

        if (actual == nuevo) {
            throw new ConflictException("El caso ya se encuentra en estado " + actual);
        }

        Set<EstadoCaso> permitidos = TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new ConflictException(String.format(
                    "Transición de estado inválida: no es posible pasar de %s a %s.",
                    actual, nuevo));
        }
    }

    /**
     * Comprueba si una transición es válida sin lanzar excepción.
     */
    public boolean esTransicionValida(EstadoCaso actual, EstadoCaso nuevo) {
        if (actual == null || nuevo == null || actual == nuevo) {
            return false;
        }
        return TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of()).contains(nuevo);
    }
}
