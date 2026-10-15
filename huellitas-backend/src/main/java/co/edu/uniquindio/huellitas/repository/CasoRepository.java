package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {
    Page<Caso> findByVisibleTrueAndEstado(EstadoCaso estado, Pageable pageable);
    Page<Caso> findByReportanteId(Long reportanteId, Pageable pageable);
    Page<Caso> findByReportanteCorreoOrderByFechaRegistroDesc(String correo, Pageable pageable);
    List<Caso> findByMotivoModeracionNotNullOrVisibleFalse();
}
