package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroMedicoRepository extends JpaRepository<RegistroMedico, Long> {
    List<RegistroMedico> findByBitacoraMedicaIdOrderByFechaAsc(Long bitacoraId);
}
