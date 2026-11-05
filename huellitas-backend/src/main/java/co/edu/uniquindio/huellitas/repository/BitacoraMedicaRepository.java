package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BitacoraMedicaRepository extends JpaRepository<BitacoraMedica, Long> {
    Optional<BitacoraMedica> findByCasoId(Long casoId);
    boolean existsByCasoId(Long casoId);

    @Query("""
            SELECT DISTINCT b FROM BitacoraMedica b
            LEFT JOIN b.registros r
            WHERE b.veterinario.id = :usuarioId
               OR (:organizacionId IS NOT NULL AND b.veterinaria.id = :organizacionId)
               OR r.firmante.id = :usuarioId
            ORDER BY b.fechaApertura DESC
            """)
    List<BitacoraMedica> findMisPacientes(@Param("usuarioId") Long usuarioId, @Param("organizacionId") Long organizacionId);
}
