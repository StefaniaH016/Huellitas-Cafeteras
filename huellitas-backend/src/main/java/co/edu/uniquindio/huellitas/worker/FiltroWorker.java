package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.dto.reporte.CasoListItemDTO;
import co.edu.uniquindio.huellitas.dto.reporte.FiltrosCasoDTO;
import co.edu.uniquindio.huellitas.mapper.CasoMapper;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.specification.CasoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Worker W-02: Organización y Filtros.
 *
 * <p>Centraliza la lógica de consulta y filtrado de casos activos por urgencia,
 * tipo, especie y zona geográfica (departamento / municipio / barrio).
 * Usa {@link CasoSpecification} con {@code JpaSpecificationExecutor} para
 * construir predicados dinámicos sobre PostgreSQL, evitando múltiples
 * métodos de repositorio y garantizando combinaciones arbitrarias de filtros.
 *
 * <p>Opera de forma síncrona dentro del ciclo de vida de la petición HTTP,
 * respondiendo directamente con el listado paginado solicitado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FiltroWorker {

    private final CasoRepository casoRepository;
    private final CasoMapper casoMapper;

    /**
     * Aplica los filtros recibidos y devuelve la página de casos visibles
     * que coincidan con los criterios indicados.
     *
     * @param filtros  criterios de búsqueda (pueden ser nulos para listar todos)
     * @param pageable paginación y ordenación
     * @return página de {@link CasoListItemDTO}
     */
    public Page<CasoListItemDTO> filtrar(FiltrosCasoDTO filtros, Pageable pageable) {
        log.debug("FiltroWorker ejecutando consulta con filtros: {}", filtros);
        return casoRepository
                .findAll(CasoSpecification.conFiltros(filtros), pageable)
                .map(casoMapper::toListItemDTO);
    }
}
