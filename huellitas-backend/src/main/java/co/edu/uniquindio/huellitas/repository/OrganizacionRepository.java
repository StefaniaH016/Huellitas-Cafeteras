package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {
    boolean existsByNit(String nit);
}
