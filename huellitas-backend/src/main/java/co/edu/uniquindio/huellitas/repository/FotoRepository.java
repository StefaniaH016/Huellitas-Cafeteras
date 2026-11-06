package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
    List<Foto> findByCasoId(Long casoId);
    boolean existsByCasoId(Long casoId);
    Optional<Foto> findFirstByCasoIdAndUrlAndSeguimientoIsNull(Long casoId, String url);
}
