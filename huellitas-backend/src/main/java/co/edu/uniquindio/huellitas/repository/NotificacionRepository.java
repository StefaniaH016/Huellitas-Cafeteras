package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    Page<Notificacion> findByUsuarioCorreoOrderByFechaDesc(String correo, Pageable pageable);
    Page<Notificacion> findByUsuarioCorreoAndLeidaOrderByFechaDesc(String correo, boolean leida, Pageable pageable);
}
