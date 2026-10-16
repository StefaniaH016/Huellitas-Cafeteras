package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BitacoraMedicaRepository extends JpaRepository<BitacoraMedica, Long> {
    Optional<BitacoraMedica> findByCasoId(Long casoId);
    boolean existsByCasoId(Long casoId);
}
