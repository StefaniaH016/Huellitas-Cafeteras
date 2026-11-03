package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByCasoIdOrderByFechaAsc(Long casoId);
    void deleteByCasoId(Long casoId);
}
