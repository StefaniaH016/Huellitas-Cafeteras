package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE u.verificado = false AND u.rol IN (:roles)")
    List<Usuario> findByVerificadoFalseAndRolIn(List<Rol> roles);
}
