package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Barrio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarrioRepository extends JpaRepository<Barrio, Long> {
    List<Barrio> findByMunicipioId(Long municipioId);
}
