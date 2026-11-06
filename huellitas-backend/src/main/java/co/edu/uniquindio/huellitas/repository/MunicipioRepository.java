package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    List<Municipio> findByDepartamentoId(Long departamentoId);
    Optional<Municipio> findFirstByNombreIgnoreCase(String nombre);
}
