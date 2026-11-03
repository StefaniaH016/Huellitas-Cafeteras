package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {
    Page<Caso> findByVisibleTrueAndEstado(EstadoCaso estado, Pageable pageable);
    Page<Caso> findByReportanteId(Long reportanteId, Pageable pageable);
    Page<Caso> findByReportanteCorreoOrderByFechaRegistroDesc(String correo, Pageable pageable);
    boolean existsByCasoOriginalId(Long casoOriginalId);
    boolean existsByAnimalIdAndIdNot(Long animalId, Long casoId);
    List<Caso> findByMotivoModeracionNotNullOrVisibleFalse();
    Page<Caso> findByUrgenciaInAndVisibleTrueOrderByFechaRegistroDesc(List<Urgencia> urgencias, Pageable pageable);

    // 13.4: Casos bajo custodia de una fundación específica
    Page<Caso> findByFundacionCustodioAndVisibleTrue(Fundacion fundacion, Pageable pageable);

    // 13.4: Casos sin custodia, visibles, de un municipio específico
    @Query("SELECT c FROM Caso c WHERE c.fundacionCustodio IS NULL AND c.visible = true AND c.barrio.municipio.id = :municipioId")
    Page<Caso> findBySinCustodiaAndMunicipioId(@Param("municipioId") Long municipioId, Pageable pageable);

    // Contar casos en custodia de una fundación
    long countByFundacionCustodio(Fundacion fundacion);
}
