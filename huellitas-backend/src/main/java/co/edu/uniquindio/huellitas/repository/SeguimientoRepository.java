package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {
    List<Seguimiento> findByCasoIdAndVisibleTrueOrderByFechaAsc(Long casoId);
}
