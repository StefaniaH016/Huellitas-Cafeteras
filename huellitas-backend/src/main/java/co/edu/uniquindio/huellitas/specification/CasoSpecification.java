package co.edu.uniquindio.huellitas.specification;

import co.edu.uniquindio.huellitas.dto.reporte.FiltrosCasoDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CasoSpecification {

    public static Specification<Caso> conFiltros(FiltrosCasoDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar por casos visibles en el feed público
            predicates.add(cb.isTrue(root.get("visible")));

            if (filtros != null && filtros.estado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtros.estado()));
            } else {
                // Subtarea 16.3: El feed público solo muestra casos activos (excluye RESUELTO y ARCHIVADO)
                predicates.add(cb.not(root.get("estado").in(EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO)));
            }

            if (filtros != null) {

                if (filtros.tipoCaso() != null) {
                    predicates.add(cb.equal(root.get("tipoCaso"), filtros.tipoCaso()));
                }

                if (filtros.urgencia() != null) {
                    predicates.add(cb.equal(root.get("urgencia"), filtros.urgencia()));
                }

                if (filtros.especie() != null) {
                    predicates.add(cb.equal(root.join("animal").get("especie"), filtros.especie()));
                }

                if (filtros.barrioId() != null) {
                    predicates.add(cb.equal(root.join("barrio").get("id"), filtros.barrioId()));
                } else if (filtros.municipioId() != null) {
                    predicates.add(cb.equal(root.join("barrio").join("municipio").get("id"), filtros.municipioId()));
                } else if (filtros.departamentoId() != null) {
                    predicates.add(cb.equal(root.join("barrio").join("municipio").join("departamento").get("id"), filtros.departamentoId()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
