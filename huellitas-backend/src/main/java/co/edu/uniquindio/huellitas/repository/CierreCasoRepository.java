package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.CierreCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CierreCasoRepository extends JpaRepository<CierreCaso, Long> {
    Optional<CierreCaso> findByCasoId(Long casoId);
}
